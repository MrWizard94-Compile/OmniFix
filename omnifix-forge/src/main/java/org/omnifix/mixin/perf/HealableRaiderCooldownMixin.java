package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.target.NearestHealableRaiderTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: when {@link NearestHealableRaiderTargetGoal#start} runs (witch / raider heal-target
 * acquired), vanilla sets {@code this.cooldown = reducedTickDelay(200)}. After that, {@code canUse}
 * refuses retarget until the cooldown drains (and a random coin-flip passes). Dense raids keep many
 * witches cycling this goal; the post-start cooldown is the gate that limits repeated
 * {@code findTarget} entity scans.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code start} rewrites the sole {@code int} literal
 * {@code 200} → {@code 300} (+50%). Audit (1.20.1 {@code NearestHealableRaiderTargetGoal}):
 * {@code start} contains only that literal and {@code super.start()}; the unused field
 * {@code DEFAULT_COOLDOWN = 200} is not loaded in {@code start}. Constructor interval {@code 500}
 * and {@code canUse} logic are out of scope.
 *
 * <p>Trade-off: witches / raider heal-target acquisition is less frequent after a successful start
 * (cooldown ~10s → ~15s at 20 TPS before {@code reducedTickDelay}). Active healing of the current
 * target is unchanged; panic / flee-from-hurt / on-fire urgency goals are unrelated and untouched.
 *
 * <p>Unit: {@code perf.healable_raider_cooldown}
 *
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 * @see MeleeCanUseCooldownMixin sibling post-gate cooldown stretch
 */
@Mixin(NearestHealableRaiderTargetGoal.class)
public abstract class HealableRaiderCooldownMixin {

    /**
     * Stretches post-start heal-target cooldown base (vanilla 200 → 300 ticks before
     * {@code reducedTickDelay}).
     *
     * @param original vanilla constant (always 200 at the matched site)
     * @return stretched cooldown base passed to {@code reducedTickDelay}
     */
    @ModifyConstant(method = "start", constant = @Constant(intValue = 200))
    private int omnifix$longerHealableCooldown(int original) {
        return 300;
    }
}
