package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.PiglinSpecificSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: brain {@link PiglinSpecificSensor} private static {@code findNearestRepellent}
 * every sensor pulse walks a volume via
 * {@code BlockPos.findClosestMatch(entity.blockPosition(), 8, 4, predicate)} looking for
 * {@code BlockTags.PIGLIN_REPELLENTS} (soul fire / soul campfire and related repellents).
 * Horizontal half-extent {@code 8} and vertical half-extent {@code 4} yield up to
 * {@code (2·8+1)² · (2·4+1) = 17² · 9 = 2601} block probes per call. Dense piglin farms
 * and bastion / nether hub spawns re-run this full volume every sensor period even when no
 * repellent is nearby, paying block-state + tag lookups over a large box.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on private static
 * {@code findNearestRepellent}:
 * <ul>
 *   <li>{@code int 8 → 6} — horizontal half-extent of {@code findClosestMatch}</li>
 *   <li>{@code int 4 → 3} — vertical half-extent of {@code findClosestMatch}</li>
 * </ul>
 * Probe volume becomes {@code (2·6+1)² · (2·3+1) = 13² · 7 = 1183} (~54.5% of vanilla).
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code findNearestRepellent}):
 * <ul>
 *   <li>{@code int 8} — sole site: {@code findClosestMatch(pos, 8, 4, …)} horizontal range</li>
 *   <li>{@code int 4} — sole site: same call, vertical range</li>
 * </ul>
 * No other {@code 8} or {@code 4} literals exist in the method body. Mixin targets the
 * private static method by name; handlers are {@code static} to match the target.
 * {@code ModifyConstant} on {@code intValue = 8} and {@code intValue = 4} is therefore
 * safe without ordinal narrowing. Intentionally untouched: {@code doTick} / memory writes,
 * {@code isValidRepellent} predicate (soul campfire lit check), sensor period (see
 * {@link SensorScanRateMixin}), and combat / hurt-revenge paths.
 *
 * <p>Trade-off: piglins detect soul fire / campfire repellents only within a smaller scan
 * (horizontal 8→6, vertical 4→3). Repellents 6–8 blocks away (or 3–4 vertically) no longer
 * populate {@code NEAREST_REPELLENT} until the piglin moves closer; flee-from-repellent
 * behavior engages slightly later at the edge of the old box. Combat urgency is untouched;
 * only repellent block search volume shrinks mildly.
 *
 * <p>Unit: {@code perf.piglin_repellent_range} (gated by mixin plugin / FeatureUnits).
 *
 * @see HoglinRepellentRangeMixin sibling hoglin repellent volume shrink (same 8/4 → 6/3)
 * @see SecondaryPoiRadiusMixin sibling block-search radius shrink
 * @see NearestBedScanMixin sibling sensor volume {@code @ModifyConstant} pattern
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 */
@Mixin(PiglinSpecificSensor.class)
public abstract class PiglinRepellentRangeMixin {

    /**
     * Rewrite the sole {@code 8} in static {@code findNearestRepellent}
     * ({@code BlockPos.findClosestMatch} horizontal half-extent) to {@code 6}.
     */
    @ModifyConstant(method = "findNearestRepellent", constant = @Constant(intValue = 8))
    private static int omnifix$shrinkRepellentHorizontal(int original) {
        return 6;
    }

    /**
     * Rewrite the sole {@code 4} in static {@code findNearestRepellent}
     * ({@code BlockPos.findClosestMatch} vertical half-extent) to {@code 3}.
     */
    @ModifyConstant(method = "findNearestRepellent", constant = @Constant(intValue = 4))
    private static int omnifix$shrinkRepellentVertical(int original) {
        return 3;
    }
}
