package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: baby animals run {@link FollowParentGoal#canUse} with an inflated AABB
 * {@code getEntitiesOfClass} every goal-selector evaluation. Throttling to every third tick cuts
 * entity section queries on dense animal pens.
 *
 * <p>Trade-off: babies may re-acquire parents up to ~2 ticks later.
 */
@Mixin(FollowParentGoal.class)
public abstract class FollowParentThrottleMixin {

    @Shadow
    @Final
    private Animal animal;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleFollowParent(CallbackInfoReturnable<Boolean> cir) {
        if ((this.animal.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
