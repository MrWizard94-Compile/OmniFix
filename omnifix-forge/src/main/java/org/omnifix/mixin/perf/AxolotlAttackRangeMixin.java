package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: brain {@link AxolotlAttackablesSensor} filters hunt / always-hostile targets
 * through private {@code isClose}, which accepts candidates when
 * {@code target.distanceToSqr(attacker) <= 64.0D} (8-block sphere). Every sensor evaluation
 * of {@code isMatchingEntity} pays that wide distance gate before water / hostile-tag /
 * hunt-target / attackability checks. Dense aquatic farms of axolotls re-admit prey and
 * hostiles out to 8 blocks even when outside practical chase / combat path range, inflating
 * {@code NEAREST_ATTACKABLE} candidates and follow-up brain combat work.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code isClose} rewrites the sole {@code double}
 * literal {@code 64.0D} → {@code 36.0D} (distance squared for 6 blocks: {@code 6*6 = 36}).
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code AxolotlAttackablesSensor}):
 * <ul>
 *   <li>{@code isClose}: sole {@code 64.0} double is
 *       {@code pTarget.distanceToSqr(pAttacker) <= 64.0D}. No other doubles in the method.</li>
 *   <li>Class field {@code TARGET_DETECTION_DISTANCE = 8.0F} is a {@code float} ConstantValue
 *       and is not referenced by the mapped {@code isClose} body (hardcoded double), so it is
 *       out of scope for this injector.</li>
 *   <li>{@code isMatchingEntity} / {@code isHuntTarget} / {@code isHostileTarget} /
 *       {@code getMemory} contain no range doubles.</li>
 * </ul>
 * {@code ModifyConstant} on {@code doubleValue = 64.0D} is therefore safe without ordinal
 * narrowing and does not alter float field metadata.
 *
 * <p>Trade-off: axolotls hunt / notice hostiles within 6 blocks instead of 8. Prey and
 * always-hostile mobs between 6–8 blocks no longer set {@code NEAREST_ATTACKABLE} until the
 * axolotl moves closer; combat engage range shrinks mildly. Scan volume drops by
 * {@code (6/8)^3 = 42.2%} of the vanilla sphere (or {@code (6/8)^2 = 56.25%} of planar area),
 * cutting distance-filter survivors and attack-memory write-ups on dense axolotl pens.
 * Combat urgency (hurt revenge {@code canUse}) is untouched; only axolotl attackable sensing
 * radius shrinks mildly.
 *
 * <p>Unit: {@code perf.axolotl_attack_range} (gated by mixin plugin / FeatureUnits).
 *
 * @see FrogAttackRangeMixin sibling attackables-sensor range shrink (10→8 closerThan)
 * @see PlayerSensorRangeMixin sibling brain sensor range shrink
 * @see TemptingSensorRangeMixin sibling closerThan / range shrink pattern
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 */
@Mixin(AxolotlAttackablesSensor.class)
public abstract class AxolotlAttackRangeMixin {

    /**
     * Rewrite the sole {@code 64.0D} in {@code isClose}
     * ({@code distanceToSqr} max, 8² blocks) to {@code 36.0D} (6² blocks).
     * Float field {@code TARGET_DETECTION_DISTANCE} is not in this method.
     */
    @ModifyConstant(method = "isClose", constant = @Constant(doubleValue = 64.0D))
    private double omnifix$shrinkAxolotlAttackRange(double original) {
        return 36.0D;
    }
}
