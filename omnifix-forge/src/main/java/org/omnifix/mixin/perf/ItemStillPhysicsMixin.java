package org.omnifix.mixin.perf;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link ItemEntity#tick} only full-runs {@code move} + friction + bounce for
 * still-on-ground entities when {@code (tickCount + id) % 4 == 0}. Airborne and sliding
 * items always take the full path. Dense resting item piles therefore still pay a full
 * physics/impulse update every 4 ticks per entity even when horizontal motion is below
 * {@code 1.0E-5F}.
 *
 * <p>Stretching the still-on-ground interval from {@code 4} → {@code 6} cuts resting
 * physics updates by ~33% while leaving the always-on airborne / sliding branch unchanged
 * (those conditions short-circuit the modulo).
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code tick}): the only {@code int 4} literal in the
 * method body is the still-physics modulo. Other ints are:
 * <ul>
 *   <li>{@code 32767} — pickup-delay freeze sentinel</li>
 *   <li>{@code -32768} — age freeze sentinel</li>
 *   <li>{@code 2} — moving merge period ({@code flag ? 2 : 40})</li>
 *   <li>{@code 40} — stationary merge period (owned by {@link ItemMergePeriodMixin})</li>
 * </ul>
 * Floats/doubles ({@code 1.0E-5F}, friction {@code 0.98F}, gravity {@code -0.04D}, bounce
 * {@code 0.5D}, impulse threshold {@code 0.01D}, etc.) are not matched by
 * {@code @Constant(intValue = 4)}. {@code ModifyConstant} is therefore safe without
 * ordinal narrowing and does not touch merge cadence.
 *
 * <p>Trade-off: still item piles run physics / friction / bounce / impulse checks every
 * 6 ticks instead of 4 (~200 ms → ~300 ms cadence at 20 TPS). Moving and airborne items
 * keep vanilla every-tick full updates. Resting piles may feel slightly less snappy when
 * something starts pushing them again; merge scan periods are unchanged by this unit.
 *
 * <p>Unit: {@code perf.item_still_physics_period} (gated by mixin plugin / FeatureUnits).
 *
 * @see ItemMergePeriodMixin stationary merge period 40→60 (complementary, different constant)
 * @see ItemEntityMergeCacheMixin same-tick empty merge-scan cache (complementary)
 * @see XpOrbScanPeriodMixin sibling period-stretch pattern
 */
@Mixin(ItemEntity.class)
public abstract class ItemStillPhysicsMixin {

    /**
     * Rewrite only the still-on-ground physics interval ({@code 4} → {@code 6}).
     * Merge periods {@code 2}/{@code 40} are different constants and are not matched.
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 4))
    private int omnifix$longerStillPhysicsPeriod(int original) {
        return 6;
    }
}
