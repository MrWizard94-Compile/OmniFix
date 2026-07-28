package org.omnifix.mixin.perf;

import net.minecraft.world.entity.monster.ElderGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link ElderGuardian#getAttackDuration()} overrides the guardian laser charge
 * window with a hard-coded {@code 60} ticks (3s at 20 TPS). {@code GuardianAttackGoal} and
 * client attack progress scale off this virtual method; elder guardians in ocean monuments /
 * farms / raids therefore complete beams on a tighter cadence than ordinary guardians
 * (vanilla 80), driving denser LOS / damage / sound / particle work per successful beam under
 * multi-elder density.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code getAttackDuration} rewrites the sole {@code int}
 * literal {@code 60} → {@code 80} (+33% duration). Audit (MC 1.20.1 mapped {@link ElderGuardian}):
 * <ul>
 *   <li>{@code getAttackDuration()}: body is {@code return 60;} — sole int constant in method</li>
 *   <li>Ordinary guardian: {@code Guardian#getAttackDuration()} returns {@code 80} on a
 *       <strong>different class</strong> — out of scope; see {@link GuardianAttackDurationMixin}</li>
 *   <li>Attack goal / client progress read the virtual method; elders only hit this body and
 *       pick up the stretched constant</li>
 * </ul>
 * No ordinal needed: a single {@code intValue = 60} site exists in {@code getAttackDuration}.
 *
 * <p>Trade-off: elder guardian laser takes longer to finish (+33%, 60→80 ticks; slower beams).
 * Fewer complete beams under monument density; combat still works. Ordinary guardians are
 * handled by {@link GuardianAttackDurationMixin}. Panic / flee-from-hurt / fire-urgency goals
 * are unrelated and untouched.
 *
 * <p>Unit: {@code perf.elder_guardian_attack_duration} (gated by mixin plugin / FeatureUnits).
 *
 * @see GuardianAttackDurationMixin sibling ordinary-guardian duration stretch
 * @see GhastFireballChargeMixin sibling attack charge stretch {@code @ModifyConstant} pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(ElderGuardian.class)
public abstract class ElderGuardianAttackMixin {

    /**
     * Stretches elder guardian laser attack duration (vanilla {@code 60} → {@code 80} ticks).
     * Ordinary guardian body ({@code 80}) is a separate method and is not rewritten here.
     *
     * @param original vanilla constant (always 60 at the matched site)
     * @return stretched attack duration in ticks
     */
    @ModifyConstant(method = "getAttackDuration", constant = @Constant(intValue = 60))
    private int omnifix$longerElderGuardianAttack(int original) {
        return 80;
    }
}
