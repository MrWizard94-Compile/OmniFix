package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.ai.goal.BegGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link BegGoal#canUse} scans for nearby players holding food every goal evaluation
 * for every wolf. Throttling to every third tick cuts player queries without removing begging.
 *
 * <p>Trade-off: wolves may notice food up to ~2 ticks later.
 */
@Mixin(BegGoal.class)
public abstract class BegGoalThrottleMixin {

    @Shadow
    @Final
    private Wolf wolf;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleBeg(CallbackInfoReturnable<Boolean> cir) {
        if ((this.wolf.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
