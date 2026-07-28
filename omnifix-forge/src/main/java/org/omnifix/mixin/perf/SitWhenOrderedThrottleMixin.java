package org.omnifix.mixin.perf;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link SitWhenOrderedToGoal#canUse} re-checks owner/sit gates every goal-selector
 * evaluation for every tamed pet. When the pet is not currently ordered to sit, that check is
 * idle work; throttling to every third tick cuts steady-state goal pressure on pet farms.
 *
 * <p>Hard law: when {@code isOrderedToSit()} is already true, evaluation stays full-rate so
 * player sit orders remain responsive (no delayed sit response).
 *
 * <p>Trade-off: idle non-sitting pets re-check owner/sit gates less often (up to ~2 ticks later);
 * player sit order is full-rate.
 *
 * <p>Unit: {@code perf.sit_when_ordered_throttle}
 */
@Mixin(SitWhenOrderedToGoal.class)
public abstract class SitWhenOrderedThrottleMixin {

    @Shadow
    @Final
    private TamableAnimal mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleSitWhenOrdered(CallbackInfoReturnable<Boolean> cir) {
        // Ordered sits must stay responsive.
        if (this.mob.isOrderedToSit()) {
            return;
        }
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
