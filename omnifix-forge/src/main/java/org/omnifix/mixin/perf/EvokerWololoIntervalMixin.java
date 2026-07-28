package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Evoker$EvokerWololoSpellGoal#getCastingInterval} returns a hard-coded
 * {@code 140} ticks between sheep-color (wololo) cast attempts. Under mansion / raid density
 * multiple evokers re-arm this decorative recolor spell on that cadence, each attempt driving
 * entity scans for nearby sheep plus spell-cast animation / sound work when the cast proceeds.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code getCastingInterval} rewrites the sole {@code int}
 * literal {@code 140} → {@code 200} (~+43%). Audit (MC 1.20.1
 * {@code Evoker$EvokerWololoSpellGoal}):
 * <ul>
 *   <li>{@code getCastingInterval()}: body is {@code return 140;} — sole int constant in method</li>
 *   <li>Fang / summon spell goals override {@code getCastingInterval} on
 *       <strong>different nested classes</strong> with different constants — out of scope</li>
 *   <li>Combat fang/summon casting, panic / flee-from-hurt, and on-fire urgency goals are
 *       unrelated and untouched</li>
 * </ul>
 * No ordinal needed: a single {@code intValue = 140} site exists in {@code getCastingInterval}.
 *
 * <p>Trade-off: sheep-color wololo is less frequent (140→200 ticks between cast-interval
 * returns; ~7s → ~10s at 20 TPS). When a wololo does start, sheep scan / recolor behavior is
 * unchanged. Combat spell goals keep their own intervals.
 *
 * <p>Unit: {@code perf.evoker_wololo_interval} (gated by mixin plugin / FeatureUnits).
 *
 * @see GuardianAttackDurationMixin sibling getter-constant {@code @ModifyConstant} pattern
 * @see BlazeFireIntervalMixin sibling nested-goal {@code targets = "...$Inner"} pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Evoker$EvokerWololoSpellGoal")
public abstract class EvokerWololoIntervalMixin {

    /**
     * Stretches wololo sheep-recolor casting interval (vanilla {@code 140} → {@code 200} ticks).
     *
     * @param original vanilla constant (always 140 at the matched site)
     * @return stretched casting interval in ticks
     */
    @ModifyConstant(method = "getCastingInterval", constant = @Constant(intValue = 140))
    private int omnifix$longerWololoCastingInterval(int original) {
        return 200;
    }
}
