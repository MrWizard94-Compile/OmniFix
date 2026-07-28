package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Vex$VexChargeAttackGoal#canUse} rolls
 * {@code nextInt(reducedTickDelay(7)) == 0} whenever the vex has a target and is not already
 * pathing. Idle / circling vexes therefore re-evaluate charge-start on a tight 1/7 (difficulty-
 * adjusted) cadence every goal-selector pass; packs of vexes from raids or spawners burn AI
 * time on random charge gates that usually fail the roll before any distance check.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code canUse} only rewrites the sole {@code int}
 * literal {@code 7} → {@code 10} (nextInt gate via {@code reducedTickDelay}: ~1/7 → ~1/10).
 * Audit (MC 1.20.1 {@code Vex$VexChargeAttackGoal}):
 * <ul>
 *   <li>{@code canUse}: sole {@code int 7} — {@code nextInt(reducedTickDelay(7)) == 0}</li>
 *   <li>{@code canUse}: {@code 4.0D} distance-squared gate — double, out of scope</li>
 *   <li>{@code canContinueToUse}/{@code start}/{@code stop}/{@code tick}: charge flight /
 *       isCharging flags — no {@code int 7} literals</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 7} in {@code canUse} is therefore safe without
 * ordinal narrowing.
 *
 * <p>Trade-off: vex charge attacks start slightly less often (fewer charge-start rolls and
 * subsequent distance checks). Once a charge does start, flight speed, isCharging flags,
 * target lock, and all other goals are unchanged. Panic / flee-from-hurt / fire-urgency goals
 * are unrelated and untouched.
 *
 * <p>Unit: {@code perf.vex_charge_chance} (gated by mixin plugin / FeatureUnits).
 *
 * @see BeeWanderChanceMixin sibling canUse nextInt-chance {@code @ModifyConstant} pattern
 * @see EndermanTakeIntervalMixin sibling nested-goal canUse {@code reducedTickDelay} stretch
 * @see GhastFireballChargeMixin sibling nested monster-goal charge-cadence pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Vex$VexChargeAttackGoal")
public abstract class VexChargeChanceMixin {

    /**
     * Stretches the charge-start random gate base (vanilla {@code reducedTickDelay(7)} →
     * {@code reducedTickDelay(10)}).
     *
     * @param original vanilla constant (always 7 at the matched site)
     * @return stretched nextInt bound base for charge canUse chance
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 7))
    private int omnifix$rarerChargeStart(int original) {
        return 10;
    }
}
