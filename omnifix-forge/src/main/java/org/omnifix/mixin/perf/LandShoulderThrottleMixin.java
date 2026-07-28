package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link LandOnOwnersShoulderGoal#canUse} re-evaluates owner proximity and shoulder
 * readiness every goal tick for every parrot (and other shoulder-riding pets). Dense pet farms
 * pay repeated owner-entity checks that need not run every tick.
 *
 * <p>Trade-off: birds may land on the owner's shoulder up to ~2 ticks later (~100&nbsp;ms).
 */
@Mixin(LandOnOwnersShoulderGoal.class)
public abstract class LandShoulderThrottleMixin {

    @Shadow
    @Final
    private ShoulderRidingEntity entity;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleLandShoulder(CallbackInfoReturnable<Boolean> cir) {
        if ((this.entity.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
