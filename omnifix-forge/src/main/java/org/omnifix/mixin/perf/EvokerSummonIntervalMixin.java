package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Evoker$EvokerSummonSpellGoal#getCastingInterval} returns a hard-coded
 * {@code 340} ticks (~17s at 20 TPS) between summon-vex cast cycles. Under mansion / raid
 * density many concurrent evokers re-enter the summon spell on that cadence, each cast
 * spawning up to three {@code Vex} entities plus spell FX / path work, inflating entity
 * count and subsequent vex AI load.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code getCastingInterval} rewrites the sole
 * {@code int} literal {@code 340} → {@code 450} (~+32% interval). Audit (MC 1.20.1 mapped
 * {@code Evoker$EvokerSummonSpellGoal}):
 * <ul>
 *   <li>{@code getCastingInterval()}: body is {@code return 340;} — sole int constant in method</li>
 *   <li>{@code getCastingTime()}: returns {@code 100} on a <strong>different method</strong> —
 *       intentionally <strong>untouched</strong> so cast wind-up feel stays vanilla</li>
 *   <li>{@code EvokerAttackSpellGoal} / {@code EvokerWololoSpellGoal}: separate nested classes
 *       with their own {@code getCastingInterval} bodies — out of scope (own units)</li>
 *   <li>{@code performSpellCasting}: vex spawn loop / {@code finalizeSpawn} — no {@code 340}
 *       literal; spawn count and geometry unchanged</li>
 * </ul>
 * No ordinal needed: a single {@code intValue = 340} site exists in {@code getCastingInterval}.
 * Matching is method-scoped, so {@code getCastingTime}'s {@code 100} cannot be rewritten.
 *
 * <p>Trade-off: vex summons fire less frequently (340→450 ticks between casts; ~17s → ~22.5s
 * at 20 TPS). Once a cast starts, casting time, vex count / spawn offsets, and fang/wololo
 * goals are unchanged. Panic / flee-from-hurt / fire-urgency goals are unrelated and untouched.
 *
 * <p>Unit: {@code perf.evoker_summon_interval} (gated by mixin plugin / FeatureUnits).
 *
 * @see EvokerFangIntervalMixin sibling Evoker nested-goal casting-interval {@code @ModifyConstant}
 * @see EvokerWololoIntervalMixin sibling Evoker nested-goal casting-interval {@code @ModifyConstant}
 * @see GuardianAttackDurationMixin sibling getter-return stretch {@code @ModifyConstant} pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Evoker$EvokerSummonSpellGoal")
public abstract class EvokerSummonIntervalMixin {

    /**
     * Stretches summon-vex casting interval (vanilla {@code 340} → {@code 450} ticks).
     * {@code getCastingTime} ({@code 100}) is a different method and is not rewritten.
     *
     * @param original vanilla constant (always 340 at the matched site)
     * @return stretched casting interval in ticks
     */
    @ModifyConstant(method = "getCastingInterval", constant = @Constant(intValue = 340))
    private int omnifix$longerSummonCastInterval(int original) {
        return 450;
    }
}
