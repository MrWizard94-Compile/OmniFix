package org.omnifix.mixin.perf;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.omnifix.duck.IClearableChunkHolder;
import org.omnifix.duck.ISuspendedHolderTrackingChunkMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderReleaseProtoMixin implements IClearableChunkHolder {

    @Shadow
    @Final
    private AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures;

    @Shadow
    private CompletableFuture<ChunkAccess> chunkToSave;

    @Shadow
    private int ticketLevel;

    @Shadow
    @Final
    private ChunkPos pos;

    @Shadow
    @Final
    private ChunkHolder.PlayerProvider playerProvider;

    @Unique
    private final AtomicInteger omnifix$generationRefCount = new AtomicInteger(0);

    @Override
    public void omnifix$resetProtoChunkFutures() {
        int len = this.futures.length();
        for (int i = 0; i < len; i++) {
            this.futures.set(i, null);
        }
        this.chunkToSave = CompletableFuture.completedFuture(null);
    }

    @Override
    public AtomicInteger omnifix$getGenerationRefCount() {
        return this.omnifix$generationRefCount;
    }

    @Inject(method = "addSaveDependency", at = @At("RETURN"))
    private void omnifix$recheckSuspensionAfterNeighbor(String source, CompletableFuture<?> future, CallbackInfo ci) {
        omnifix$markAsNeedingProtoChunkDrop();
    }

    @Inject(method = "updateChunkToSave", at = @At("RETURN"))
    private void omnifix$checkSuspension(CallbackInfo ci) {
        omnifix$markAsNeedingProtoChunkDrop();
    }

    @Inject(method = "updateFutures", at = @At("RETURN"))
    private void omnifix$markForSuspensionOnDemotion(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        omnifix$markAsNeedingProtoChunkDrop();
    }

    @Unique
    private void omnifix$markAsNeedingProtoChunkDrop() {
        if (this.ticketLevel >= LOWEST_DROPPABLE_TICKET_LEVEL && ChunkLevel.isLoaded(this.ticketLevel)) {
            var map = (ISuspendedHolderTrackingChunkMap) this.playerProvider;
            this.chunkToSave.whenCompleteAsync(
                    (r, e) -> map.omnifix$markForSuspensionCheck(this.pos),
                    map.omnifix$getMainThreadExecutor()
            );
        }
    }
}
