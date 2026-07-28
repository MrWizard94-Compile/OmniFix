package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link LeapAtTargetGoal#canUse} runs distance/random leap checks every goal evaluation
 * for cats, ocelots, wolves, foxes, and similar leapers. Skipping odd ticks halves leap-attempt
 * evaluations without removing the behavior.
 *
 * <p>Trade-off: leap windows may open up to ~1 tick later (~50&nbsp;ms).
 */
@Mixin(LeapAtTargetGoal.class)
public abstract class LeapAtTargetThrottleMixin {

    @Shadow
    @Final
    private Mob mob;

    /**
     * Rejects {@code canUse} on odd entity ticks so leap attempts run every other tick.
     *
     * @param cir cancelable return for {@link LeapAtTargetGoal#canUse}
     */
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleLeapAtTarget(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount & 1) != 0) {
            cir.setReturnValue(false);
        }
    }
}
