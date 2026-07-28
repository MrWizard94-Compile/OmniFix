package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

/**
 * MC-224729 — Partially generated chunks are not saved in some situations.
 *
 * <p>Root cause: {@link ChunkMap#saveAllChunks} and {@link ChunkMap#saveChunkIfNeeded} only flush
 * holders that {@link ChunkHolder#wasAccessibleSinceLastSave()}, and the full-flush path further
 * filters to {@code ImposterProtoChunk || LevelChunk}. Real {@link ProtoChunk}s from unfinished
 * generation are dropped, so border features can regenerate incorrectly after a restart.
 *
 * <p>Fix: on full flush, always consider holders accessible and keep protochunks in the save
 * stream; on incremental save, always treat holders as accessible so partial work is persisted.
 */
@Mixin(ChunkMap.class)
public abstract class PartialChunkSaveMixin {

    @ModifyArg(
            method = "saveAllChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;",
                    ordinal = 0
            )
    )
    private Predicate<ChunkHolder> omnifix$alwaysAccessibleFlushSave(Predicate<ChunkHolder> predicate) {
        return chunkHolder -> true;
    }

    @ModifyArg(
            method = "saveAllChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;",
                    ordinal = 1
            )
    )
    private Predicate<ChunkAccess> omnifix$saveProtoChunks(Predicate<ChunkAccess> predicate) {
        return chunk -> predicate.test(chunk) || chunk instanceof ProtoChunk;
    }

    @ModifyExpressionValue(
            method = "saveChunkIfNeeded",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ChunkHolder;wasAccessibleSinceLastSave()Z"
            )
    )
    private boolean omnifix$alwaysAccessibleChunkSave(boolean accessible) {
        return true;
    }
}
