package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Unit: {@code perf.float_goal_throttle}
 *
 * <p>Root cause: {@link FloatGoal#canUse} is registered on nearly every swimming-capable mob and
 * re-evaluates fluid immersion / fluid height on every goal-selector pass even when the mob is
 * fully dry. That fluid-height probe is pure waste for land herds (cows, sheep, pigs, villagers,
 * etc.) that only need float behavior once they actually enter water or lava.
 *
 * <p>Policy: on {@code canUse} HEAD, if the mob is <em>not</em> in water and <em>not</em> in lava
 * and {@code (mob.tickCount % 3) != 0}, cancel with {@code false}. When already wet or in lava,
 * never throttle so float remains urgent for drowning / lava survival.
 *
 * <p>Trade-off: dry mobs may begin the float goal up to ~2 ticks later after entering fluid
 * (goal-selector still runs; only this canUse probe is sparse). Active float
 * ({@code tick}/{@code canContinueToUse}) is unchanged once running. Wet/lava path is hot every
 * evaluation — drowning urgency is preserved.
 */
@Mixin(FloatGoal.class)
public abstract class FloatGoalThrottleMixin {

    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleFloat(CallbackInfoReturnable<Boolean> cir) {
        // Never throttle when already in water or lava — float must stay urgent for drowning.
        if (this.mob.isInWater() || this.mob.isInLava()) {
            return;
        }
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
