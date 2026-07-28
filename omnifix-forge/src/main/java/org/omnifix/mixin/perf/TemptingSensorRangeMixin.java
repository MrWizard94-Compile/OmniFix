package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: brain {@link TemptingSensor#doTick} every sensor period resolves the nearest
 * tempting player via {@code TEMPT_TARGETING} and {@code closerThan(..., 10.0D)}. Static
 * {@code TEMPT_TARGETING} is built in {@code <clinit>} with {@code TargetingConditions.range(10.0D)}
 * (line-of-sight / non-spectator filters). Farms of brain animals (sheep, cows, pigs, chickens,
 * goats, etc.) re-evaluate temptation over a 10-block sphere on every sensor period even when
 * the player is outside practical follow / path range, paying player-list + visibility work.
 *
 * <p>Policy: one {@code @ModifyConstant} on both {@code doTick} and {@code <clinit>} rewrites
 * every {@code double} literal {@code 10.0D} → {@code 8.0D}. That hits the static targeting
 * range and the runtime {@code closerThan} max distance so the two stay consistent.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code TemptingSensor}):
 * <ul>
 *   <li>{@code <clinit>}: sole {@code 10.0} double is {@code TargetingConditions.forNonCombat().range(10.0D)}
 *       assigned to {@code TEMPT_TARGETING}.</li>
 *   <li>{@code doTick}: sole {@code 10.0} double is {@code entity.closerThan(player, 10.0D)}
 *       after the targeting filter.</li>
 * </ul>
 * No other {@code 10.0D} literals exist in either method body. Handler is {@code static} so it
 * may inject into {@code <clinit>} and instance {@code doTick}. {@code ModifyConstant} on
 * {@code doubleValue = 10.0D} is therefore safe without ordinal narrowing.
 *
 * <p>Trade-off: animals notice temptation items / tempting players within 8 blocks instead of
 * 10. Players holding food between 8–10 blocks no longer set {@code TEMPTING_PLAYER} until
 * closer; follow-tempt goals engage slightly later. Scan volume drops by
 * {@code (8/10)^3 ≈ 51%} of the vanilla sphere (or {@code (8/10)^2 ≈ 64%} of planar area),
 * cutting player filter / LOS follow-ups on dense animal pens. Combat urgency (hurt revenge)
 * is untouched; only temptation sensing radius shrinks mildly.
 *
 * <p>Unit: {@code perf.tempting_sensor_range} (gated by mixin plugin / FeatureUnits).
 *
 * @see PlayerSensorRangeMixin sibling brain player-range shrink
 * @see NearestItemSensorRangeMixin sibling sensor range shrink pattern
 * @see SensorScanRateMixin complementary sensor period stretch (all sensors)
 */
@Mixin(TemptingSensor.class)
public abstract class TemptingSensorRangeMixin {

    /**
     * Rewrite every {@code 10.0D} in {@code doTick} ({@code closerThan}) and {@code <clinit>}
     * ({@code TEMPT_TARGETING.range}) to {@code 8.0D} so runtime and static ranges match.
     */
    @ModifyConstant(
            method = {"doTick", "<clinit>"},
            constant = @Constant(doubleValue = 10.0D))
    private static double omnifix$shrinkTemptRange(double original) {
        return 8.0D;
    }
}
