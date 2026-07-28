package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while melee-chasing, {@link MeleeAttackGoal} resets
 * {@code ticksUntilNextPathRecalculation = 4 + nextInt(7)} both in the {@code canPenalize}
 * branch of {@code canUse} and after a repath decision in {@code tick}. Dense melee packs
 * (zombies, hoglins, iron golems, etc.) therefore re-run {@code createPath}/{@code moveTo}
 * at a high cadence even when the target has barely moved.
 *
 * <p>Policy: {@code @ModifyConstant} rewrites the base literal {@code 4} → {@code 6} in both
 * methods. On 1.20.1 each method has exactly one {@code iconst_4}; other nearby ints are
 * {@code 0}/{@code 1}/{@code 5}/{@code 7}/{@code 10}/{@code 15} (penalties and distance bands),
 * so matching {@code intValue = 4} is unambiguous.
 *
 * <p>Trade-off: active melee repaths slightly less often (base interval 6 vs 4 ticks before
 * random + penalties). Attack cooldown / {@code checkAndPerformAttack} cadence is unchanged.
 * Complements {@code perf.melee_canuse_cooldown} (start-gate) without overlapping its long
 * {@code 20L} constant.
 *
 * <p>Unit: {@code perf.melee_path_recalc_base}
 */
@Mixin(MeleeAttackGoal.class)
public abstract class MeleePathRecalcMixin {

    /**
     * Raises the path-recalc base delay from 4 to 6 game ticks in both canUse (canPenalize)
     * and tick repath sites.
     *
     * @param original vanilla constant (always 4 at the matched sites)
     * @return stretched base delay before the next path recalculation
     */
    @ModifyConstant(method = {"canUse", "tick"}, constant = @Constant(intValue = 4))
    private int omnifix$slowerPathRecalcBase(int original) {
        return 6;
    }
}
