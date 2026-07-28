package org.omnifix.mixin.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MC-160095 — Cactus breaks when an end rod (or other non-solid) is pushed beside it by a piston.
 *
 * <p>Root cause: {@link CactusBlock#canSurvive} treats {@link Blocks#MOVING_PISTON} as solid via
 * {@link BlockState#isSolid()}, even when the piston is carrying a non-solid block such as an end
 * rod. Use the moving-piston block entity's {@link PistonMovingBlockEntity#getMovedState()} for
 * the horizontal neighbor solidity check so only the carried block is considered.
 */
@Mixin(CactusBlock.class)
public abstract class EndRodCactusMixin {

    @Redirect(
            method = "canSurvive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                    ordinal = 0
            )
    )
    private BlockState omnifix$useMovedStateForHorizontalNeighbor(net.minecraft.world.level.LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.MOVING_PISTON)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PistonMovingBlockEntity piston) {
                return piston.getMovedState();
            }
        }
        return state;
    }
}
