package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: when a bee has no hive and wants to enter one, {@code Bee$BeeLocateHiveGoal#start}
 * immediately runs {@code findNearbyHivesWithSpace}, which queries
 * {@code PoiManager.getInRange(BEE_HOME, pos, 20, Occupancy.ANY)} and sorts every in-range hive
 * POI by distance. Dense apiaries re-run this 20-block POI walk every time the locate goal fires;
 * after each attempt vanilla sets {@code remainingCooldownBeforeLocatingNewHive = 200} so swarms
 * re-scan on a short cadence even when no free hive exists nearby.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on the nested goal only:
 * <ul>
 *   <li>{@code findNearbyHivesWithSpace}: sole {@code int 20 → 15} (BEE_HOME {@code getInRange} radius)</li>
 *   <li>{@code start}: sole {@code int 200 → 300} (post-locate cooldown assignment)</li>
 * </ul>
 * Intentionally untouched: {@code canBeeUse}/{@code canUse} cooldown gate (must stay hot so a
 * zeroed cooldown still starts locate immediately), {@code BeeGoToHiveGoal} pathing, anger / sting
 * combat goals, pollinate / flower cooldowns ({@link BeePollinateCooldownMixin}), and hive block
 * entity tick logic.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code Bee$BeeLocateHiveGoal}):
 * <ul>
 *   <li>{@code findNearbyHivesWithSpace}: sole {@code int 20} — {@code getInRange(..., 20, ANY)}</li>
 *   <li>{@code start}: sole {@code int 200} — {@code remainingCooldownBeforeLocatingNewHive = 200}</li>
 *   <li>{@code canUse}: only compares cooldown to {@code 0}; no {@code 20}/{@code 200}</li>
 *   <li>{@code canContinueToUse}: returns {@code false}; no integer radius/cooldown literals</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 20} and {@code intValue = 200} is therefore safe
 * without ordinal narrowing.
 *
 * <p>Trade-off: hive POI search uses a slightly shorter radius (20→15, volume factor
 * {@code (15/20)³ ≈ 42%} of vanilla sphere) and waits longer between locate attempts (200→300
 * ticks, +50%). Hives 15–20 blocks away are ignored until the bee moves closer; failed hive
 * searches re-run less often. Combat urgency and successful go-to-hive pathing are unchanged.
 *
 * <p>Unit: {@code perf.bee_hive_locate} (gated by mixin plugin / FeatureUnits).
 *
 * @see BeePollinateCooldownMixin sibling nested Bee goal {@code @ModifyConstant} pattern
 * @see NearestBedScanMixin sibling POI-radius shrink pattern
 * @see SecondaryPoiRadiusMixin sibling POI-radius {@code @ModifyConstant} pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeeLocateHiveGoal")
public abstract class BeeHiveLocateMixin {

    /**
     * Rewrite the sole {@code 20} in {@code findNearbyHivesWithSpace}
     * ({@code PoiManager.getInRange} BEE_HOME radius) to {@code 15}.
     *
     * @param original vanilla constant (always 20 at the matched site)
     * @return reduced hive POI search radius
     */
    @ModifyConstant(method = "findNearbyHivesWithSpace", constant = @Constant(intValue = 20))
    private int omnifix$shrinkHivePoiRadius(int original) {
        return 15;
    }

    /**
     * Rewrite the sole {@code 200} in {@code start}
     * ({@code remainingCooldownBeforeLocatingNewHive = 200}) to {@code 300}.
     *
     * @param original vanilla constant (always 200 at the matched site)
     * @return stretched cooldown between hive locate attempts
     */
    @ModifyConstant(method = "start", constant = @Constant(intValue = 200))
    private int omnifix$longerHiveLocateCooldown(int original) {
        return 300;
    }
}
