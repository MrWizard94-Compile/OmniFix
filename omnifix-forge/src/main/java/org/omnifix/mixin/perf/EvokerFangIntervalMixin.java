package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Evoker$EvokerAttackSpellGoal#getCastingInterval} returns a hard-coded
 * {@code 100} ticks (5s at 20 TPS) between fang-spell casts. Under mansion / raid density
 * many concurrent evokers re-enter the fang spell on that cadence, driving repeated
 * {@code EvokerFangs} entity spawns, path/target work, and spell FX per cast cycle.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code getCastingInterval} rewrites the sole
 * {@code int} literal {@code 100} → {@code 150} (+50% interval). Audit (MC 1.20.1 mapped
 * {@code Evoker$EvokerAttackSpellGoal}):
 * <ul>
 *   <li>{@code getCastingInterval()}: body is {@code return 100;} — sole int constant in method</li>
 *   <li>{@code getCastingTime()}: returns {@code 40} on a <strong>different method</strong> —
 *       intentionally <strong>untouched</strong> so cast wind-up feel stays vanilla</li>
 *   <li>{@code EvokerSummonSpellGoal} / {@code EvokerWololoSpellGoal}: separate nested classes
 *       with their own {@code getCastingInterval} bodies — out of scope (own units)</li>
 * </ul>
 * No ordinal needed: a single {@code intValue = 100} site exists in {@code getCastingInterval}.
 * Matching is method-scoped, so {@code getCastingTime}'s {@code 40} cannot be rewritten.
 *
 * <p>Trade-off: fang spells fire less frequently (100→150 ticks between casts). Once a cast
 * starts, casting time ({@code 40}), fang pattern geometry, damage, and summon/wololo goals
 * are unchanged. Panic / flee-from-hurt / fire-urgency goals are unrelated and untouched.
 *
 * <p>Unit: {@code perf.evoker_fang_interval} (gated by mixin plugin / FeatureUnits).
 *
 * @see BlazeFireIntervalMixin sibling nested attack-goal interval {@code @ModifyConstant} pattern
 * @see GuardianAttackDurationMixin sibling getter-return stretch {@code @ModifyConstant} pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Evoker$EvokerAttackSpellGoal")
public abstract class EvokerFangIntervalMixin {

    /**
     * Stretches fang-spell casting interval (vanilla {@code 100} → {@code 150} ticks).
     * {@code getCastingTime} ({@code 40}) is a different method and is not rewritten.
     *
     * @param original vanilla constant (always 100 at the matched site)
     * @return stretched casting interval in ticks
     */
    @ModifyConstant(method = "getCastingInterval", constant = @Constant(intValue = 100))
    private int omnifix$longerFangCastInterval(int original) {
        return 150;
    }
}
