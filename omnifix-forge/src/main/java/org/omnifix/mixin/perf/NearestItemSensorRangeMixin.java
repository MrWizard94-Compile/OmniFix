package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.NearestItemSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: brain {@link NearestItemSensor#doTick} every sensor period builds a wanted-item
 * candidate list via {@code getEntitiesOfClass(ItemEntity, inflate(32, 16, 32))} then filters
 * with {@code closerThan(..., 32.0)}. Each scan walks a 64×32×64 block AABB and may stream-sort
 * every nearby item entity. Dense farms of piglins, allays, villagers, and similar brain mobs
 * re-run this wide item scan on every sensor tick even when wanted loot is far outside practical
 * path range.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code doTick} rewrites every {@code double} literal
 * {@code 32.0D} → {@code 24.0D}. That hits both horizontal inflate arms and the
 * {@code closerThan} max distance in one injector. The vertical inflate {@code 16.0D} is a
 * different constant and is intentionally left alone so Y span stays vanilla.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code doTick}): the method body contains only three
 * {@code 32.0} doubles — {@code inflate} X, {@code inflate} Z, and {@code closerThan} — all
 * range-related. The only other double is {@code 16.0} (Y inflate). Class-level
 * {@code XZ_RANGE}/{@code Y_RANGE}/{@code MAX_DISTANCE_TO_WANTED_ITEM} are not referenced by
 * the mapped {@code doTick} body (hardcoded doubles), so they are out of scope for this
 * injector. {@code ModifyConstant} on {@code doubleValue = 32.0D} is therefore safe without
 * ordinal narrowing and does not alter Y.
 *
 * <p>Trade-off: brain mobs notice wanted items only within 24 blocks instead of 32 (AABB
 * half-extent XZ 24, max distance 24). Items between 24–32 blocks are ignored until the mob
 * moves closer; Y half-extent remains 16. Scan volume drops by
 * {@code (24/32)^2 ≈ 56%} in XZ area (same Y), cutting item-entity iteration cost on large
 * item dumps and brain-mob farms.
 *
 * <p>Unit: {@code perf.nearest_item_sensor_range} (gated by mixin plugin / FeatureUnits).
 *
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(NearestItemSensor.class)
public abstract class NearestItemSensorRangeMixin {

    /**
     * Rewrite every {@code 32.0D} in {@code doTick} (inflate X/Z + closerThan) to {@code 24.0D}.
     * Y inflate {@code 16.0D} is not matched.
     */
    @ModifyConstant(method = "doTick", constant = @Constant(doubleValue = 32.0D))
    private double omnifix$shrinkItemScanRange(double original) {
        return 24.0D;
    }
}
