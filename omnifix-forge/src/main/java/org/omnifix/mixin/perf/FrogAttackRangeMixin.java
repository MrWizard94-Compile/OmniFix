package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.FrogAttackablesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: brain {@link FrogAttackablesSensor#isMatchingEntity} filters edible tongue
 * targets with {@code target.closerThan(attacker, 10.0D)} on every sensor evaluation after
 * hunting-cooldown / attackability / {@code Frog.canEat} / unreachable-tongue checks. Swamp
 * and mangrove frog farms re-evaluate this 10-block sphere over visible living candidates
 * even when prey sits outside practical tongue / path range, paying distance + follow-up
 * attack-memory work per frog per sensor period.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code isMatchingEntity} rewrites the sole
 * {@code double} literal {@code 10.0D} → {@code 8.0D} (the {@code closerThan} max distance).
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code FrogAttackablesSensor}):
 * <ul>
 *   <li>{@code isMatchingEntity}: sole {@code 10.0} double is
 *       {@code target.closerThan(attacker, 10.0D)} after cooldown / attackable / canEat /
 *       unreachable filters. No other double literals exist in the method body.</li>
 *   <li>Class field {@code TARGET_DETECTION_DISTANCE = 10.0F} is a {@code float} ConstantValue
 *       and is not referenced by the mapped {@code isMatchingEntity} body (hardcoded double),
 *       so it is out of scope for this injector.</li>
 *   <li>{@code isUnreachableAttackTarget} / {@code getMemory} contain no range doubles.</li>
 * </ul>
 * {@code ModifyConstant} on {@code doubleValue = 10.0D} is therefore safe without ordinal
 * narrowing and does not alter float field metadata.
 *
 * <p>Trade-off: frogs notice edible targets within 8 blocks instead of 10. Prey between
 * 8–10 blocks no longer sets {@code NEAREST_ATTACKABLE} until the frog moves closer; tongue
 * hunt engages slightly later. Scan volume drops by {@code (8/10)^3 ≈ 51%} of the vanilla
 * sphere (or {@code (8/10)^2 ≈ 64%} of planar area), cutting distance-filter survivors and
 * attack-memory write-ups on dense frog pens. Combat urgency (hurt revenge {@code canUse})
 * is untouched; only frog edible-target sensing radius shrinks mildly.
 *
 * <p>Unit: {@code perf.frog_attack_range} (gated by mixin plugin / FeatureUnits).
 *
 * @see TemptingSensorRangeMixin sibling 10→8 closerThan shrink pattern
 * @see PlayerSensorRangeMixin sibling brain sensor range shrink
 * @see NearestItemSensorRangeMixin sibling sensor range shrink pattern
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 */
@Mixin(FrogAttackablesSensor.class)
public abstract class FrogAttackRangeMixin {

    /**
     * Rewrite the sole {@code 10.0D} in {@code isMatchingEntity}
     * ({@code closerThan} edible-target range) to {@code 8.0D}.
     * Float field {@code TARGET_DETECTION_DISTANCE} is not in this method.
     */
    @ModifyConstant(method = "isMatchingEntity", constant = @Constant(doubleValue = 10.0D))
    private double omnifix$shrinkFrogAttackRange(double original) {
        return 8.0D;
    }
}
