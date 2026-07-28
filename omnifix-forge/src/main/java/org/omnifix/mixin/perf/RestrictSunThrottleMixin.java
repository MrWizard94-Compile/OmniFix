package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link RestrictSunGoal#canUse} evaluates day time, remaining fire ticks, and
 * {@code canSeeSky} at the mob position on every goal-selector evaluation for undead that use it
 * (skeletons, etc.). Throttling to every third mob tick cuts repeated sky/light probes in large
 * outdoor undead groups without removing sun-avoidance pathing.
 *
 * <p>Trade-off: sun-restriction may engage or disengage up to ~2 ticks later.
 */
@Mixin(RestrictSunGoal.class)
public abstract class RestrictSunThrottleMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleRestrictSun(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
