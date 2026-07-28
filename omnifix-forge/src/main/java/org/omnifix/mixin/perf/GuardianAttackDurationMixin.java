package org.omnifix.mixin.perf;

import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link Guardian#getAttackDuration()} returns a hard-coded {@code 80} ticks
 * (4s at 20 TPS) for the laser charge / beam completion window. {@code GuardianAttackGoal}
 * and client attack progress scale off this value; under guardian density (ocean monuments,
 * farms, conduit fights) many concurrent beams complete and re-arm on that cadence, driving
 * repeated LOS / damage / sound / particle work per successful beam.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code getAttackDuration} rewrites the sole {@code int}
 * literal {@code 80} → {@code 100} (+25% duration). Audit (MC 1.20.1 mapped {@link Guardian}):
 * <ul>
 *   <li>{@code getAttackDuration()}: body is {@code return 80;} — sole int constant in method</li>
 *   <li>Elder guardian: {@code ElderGuardian#getAttackDuration()} overrides with {@code return 60;}
 *       on a <strong>different class</strong> — out of scope; this mixin does not target it</li>
 *   <li>Attack goal / client progress read the virtual method; ordinary guardians only hit this
 *       body and pick up the stretched constant</li>
 * </ul>
 * No ordinal needed: a single {@code intValue = 80} site exists in {@code getAttackDuration}.
 *
 * <p>Trade-off: guardian laser takes longer to finish (+25%, 80→100 ticks). Fewer complete beams
 * under density; combat still works. Elder guardians keep vanilla 60 via their override.
 * Panic / flee-from-hurt / fire-urgency goals are unrelated and untouched.
 *
 * <p>Unit: {@code perf.guardian_attack_duration} (gated by mixin plugin / FeatureUnits).
 *
 * @see GhastFireballChargeMixin sibling attack charge stretch {@code @ModifyConstant} pattern
 * @see RangedCrossbowDelayMixin sibling fire-cadence stretch pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(Guardian.class)
public abstract class GuardianAttackDurationMixin {

    /**
     * Stretches ordinary guardian laser attack duration (vanilla {@code 80} → {@code 100} ticks).
     * Elder guardian override ({@code 60}) is a separate method body and is not rewritten.
     *
     * @param original vanilla constant (always 80 at the matched site)
     * @return stretched attack duration in ticks
     */
    @ModifyConstant(method = "getAttackDuration", constant = @Constant(intValue = 80))
    private int omnifix$longerGuardianAttack(int original) {
        return 100;
    }
}
