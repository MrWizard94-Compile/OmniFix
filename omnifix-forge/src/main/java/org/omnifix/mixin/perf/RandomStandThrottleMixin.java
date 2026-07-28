package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.ai.goal.RandomStandGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link RandomStandGoal#canUse} is evaluated every goal-selector pass for idle
 * horses (ambient stand animation acquisition). Throttle to every third horse tick.
 *
 * <p>Trade-off: horse stand ambient anim checks are less frequent (may lag up to ~2 ticks).
 * No panic/flee or fire-urgency path is affected — this goal is ambient-only.
 *
 * <p>Unit: {@code perf.random_stand_throttle}
 */
@Mixin(RandomStandGoal.class)
public abstract class RandomStandThrottleMixin {

    @Shadow
    @Final
    private AbstractHorse horse;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleRandomStand(CallbackInfoReturnable<Boolean> cir) {
        if ((this.horse.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
