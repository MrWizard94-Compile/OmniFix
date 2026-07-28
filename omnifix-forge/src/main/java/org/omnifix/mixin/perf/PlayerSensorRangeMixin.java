package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.PlayerSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: brain {@link PlayerSensor#doTick} every sensor period streams
 * {@code ServerLevel#players()}, filters spectators, then keeps only players within
 * {@code closerThan(..., 16.0D)}, sorts by distance squared, and writes
 * {@code NEAREST_PLAYERS} / {@code NEAREST_VISIBLE_PLAYER} /
 * {@code NEAREST_VISIBLE_ATTACKABLE_PLAYER}. Dense farms of villagers, piglins, axolotls,
 * and other brain mobs re-run this full player-list filter on every sensor tick even when
 * no player is within practical interaction range.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code doTick} rewrites the sole {@code double}
 * literal {@code 16.0D} → {@code 12.0D} (the {@code closerThan} max distance).
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code doTick}): the method body contains exactly one
 * {@code 16.0} double — the {@code closerThan(player, 16.0)} filter. No other double
 * literals appear in {@code doTick}. Class-level / base-class {@link Sensor#TARGETING_RANGE}
 * ({@code 16}) and the {@link Sensor} static {@code TargetingConditions} ranges
 * ({@code .range(16.0)}) live on {@link Sensor}, not in this method body, and are out of
 * scope for this injector (line-of-sight / attackability checks still use those separate
 * conditions). {@code ModifyConstant} on {@code doubleValue = 16.0D} is therefore safe
 * without ordinal narrowing and does not alter {@link Sensor} targeting ranges.
 *
 * <p>Trade-off: brain entities notice players within 12 blocks instead of 16 for the
 * nearest-player memory modules. Players between 12–16 blocks are omitted from
 * {@code NEAREST_PLAYERS} (and thus from visible/attackable derivatives) until the mob
 * moves closer. Scan work on {@code level.players()} streams drops earlier via the tighter
 * distance filter (O(players) still, but fewer survivors for sort/visibility follow-ups).
 * Combat urgency via hurt-revenge goals is untouched; only brain player sensing radius
 * shrinks mildly.
 *
 * <p>Unit: {@code perf.player_sensor_range} (gated by mixin plugin / FeatureUnits).
 *
 * @see NearestItemSensorRangeMixin sibling sensor range shrink pattern
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 */
@Mixin(PlayerSensor.class)
public abstract class PlayerSensorRangeMixin {

    /**
     * Rewrite the sole {@code 16.0D} in {@code doTick} ({@code closerThan} player range)
     * to {@code 12.0D}. {@link Sensor} {@code TARGETING_RANGE} / condition ranges are not
     * in this method.
     */
    @ModifyConstant(method = "doTick", constant = @Constant(doubleValue = 16.0D))
    private double omnifix$shrinkPlayerScanRange(double original) {
        return 12.0D;
    }
}
