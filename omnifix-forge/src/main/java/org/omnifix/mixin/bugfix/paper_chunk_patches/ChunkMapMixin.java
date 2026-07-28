package org.omnifix.mixin.bugfix.paper_chunk_patches;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Paper-class ChunkMap scheduling fixes (ModernFix paper_chunk_patches port).
 *
 * <ul>
 *   <li>Accessible-chunk promotion must complete on the main thread executor (low-TPS unload fix).</li>
 *   <li>Avoid scheduling generation when the parent status future is still incomplete — wait then retry.</li>
 * </ul>
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Shadow
    @Final
    private BlockableEventLoop<Runnable> mainThreadExecutor;

    @ModifyArg(
            method = "prepareAccessibleChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 1
    )
    private Executor omnifix$useMainThreadExecutor(Executor executor) {
        return this.mainThreadExecutor;
    }

    @WrapOperation(
            method = "schedule",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ChunkMap;scheduleChunkGeneration(Lnet/minecraft/server/level/ChunkHolder;Lnet/minecraft/world/level/chunk/ChunkStatus;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>
    omnifix$avoidPrematureGeneration(
            ChunkMap map,
            ChunkHolder holder,
            ChunkStatus status,
            Operation<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> original
    ) {
        if (!status.hasLoadDependencies()) {
            var parentFuture = holder.getOrScheduleFuture(status.getParent(), map);
            if (!parentFuture.isDone()) {
                return parentFuture.thenComposeAsync(
                        either -> map.schedule(holder, status),
                        this.mainThreadExecutor
                );
            }
        }
        return original.call(map, holder, status);
    }
}
