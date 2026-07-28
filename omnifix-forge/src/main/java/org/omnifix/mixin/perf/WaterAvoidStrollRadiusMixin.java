package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link WaterAvoidingRandomStrollGoal#getPosition} samples a land wander target
 * via {@code LandRandomPos.getPos(mob, 15, 7)} when the mob is in water / rain (wet escape)
 * and {@code LandRandomPos.getPos(mob, 10, 7)} when dry. Horizontal radii 15 and 10 drive
 * pathfinder candidate volume for every water-avoiding repath (most land animals, villagers'
 * random stroll subclass, etc.). Dense herds re-run these samples often; shrinking the
 * horizontal cylinder cuts path-search cost without changing repath cadence.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on {@code getPosition}:
 * <ul>
 *   <li>{@code intValue = 15} → {@code 12} (wet / in-water XZ sample radius)</li>
 *   <li>{@code intValue = 10} → {@code 8} (dry XZ sample radius)</li>
 * </ul>
 * Vertical sample height {@code 7} is intentionally left alone.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code getPosition}): the method body contains the
 * wet path {@code LandRandomPos.getPos(this.mob, 15, 7)} and the dry path
 * {@code LandRandomPos.getPos(this.mob, 10, 7)} (plus a probability branch that does not
 * introduce other {@code 15}/{@code 10} ints). No other {@code 15} or {@code 10} exists in
 * the method, so {@code ModifyConstant} on those values is safe without ordinal narrowing.
 * The shared Y literal {@code 7} is not matched.
 *
 * <p>Trade-off: water-avoiding stroll samples slightly smaller XZ radii (wet 15→12, dry
 * 10→8; ~20% horizontal shrink). Vertical sampling stays vanilla (Y 7). Mobs wander slightly
 * less far per repath; path quality inside the smaller radius is unchanged. Not panic/flee —
 * no urgency exception required (goal only picks idle wander targets, never fire/hurt escape).
 *
 * <p>Unit: {@code perf.water_avoid_stroll_radius} (gated by mixin plugin / FeatureUnits).
 *
 * @see FlyingHoverRadiusMixin sibling flying water-avoid radius {@code @ModifyConstant} pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 * @see SecondaryPoiRadiusMixin sibling dual-constant radius-shrink pattern
 */
@Mixin(WaterAvoidingRandomStrollGoal.class)
public abstract class WaterAvoidStrollRadiusMixin {

    /**
     * Wet path: {@code LandRandomPos.getPos(mob, 15, 7)} horizontal radius 15 → 12.
     */
    @ModifyConstant(method = "getPosition", constant = @Constant(intValue = 15))
    private int omnifix$shrinkWetStrollRadius(int original) {
        return 12;
    }

    /**
     * Dry path: {@code LandRandomPos.getPos(mob, 10, 7)} horizontal radius 10 → 8.
     * Y constant {@code 7} is not matched by either injector.
     */
    @ModifyConstant(method = "getPosition", constant = @Constant(intValue = 10))
    private int omnifix$shrinkDryStrollRadius(int original) {
        return 8;
    }
}
