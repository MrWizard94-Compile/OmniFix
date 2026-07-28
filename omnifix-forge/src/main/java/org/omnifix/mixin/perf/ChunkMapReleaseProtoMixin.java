package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.omnifix.duck.IClearableChunkHolder;
import org.omnifix.duck.ISuspendedHolderTrackingChunkMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

/**
 * Suspends idle protochunk holders (not FULL) after generation work completes so their futures
 * and in-memory proto data can be reclaimed.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapReleaseProtoMixin implements ISuspendedHolderTrackingChunkMap {

    @Unique
    private static final int OMNIFIX$TICKS_BEFORE_SUSPEND = 100;

    @Shadow
    @Final
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;

    @Shadow
    @Final
    private BlockableEventLoop<Runnable> mainThreadExecutor;

    @Shadow
    protected abstract void lambda$scheduleUnload$14(
            ChunkHolder holder,
            CompletableFuture<ChunkAccess> chunkToSaveFuture,
            long chunkPos,
            ChunkAccess chunk
    );

    @Shadow
    @Final
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads;

    @Unique
    private final Long2IntOpenHashMap omnifix$protoChunksToDrop = new Long2IntOpenHashMap();

    @Unique
    private int omnifix$dropTickCounter;

    @Inject(method = "processUnloads(Ljava/util/function/BooleanSupplier;)V", at = @At("RETURN"))
    private void omnifix$dropProtoChunks(BooleanSupplier hasMoreTime, CallbackInfo ci) {
        int suspended = 0;
        int iterations = 0;
        omnifix$dropTickCounter++;
        var dropIterator = omnifix$protoChunksToDrop.long2IntEntrySet().fastIterator();
        while (dropIterator.hasNext()
                && suspended < 50
                && iterations < 500
                && (hasMoreTime.getAsBoolean() || omnifix$protoChunksToDrop.size() > 1000)) {
            iterations++;
            var entry = dropIterator.next();
            long pos = entry.getLongKey();
            ChunkHolder holder = this.updatingChunkMap.get(pos);
            if (holder == null
                    || holder.getTicketLevel() < IClearableChunkHolder.LOWEST_DROPPABLE_TICKET_LEVEL
                    || !ChunkLevel.isLoaded(holder.getTicketLevel())) {
                dropIterator.remove();
                continue;
            }
            if (!holder.getChunkToSave().isDone()
                    || ((IClearableChunkHolder) holder).omnifix$getGenerationRefCount().get() != 0) {
                entry.setValue(omnifix$dropTickCounter);
                continue;
            }
            if ((omnifix$dropTickCounter - entry.getIntValue()) < OMNIFIX$TICKS_BEFORE_SUSPEND) {
                continue;
            }
            dropIterator.remove();
            var chunkToSaveFuture = holder.getChunkToSave();
            this.pendingUnloads.put(pos, holder);
            this.lambda$scheduleUnload$14(holder, chunkToSaveFuture, pos, chunkToSaveFuture.getNow(null));
            ((IClearableChunkHolder) holder).omnifix$resetProtoChunkFutures();
            suspended++;
        }
    }

    @Inject(method = "scheduleChunkGeneration", at = @At("HEAD"))
    private void omnifix$incrementGenRefCounts(
            ChunkHolder chunkHolder,
            ChunkStatus chunkStatus,
            CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir
    ) {
        int range = chunkStatus.getRange();
        ChunkPos center = chunkHolder.getPos();
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                ChunkHolder neighbor = this.updatingChunkMap.get(ChunkPos.asLong(center.x + dx, center.z + dz));
                if (neighbor != null) {
                    ((IClearableChunkHolder) neighbor).omnifix$getGenerationRefCount().incrementAndGet();
                }
            }
        }
    }

    @ModifyReturnValue(method = "scheduleChunkGeneration", at = @At("RETURN"))
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> omnifix$decrementGenRefCounts(
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> future,
            @Local(ordinal = 0, argsOnly = true) ChunkHolder chunkHolder,
            @Local(ordinal = 0, argsOnly = true) ChunkStatus chunkStatus
    ) {
        int range = chunkStatus.getRange();
        ChunkPos center = chunkHolder.getPos();
        return future.whenCompleteAsync((result, error) -> {
            for (int dx = -range; dx <= range; dx++) {
                for (int dz = -range; dz <= range; dz++) {
                    ChunkHolder neighbor = this.updatingChunkMap.get(ChunkPos.asLong(center.x + dx, center.z + dz));
                    if (neighbor != null) {
                        ((IClearableChunkHolder) neighbor).omnifix$getGenerationRefCount().decrementAndGet();
                    }
                }
            }
        }, this.mainThreadExecutor);
    }

    @Override
    public void omnifix$markForSuspensionCheck(ChunkPos pos) {
        this.omnifix$protoChunksToDrop.put(pos.toLong(), this.omnifix$dropTickCounter);
    }

    @Override
    public Executor omnifix$getMainThreadExecutor() {
        return this.mainThreadExecutor;
    }
}
