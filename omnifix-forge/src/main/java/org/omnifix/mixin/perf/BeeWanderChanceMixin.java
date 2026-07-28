package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Bee$BeeWanderGoal#canUse} rolls {@code random.nextInt(10) == 0} every
 * goal-selector pass while navigation is idle. Dense bee swarms therefore start hover/air
 * wander pathfinding on a 1/10 cadence whenever not already pathing; path creation and
 * {@code HoverRandomPos}/{@code AirAndWaterRandomPos} probes dominate AI time without
 * improving pollination, hive return, or combat behavior.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code canUse} only rewrites the sole {@code int}
 * literal {@code 10} → {@code 15} (nextInt gate: 1/10 → 1/15). Audit (MC 1.20.1 mapped
 * {@code Bee$BeeWanderGoal}):
 * <ul>
 *   <li>{@code canUse}: sole {@code int 10} — {@code random.nextInt(10) == 0}</li>
 *   <li>{@code canContinueToUse}: navigation-in-progress check only; no int literals</li>
 *   <li>{@code start}/{@code findPos}: other ints ({@code 8}, {@code 7}, {@code 22}, …) live
 *       in different methods — out of scope</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 10} in {@code canUse} is therefore safe without
 * ordinal narrowing.
 *
 * <p>Trade-off: bees start idle wander paths less often (fewer path starts and random-pos
 * probes). Once a wander does start, path length, hover radius, hive-homing threshold, and
 * all pollinate / hive / anger / sting goals are unchanged. Combat urgency is not touched.
 *
 * <p>Unit: {@code perf.bee_wander_chance} (gated by mixin plugin / FeatureUnits).
 *
 * @see BeePollinateCooldownMixin sibling nested Bee goal {@code @ModifyConstant} pattern
 * @see BeeHiveLocateMixin sibling nested Bee goal {@code @ModifyConstant} pattern
 * @see EndermanTakeIntervalMixin sibling canUse nextInt-interval stretch pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeeWanderGoal")
public abstract class BeeWanderChanceMixin {

    /**
     * Stretches the idle-wander random gate (vanilla {@code nextInt(10)} → {@code nextInt(15)}).
     *
     * @param original vanilla constant (always 10 at the matched site)
     * @return stretched nextInt bound for wander canUse chance
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 10))
    private int omnifix$rarerWanderStart(int original) {
        return 15;
    }
}
