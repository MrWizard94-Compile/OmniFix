package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.omnifix.util.blockpos.SectionBlockPosIterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Chunk mesh rebuild: faster section iteration + reuse already-fetched BlockState.
 */
@Mixin(
        targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask",
        priority = 2000
)
public abstract class ChunkRebuildFastIteratorMixin {

    @Redirect(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;betweenClosed(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;"
            ),
            require = 0
    )
    private Iterable<BlockPos> omnifix$fastBetweenClosed(BlockPos firstPos, BlockPos secondPos) {
        return () -> new SectionBlockPosIterator(firstPos);
    }

    @Redirect(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                    ordinal = 1
            ),
            require = 0
    )
    private BlockState omnifix$useExistingBlockState(
            RenderChunkRegion instance,
            BlockPos pos,
            @Local(ordinal = 0) BlockState state
    ) {
        return state;
    }
}
