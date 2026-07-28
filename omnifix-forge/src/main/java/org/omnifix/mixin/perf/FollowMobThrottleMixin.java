package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link FollowMobGoal#canUse} runs an inflated AABB {@code getEntitiesOfClass} for
 * nearby mobs every goal evaluation (baby animals following adults, etc.). Throttle to every third
 * tick.
 *
 * <p>Trade-off: follow acquisition may lag up to ~2 ticks.
 */
@Mixin(FollowMobGoal.class)
public abstract class FollowMobThrottleMixin {

    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleFollowMob(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
