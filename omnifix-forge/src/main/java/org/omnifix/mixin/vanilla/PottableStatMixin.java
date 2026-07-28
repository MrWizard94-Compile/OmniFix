package org.omnifix.mixin.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-231743 — {@code minecraft.used:minecraft.*} does not increase when placing plants in flower pots.
 *
 * <p>Root cause: {@link FlowerPotBlock#use} awards {@link Stats#POT_FLOWER} when potting a plant but
 * never awards {@link Stats#ITEM_USED} for the held plant item. Mirror other place/use paths and
 * award ITEM_USED alongside POT_FLOWER.
 */
@Mixin(FlowerPotBlock.class)
public abstract class PottableStatMixin {

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/ResourceLocation;)V"
            )
    )
    private void omnifix$awardItemUsedWhenPotting(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<?> cir
    ) {
        player.awardStat(Stats.ITEM_USED.get(player.getItemInHand(hand).getItem()));
    }
}
