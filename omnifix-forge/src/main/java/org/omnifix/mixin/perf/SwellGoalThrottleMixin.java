package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link SwellGoal#canUse} re-evaluates target presence, distance, and line-of-sight
 * on every goal-selector pass for every creeper. Halving evaluation frequency cuts steady-state
 * visibility / target checks when many creepers are idle or hunting without removing fuse
 * behavior once ignition has begun.
 *
 * <p>Hard law: never throttle when the creeper is already swelling ({@code getSwellDir() > 0})
 * so fuse progress and re-entry while primed stay on every goal evaluation.
 *
 * <p>Trade-off: non-swelling creepers may start the swell check up to 1 tick later; once
 * swelling, full-rate {@code canUse} is preserved.
 */
@Mixin(SwellGoal.class)
public abstract class SwellGoalThrottleMixin {

    @Shadow
    @Final
    private Creeper creeper;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleSwell(CallbackInfoReturnable<Boolean> cir) {
        // Already swelling — hot path; never throttle fuse re-entry / progress.
        if (this.creeper.getSwellDir() > 0) {
            return;
        }
        if ((this.creeper.tickCount & 1) != 0) {
            cir.setReturnValue(false);
        }
    }
}
