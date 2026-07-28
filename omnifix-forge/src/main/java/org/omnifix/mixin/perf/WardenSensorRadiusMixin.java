package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.WardenEntitySensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link WardenEntitySensor} overrides {@link WardenEntitySensor#radiusXZ()} and
 * {@link WardenEntitySensor#radiusY()} to return {@code 24}, expanding the inherited
 * {@code NearestLivingEntitySensor#doTick} living-entity AABB to half-extent
 * {@code 24×24×24} (inflate XZ/Y). Every sensor period the warden re-scans that volume for
 * {@code NEAREST_LIVING_ENTITIES} / {@code NEAREST_VISIBLE_LIVING_ENTITIES}, then derives
 * {@code NEAREST_ATTACKABLE} from the living list. Dense deep-dark / farm scenarios pay a
 * large entity-iteration + sort cost for candidates far outside useful combat approach range.
 *
 * <p>Policy: {@code @ModifyConstant} on both {@code radiusXZ} and {@code radiusY} rewrites the
 * single {@code int} literal {@code 24} → {@code 20} in each method. Parent {@code doTick}
 * calls these hooks for inflate arms, so both horizontal and vertical half-extents shrink
 * together. The base {@code NearestLivingEntitySensor} 16→12 unit does not touch this subclass
 * (overrides keep their own constants).
 *
 * <p>Constant audit (MC 1.20.1 mapped):
 * <ul>
 *   <li>{@code radiusXZ()} — body is solely {@code return 24;} (one {@code int} 24)</li>
 *   <li>{@code radiusY()} — body is solely {@code return 24;} (one {@code int} 24)</li>
 *   <li>{@code doTick} delegates to {@code super.doTick} then filters
 *       {@code NEAREST_LIVING_ENTITIES} for {@code NEAREST_ATTACKABLE} — no hard-coded radius</li>
 *   <li>{@code getClosest} / {@code requires()} have no numeric range constants</li>
 * </ul>
 * Each injector therefore matches exactly one constant without ordinal narrowing.
 *
 * <p>Trade-off: warden living-entity perception sphere is slightly smaller — wardens see living
 * entities only within 20 blocks instead of 24 (AABB half-extent XZ and Y both 20). Entities
 * between 20–24 blocks are omitted from nearest-living / nearest-attackable memories until the
 * warden moves closer. Scan volume drops by {@code (20/24)^3 ≈ 57.9%} of the warden's vanilla
 * sensor volume (about 42% less entity iteration). Combat urgency (hurt revenge, sonic boom
 * goals, anger system) is not touched — only the living-entity sensor radius shrinks mildly.
 *
 * <p>Unit: {@code perf.warden_sensor_radius} (gated by mixin plugin / FeatureUnits).
 *
 * @see NearestLivingSensorRadiusMixin sibling base nearest-living radius shrink (16→12)
 * @see PlayerSensorRangeMixin sibling brain player-sensor range shrink
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 */
@Mixin(WardenEntitySensor.class)
public abstract class WardenSensorRadiusMixin {

    /**
     * Rewrite {@code radiusXZ} return literal {@code 24} → {@code 20}.
     */
    @ModifyConstant(method = "radiusXZ", constant = @Constant(intValue = 24))
    private int omnifix$shrinkWardenRadiusXZ(int original) {
        return 20;
    }

    /**
     * Rewrite {@code radiusY} return literal {@code 24} → {@code 20}.
     */
    @ModifyConstant(method = "radiusY", constant = @Constant(intValue = 24))
    private int omnifix$shrinkWardenRadiusY(int original) {
        return 20;
    }
}
