package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link Allay#aiStep} gates passive regeneration with
 * {@code tickCount % 10 == 0} then {@code heal(1.0F)} on the server while alive. Every
 * allay therefore performs a full heal path every 10 ticks (~0.5 s at 20 TPS), even when
 * already at full health (vanilla still enters the heal call). Dense allay farms and
 * storage rooms amplify that idle tick work.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code aiStep} rewrites the sole {@code int}
 * literal {@code 10} → {@code 15} (+50%). Audit (MC 1.20.1 mapped {@code Allay#aiStep}):
 * <ul>
 *   <li>Passive heal gate: {@code tickCount % 10 == 0} — <strong>matched</strong>.</li>
 *   <li>Dance-stop gate: {@code tickCount % 20 == 0} — different constant; left alone so
 *       jukebox dance termination cadence stays vanilla.</li>
 *   <li>Client animation clamps use {@code 15.0F} (float), not {@code int 15}; no collision
 *       with the rewritten heal period return value at the injection site.</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 10} is therefore safe without ordinal
 * narrowing and cannot rewrite the dance {@code 20}.
 *
 * <p>Trade-off: allays passive-heal less often (every 15 ticks / ~0.75 s instead of every
 * 10 ticks / ~0.5 s). Effective regen rate drops from 2 HP/s to ~1.33 HP/s while damaged.
 * Dance, item-seek brain, duplication cooldown, and other allay behaviour are unchanged.
 *
 * <p>Unit: {@code perf.allay_heal_period} (gated by mixin plugin / FeatureUnits).
 *
 * @see XpOrbScanPeriodMixin sibling period-stretch pattern
 * @see ItemMergePeriodMixin sibling period-stretch with multi-constant audit
 * @see FollowOwnerRepathMixin sibling {@code 10 → 15} {@code @ModifyConstant} pattern
 */
@Mixin(Allay.class)
public abstract class AllayHealPeriodMixin {

    /**
     * Stretch passive heal period only ({@code 10} → {@code 15}).
     * Dance-stop period {@code 20} is a different constant and is not matched.
     *
     * @param original vanilla constant (always 10 at the matched heal gate)
     * @return stretched heal period modulus
     */
    @ModifyConstant(method = "aiStep", constant = @Constant(intValue = 10))
    private int omnifix$longerHealPeriod(int original) {
        return 15;
    }
}
