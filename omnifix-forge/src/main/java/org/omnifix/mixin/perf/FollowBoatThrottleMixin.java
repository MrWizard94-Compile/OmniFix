package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link FollowBoatGoal#canUse} scans for nearby boats and players every goal-selector
 * evaluation. On busy shores or river villages this is a repeated entity-section query with no
 * benefit from sub-tick refresh. Throttle acquisition checks to every third mob tick.
 *
 * <p>Trade-off: boat-follow acquisition may lag up to ~2 ticks.
 */
@Mixin(FollowBoatGoal.class)
public abstract class FollowBoatThrottleMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleFollowBoat(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
