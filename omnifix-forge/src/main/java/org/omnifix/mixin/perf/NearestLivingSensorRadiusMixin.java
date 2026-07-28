package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: brain {@link NearestLivingEntitySensor#doTick} builds a nearest-living candidate
 * list via {@code getEntitiesOfClass(LivingEntity, inflate(radiusXZ, radiusY, radiusXZ))} then
 * sorts by distance every sensor period. Vanilla {@link NearestLivingEntitySensor#radiusXZ()} and
 * {@link NearestLivingEntitySensor#radiusY()} both return {@code 16}, producing a
 * {@code 32×32×32} half-extent AABB (64³ volume of entity iteration when dense). Every brain mob
 * using this base sensor (villagers, piglins, axolotls, frogs, wardens via subclass defaults,
 * etc.) re-scans that volume on the sensor cadence even when most candidates are outside useful
 * interaction range.
 *
 * <p>Policy: {@code @ModifyConstant} on both {@code radiusXZ} and {@code radiusY} rewrites the
 * single {@code int} literal {@code 16} → {@code 12} in each method. {@code doTick} calls these
 * hooks for inflate arms, so both horizontal and vertical half-extents shrink together. Methods
 * that override either radius in subclasses keep their own constants and are out of scope.
 *
 * <p>Constant audit (MC 1.20.1 mapped):
 * <ul>
 *   <li>{@code radiusXZ()} — body is solely {@code return 16;} (one {@code int} 16)</li>
 *   <li>{@code radiusY()} — body is solely {@code return 16;} (one {@code int} 16)</li>
 *   <li>{@code doTick} uses {@code (double)this.radiusXZ()} / {@code radiusY()} — no hard-coded
 *       16 of its own, so no injector there</li>
 *   <li>{@code requires()} has no numeric range constants</li>
 * </ul>
 * Each injector therefore matches exactly one constant without ordinal narrowing.
 *
 * <p>Trade-off: brain mobs see living entities only within 12 blocks instead of 16 (AABB
 * half-extent XZ and Y both 12). Entities between 12–16 blocks are omitted from
 * {@code NEAREST_LIVING_ENTITIES} / {@code NEAREST_VISIBLE_LIVING_ENTITIES} until the mob moves
 * closer. Scan volume drops by {@code (12/16)^3 = 42.2%} of vanilla, cutting entity-list
 * iteration and sort cost on dense brain-mob farms. Combat-urgency goals
 * ({@code HurtByTargetGoal}, melee canUse) are not touched.
 *
 * <p>Unit: {@code perf.nearest_living_sensor_radius} (gated by mixin plugin / FeatureUnits).
 *
 * @see NearestItemSensorRangeMixin sibling item-sensor range shrink
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 */
@Mixin(NearestLivingEntitySensor.class)
public abstract class NearestLivingSensorRadiusMixin {

    /**
     * Rewrite {@code radiusXZ} return literal {@code 16} → {@code 12}.
     */
    @ModifyConstant(method = "radiusXZ", constant = @Constant(intValue = 16))
    private int omnifix$shrinkNearestLivingRadiusXZ(int original) {
        return 12;
    }

    /**
     * Rewrite {@code radiusY} return literal {@code 16} → {@code 12}.
     */
    @ModifyConstant(method = "radiusY", constant = @Constant(intValue = 16))
    private int omnifix$shrinkNearestLivingRadiusY(int original) {
        return 12;
    }
}
