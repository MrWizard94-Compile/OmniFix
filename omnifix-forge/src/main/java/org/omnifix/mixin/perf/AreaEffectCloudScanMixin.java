package org.omnifix.mixin.perf;

import net.minecraft.world.entity.AreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link AreaEffectCloud#tick} server branch gates victim map cleanup,
 * {@code getEntitiesOfClass(LivingEntity)} AABB scan, and potion re-application on
 * {@code this.tickCount % 5 == 0} (vanilla {@code TIME_BETWEEN_APPLICATIONS}). Dense
 * lingering potions / dragon breath clouds re-scan every 5 ticks even when empty.
 *
 * <p>Stretching the modulo period from 5 → 8 cuts scan + apply frequency by ~37.5%
 * (~60% longer interval). Duration countdown, wait-time transition, radius-per-tick
 * shrink, and discard logic remain on the vanilla path every tick.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code tick}): the only {@code int 5} in the method
 * body is the scan-period modulo. Client particle counts use {@code 2}; effect duration
 * scale uses {@code / 4}; radius discard uses {@code 0.5F}. {@code ModifyConstant} is
 * therefore safe without ordinal narrowing.
 *
 * <p>Trade-off: potion re-application / victim scan every 8 ticks instead of 5
 * (~150 ms → ~400 ms cadence at 20 TPS). Instant effects and lingering re-apply feel
 * slightly less snappy under dense entity piles; cloud lifetime and radius unchanged.
 *
 * <p>Unit: {@code perf.aec_scan_period}
 *
 * @see XpOrbScanPeriodMixin sibling period-stretch pattern
 */
@Mixin(AreaEffectCloud.class)
public abstract class AreaEffectCloudScanMixin {

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 5))
    private int omnifix$longerScanPeriod(int original) {
        return 8;
    }
}
