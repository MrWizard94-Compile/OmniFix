package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while {@code Bee$BeeGoToKnownFlowerGoal} is active, every goal tick increments
 * {@code travellingTicks} and, once {@code travellingTicks > adjustedTickDelay(600)}, clears
 * {@code savedFlowerPos} so the bee abandons the known flower and restarts flower / pollinate
 * selection. Dense apiaries with many bees pathing toward remembered flowers therefore churn
 * navigation (stop / reset multiplier / re-path) and goal re-selection on a ~30s fail cadence
 * when flowers are obstructed, unloaded, or temporarily unreachable — without improving combat
 * urgency or successful pollination once a path is found.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code tick} only rewrites the sole {@code int}
 * literal {@code 600} → {@code 800} ({@code adjustedTickDelay} travel-fail threshold, +33%).
 * Constant audit (MC 1.20.1 mapped {@code Bee$BeeGoToKnownFlowerGoal}):
 * <ul>
 *   <li>{@code tick}: sole {@code int 600} — {@code travellingTicks > adjustedTickDelay(600)}
 *       (or static {@code MAX_TRAVELLING_TICKS = 600} inlined at that call site only)</li>
 *   <li>{@code canBeeUse}/{@code canBeeContinueToUse}: flower validity / distance checks;
 *       no {@code int 600}</li>
 *   <li>{@code start}/{@code stop}: only zero {@code travellingTicks}; no timeout literal</li>
 *   <li>{@code wantsToGoToKnownFlower}: {@code ticksWithoutNectarSinceExitingHive > 2400} —
 *       different constant, untouched</li>
 *   <li>field init {@code nextInt(10)} — not in {@code tick}</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 600} in {@code tick} is therefore safe without
 * ordinal narrowing.
 *
 * <p>Intentionally untouched: {@code BeeGoToHiveGoal} travel timeout
 * (sibling unit {@code perf.bee_go_hive_timeout}), pollinate / hive-locate / wander / grow-crop
 * goals ({@link BeePollinateCooldownMixin}, {@link BeeHiveLocateMixin},
 * {@link BeeWanderChanceMixin}, {@link BeeGrowCropIntervalMixin}), pathfind / too-far drop
 * branches that still clear {@code savedFlowerPos} when navigation cannot progress, and anger /
 * sting combat goals.
 *
 * <p>Trade-off: bees stick longer to a known flower path before abandoning (600→800 ticks
 * before {@code adjustedTickDelay}, +33%). Failed or obstructed flower approaches re-path and
 * re-select goals less often under density; successful arrivals are unchanged. Combat urgency
 * is unchanged.
 *
 * <p>Unit: {@code perf.bee_go_flower_timeout} (gated by mixin plugin / FeatureUnits).
 *
 * @see BeeGrowCropIntervalMixin sibling nested Bee goal {@code @ModifyConstant} on {@code tick}
 * @see BeeHiveLocateMixin sibling nested Bee goal {@code @ModifyConstant} pattern
 * @see BeePollinateCooldownMixin sibling nested Bee goal {@code @ModifyConstant} pattern
 * @see TurtleLayEggDurationMixin sibling nested-goal {@code adjustedTickDelay} threshold stretch
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeeGoToKnownFlowerGoal")
public abstract class BeeGoFlowerTimeoutMixin {

    /**
     * Stretches the known-flower travel-fail threshold (vanilla
     * {@code adjustedTickDelay(600)} → {@code adjustedTickDelay(800)}).
     *
     * @param original vanilla constant (always 600 at the matched site)
     * @return stretched travel-timeout base in ticks before abandoning saved flower
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 600))
    private int omnifix$longerKnownFlowerTravelTimeout(int original) {
        return 800;
    }
}
