package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Ghast$RandomFloatAroundGoal#start} picks a new move-control wanted position
 * by offsetting the ghast's current XYZ with
 * {@code (nextFloat() * 2.0F - 1.0F) * 16.0F} on each axis — a ±16 block random float cube.
 * Every time the goal restarts (when the previous wanted point is reached or is farther than
 * 3600 distSq), the move controller then steers through that large volume. Dense nether skies
 * with many idle ghasts therefore re-roll far wander targets often; shrinking the cube cuts
 * the typical move distance and how long the controller spends chasing distant idle points
 * without changing fireball combat goals.
 *
 * <p>Policy: one {@code @ModifyConstant} on {@code start} rewrites every {@code float} literal
 * {@code 16.0F} → {@code 12.0F}. That hits the three axis multipliers (X, Y, Z) in a single
 * injector. The unit-cube factors {@code 2.0F}/{@code 1.0F} and the move-control speed
 * {@code 1.0D} are intentionally left alone.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code Ghast$RandomFloatAroundGoal}):
 * <ul>
 *   <li>{@code start}: exactly three {@code 16.0F} literals — one per axis offset multiplier</li>
 *   <li>{@code start}: also {@code 2.0F}, {@code 1.0F} (unit cube), and {@code 1.0D} (speed)</li>
 *   <li>{@code canUse}: only {@code 1.0D} / {@code 3600.0D} reach checks — no {@code 16.0F}</li>
 *   <li>{@code canContinueToUse}: returns {@code false}; no float radius literals</li>
 * </ul>
 * {@code ModifyConstant} on {@code floatValue = 16.0F} in {@code start} is therefore safe
 * without ordinal narrowing.
 *
 * <p>Trade-off: ghast random float wanders a smaller cube (±12 instead of ±16 on each axis;
 * volume factor {@code (12/16)³ = 0.421875} of vanilla). Idle roam is slightly tighter around
 * the spawn/current area; look and shoot-fireball goals are out of scope. Not panic/flee —
 * no urgency exception required (idle wander only; combat charge lives on
 * {@code GhastShootFireballGoal}).
 *
 * <p>Unit: {@code perf.ghast_wander_radius} (gated by mixin plugin / FeatureUnits).
 *
 * @see FlyingHoverRadiusMixin sibling flying wander radius {@code @ModifyConstant} pattern
 * @see WaterAvoidStrollRadiusMixin sibling land wander radius {@code @ModifyConstant} pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$RandomFloatAroundGoal")
public abstract class GhastWanderRadiusMixin {

    /**
     * Rewrite every {@code 16.0F} in {@code start} (X/Y/Z wanted-position offset scale) to
     * {@code 12.0F}. Unit-cube and speed constants are not matched.
     *
     * @param original vanilla constant (always 16.0F at the matched sites)
     * @return reduced random-float wander half-extent per axis
     */
    @ModifyConstant(method = "start", constant = @Constant(floatValue = 16.0F))
    private float omnifix$shrinkGhastWanderRadius(float original) {
        return 12.0F;
    }
}
