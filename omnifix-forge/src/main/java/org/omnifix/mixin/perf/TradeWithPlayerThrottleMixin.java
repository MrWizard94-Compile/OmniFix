package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link TradeWithPlayerGoal#canUse} re-checks villager state and trading-player
 * distance every goal evaluation for every villager. Throttle to every third tick.
 *
 * <p>Trade-off: trade-focus acquisition may lag up to ~2 ticks when a player opens trading.
 *
 * <p>Unit: {@code perf.trade_with_player_throttle}
 */
@Mixin(TradeWithPlayerGoal.class)
public abstract class TradeWithPlayerThrottleMixin {

    @Shadow
    @Final
    private AbstractVillager mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleTradeWithPlayer(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
