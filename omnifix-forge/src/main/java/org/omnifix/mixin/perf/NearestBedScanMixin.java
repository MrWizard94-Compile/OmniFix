package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.NearestBedSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: brain {@link NearestBedSensor#doTick} (baby mobs only) every sensor period
 * queries {@code PoiManager.findAllWithType(HOME, …, radius 48)} and pathfinds toward the
 * collected POIs. The batch predicate admits at most {@code BATCH_SIZE = 5} uncached bed
 * positions per pulse before refusing further candidates; when fewer than 5 were tried, the
 * long-lived batch cache is pruned. Dense baby-villager / baby-piglin farms re-run this
 * 48-block HOME POI scan and multi-POI path batch every sensor tick even when beds are
 * nearby or already known.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on {@code doTick}:
 * <ul>
 *   <li>{@code int 48 → 36} — {@code findAllWithType} HOME POI search radius</li>
 *   <li>{@code int 5 → 3} — both batch sites ({@code triedCount >= 5} admit cap and
 *       {@code triedCount < 5} prune gate)</li>
 * </ul>
 * Intentionally untouched: {@code CACHE_TIMEOUT} / {@code lastUpdate + 40L} (result cache
 * TTL) and {@code 20} (sensor scan rate via constructor / {@code nextInt(20)} jitter —
 * already stretched by {@link SensorScanRateMixin}).
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code doTick}):
 * <ul>
 *   <li>{@code int 48} — sole site: {@code findAllWithType(…, 48, Occupancy.ANY)}</li>
 *   <li>{@code int 5} — exactly two sites: {@code ++triedCount >= 5} and
 *       {@code triedCount < 5}</li>
 *   <li>{@code long 40L} — cache put expiry; not matched by {@code intValue} injectors</li>
 *   <li>{@code int 20} — random lastUpdate jitter only; not targeted</li>
 *   <li>{@code int 0} — {@code triedCount = 0} reset; not targeted</li>
 * </ul>
 * Class-level {@code CACHE_TIMEOUT}/{@code BATCH_SIZE}/{@code RATE} static finals are
 * inlined into the method body as the literals above. {@code ModifyConstant} on
 * {@code intValue = 48} and {@code intValue = 5} is therefore safe without ordinal
 * narrowing and does not alter cache TTL or scan cadence.
 *
 * <p>Trade-off: baby bed search uses a slightly shorter HOME POI radius (48→36, volume
 * factor {@code (36/48)³ ≈ 42%} of vanilla sphere) and fewer POI path attempts per pulse
 * (batch 5→3). Beds 36–48 blocks away are ignored until the baby moves closer; finding a
 * reachable bed among many candidates may take more sensor pulses. Cache TTL and sensor
 * period remain vanilla (period already handled by {@link SensorScanRateMixin}).
 *
 * <p>Unit: {@code perf.nearest_bed_scan} (gated by mixin plugin / FeatureUnits).
 *
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 * @see SecondaryPoiRadiusMixin sibling POI-radius {@code @ModifyConstant} pattern
 * @see NearestItemSensorRangeMixin sibling sensor-range shrink pattern
 */
@Mixin(NearestBedSensor.class)
public abstract class NearestBedScanMixin {

    /**
     * Rewrite the sole {@code 48} in {@code doTick} ({@code findAllWithType} HOME radius)
     * to {@code 36}.
     */
    @ModifyConstant(method = "doTick", constant = @Constant(intValue = 48))
    private int omnifix$shrinkBedPoiRadius(int original) {
        return 36;
    }

    /**
     * Rewrite every {@code 5} in {@code doTick} (batch admit cap {@code triedCount >= 5}
     * and prune gate {@code triedCount < 5}) to {@code 3}.
     */
    @ModifyConstant(method = "doTick", constant = @Constant(intValue = 5))
    private int omnifix$shrinkBedBatchSize(int original) {
        return 3;
    }
}
