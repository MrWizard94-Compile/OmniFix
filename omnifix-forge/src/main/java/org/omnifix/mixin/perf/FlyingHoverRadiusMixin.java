package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link WaterAvoidingRandomFlyingGoal#getPosition} samples a flying wander target
 * via {@code HoverRandomPos.getPos(mob, 8, 7, ...)} and falls back to
 * {@code AirAndWaterRandomPos.getPos(mob, 8, 4, -2, ...)}. Both paths use horizontal radius
 * {@code 8}, which drives pathfinder candidate volume for every random-fly repath (parrots,
 * bees when grounded into this goal, allays' related wander, etc. depending on registration).
 * Dense flocks re-run these samples often; shrinking the horizontal cylinder cuts path-search
 * cost without changing repath cadence.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code getPosition} rewrites every {@code int} literal
 * {@code 8} → {@code 6}. That hits the dead local {@code int i = 8}, the
 * {@code HoverRandomPos} horizontal radius, and the {@code AirAndWaterRandomPos} horizontal
 * radius in one injector.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code getPosition}): the method body contains exactly
 * three {@code int} literals with value {@code 8} — all horizontal hover radius. Other ints
 * are intentionally left alone: {@code 7} (HoverRandomPos max Y), {@code 3}/{@code 1}
 * (HoverRandomPos solid-clearance counts), {@code 4} (AirAndWater Y range), and {@code -2}
 * (AirAndWater Y offset). No other {@code 8} exists, so {@code ModifyConstant} on
 * {@code intValue = 8} is safe without ordinal narrowing.
 *
 * <p>Trade-off: flying random wander samples a smaller horizontal hover cylinder (radius 6
 * instead of 8). Vertical sampling stays vanilla (Y 7 / 4 / -2). Mobs wander slightly less
 * far per repath; path quality inside the smaller radius is unchanged. Not panic/flee — no
 * urgency exception required.
 *
 * <p>Unit: {@code perf.flying_hover_radius} (gated by mixin plugin / FeatureUnits).
 *
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 * @see SecondaryPoiRadiusMixin sibling radius-shrink {@code @ModifyConstant} pattern
 */
@Mixin(WaterAvoidingRandomFlyingGoal.class)
public abstract class FlyingHoverRadiusMixin {

    /**
     * Rewrite every {@code 8} in {@code getPosition} (local + HoverRandomPos +
     * AirAndWaterRandomPos horizontal radii) to {@code 6}. Vertical constants are not matched.
     */
    @ModifyConstant(method = "getPosition", constant = @Constant(intValue = 8))
    private int omnifix$shrinkFlyingHoverRadius(int original) {
        return 6;
    }
}
