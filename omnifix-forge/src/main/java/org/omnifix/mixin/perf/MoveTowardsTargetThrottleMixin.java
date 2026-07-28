package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link MoveTowardsTargetGoal#canUse} runs target-distance and random-pos path
 * searches every goal-selector evaluation (e.g. iron golems approaching a combat target).
 * Evaluating only on even entity ticks halves pathfinder pressure without removing the approach.
 *
 * <p>Trade-off: approach may start up to ~1 tick later (~50&nbsp;ms). Not a panic/flee goal —
 * fire and hurt urgency are unaffected.
 */
@Mixin(MoveTowardsTargetGoal.class)
public abstract class MoveTowardsTargetThrottleMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    /**
     * Rejects {@code canUse} on odd entity ticks so approach attempts run every other tick.
     *
     * @param cir cancelable return for {@link MoveTowardsTargetGoal#canUse}
     */
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleMoveTowardsTarget(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount & 1) != 0) {
            cir.setReturnValue(false);
        }
    }
}
