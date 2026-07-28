package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while {@code Bee$BeeGoToHiveGoal} is active, each {@code tick} increments
 * {@code travellingTicks} and, when {@code travellingTicks > adjustedTickDelay(600)},
 * calls {@code dropAndBlacklistHive()}. Unreachable or far hives therefore hit the fail
 * path after ~30s (difficulty-adjusted), blacklist the hive, null {@code hivePos}, and set
 * {@code remainingCooldownBeforeLocatingNewHive = 200}. Dense apiaries with obstructed or
 * contested hives then re-enter locate + pathfinding thrash: drop → cooldown → locate POI
 * → go-to-hive → timeout → blacklist again. Rapid blacklist churn dominates AI / path time
 * without improving hive entry success.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code tick} only rewrites the sole {@code int}
 * literal {@code 600} → {@code 800} ({@code adjustedTickDelay} travel-fail threshold, +33%).
 * Intentionally untouched: path restarts when navigation is idle, {@code closerThan(..., 16)}
 * direct-path gate, stuck-path drop ({@code ticksStuck > 60}), {@code dropHive} locate
 * cooldown ({@code 200}), blacklist capacity ({@code 3}), {@code canBeeUse}/
 * {@code canBeeContinueToUse} hive-want gates, anger / sting combat goals, pollinate /
 * flower / locate / wander goals, and successful hive entry once the bee arrives.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code Bee$BeeGoToHiveGoal#tick}):
 * <ul>
 *   <li>{@code travellingTicks > adjustedTickDelay(600)} fail threshold — <strong>only</strong>
 *       int {@code 600} in method (this injector; field {@code MAX_TRAVELLING_TICKS = 600}
 *       is a separate constant pool site, not rewritten by method-scoped {@code ModifyConstant})</li>
 *   <li>{@code closerThan(hivePos, 16)} near-hive gate — int/double {@code 16}, untouched</li>
 *   <li>{@code ticksStuck > 60} stuck-path drop — int {@code 60}, untouched</li>
 *   <li>no other int {@code 600} in {@code tick}</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 600} in {@code tick} is therefore safe without
 * ordinal narrowing.
 *
 * <p>Trade-off: bees keep trying to reach a hive longer before {@code dropAndBlacklistHive}
 * (600→800 ticks via {@code adjustedTickDelay}, +33%). Longer stick time means less frequent
 * drop/blacklist cycles and fewer immediate repath / locate restarts when a hive is merely
 * slow to reach (far path, temporary obstruction). Truly unreachable hives still blacklists,
 * only later. Combat urgency, successful go-to-hive pathing quality, and enter-hive behavior
 * are unchanged.
 *
 * <p>Unit: {@code perf.bee_go_hive_timeout} (gated by mixin plugin / FeatureUnits).
 *
 * @see BeeHiveLocateMixin sibling nested Bee hive-locate POI / cooldown {@code @ModifyConstant}
 * @see BeePollinateCooldownMixin sibling nested Bee flower-search cooldown pattern
 * @see BeeWanderChanceMixin sibling nested Bee goal {@code @ModifyConstant} pattern
 * @see TurtleLayEggDurationMixin sibling nested-goal {@code adjustedTickDelay} threshold stretch
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeeGoToHiveGoal")
public abstract class BeeGoHiveTimeoutMixin {

    /**
     * Stretches the go-to-hive travel fail threshold (vanilla
     * {@code adjustedTickDelay(600)} → {@code adjustedTickDelay(800)}).
     *
     * @param original vanilla constant (always 600 at the matched site)
     * @return stretched travellingTicks fail threshold base in ticks
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 600))
    private int omnifix$longerGoToHiveTravelTimeout(int original) {
        return 800;
    }
}
