package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Vex$VexRandomMoveGoal#canUse} rolls
 * {@code nextInt(reducedTickDelay(7)) == 0} before picking an idle random air target.
 * Bound vexes that are not already pathing re-evaluate this on every goal-selector pass;
 * dense raid / mansion packs therefore start random float paths on a ~1/7 cadence whenever
 * navigation is idle, burning pathfinding without improving charge-attack combat.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code canUse} only rewrites the sole {@code int}
 * literal {@code 7} → {@code 10} (nextInt gate base: 1/7 → 1/10 before {@code reducedTickDelay}).
 * Audit (MC 1.20.1 mapped {@code Vex$VexRandomMoveGoal}):
 * <ul>
 *   <li>{@code canUse}: sole {@code int 7} — {@code nextInt(reducedTickDelay(7)) == 0}</li>
 *   <li>{@code canContinueToUse}: navigation / wanted-position checks; no int {@code 7}</li>
 *   <li>{@code start}: random target offsets use other ints — out of scope</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 7} in {@code canUse} is therefore safe without
 * ordinal narrowing. Charge-attack goal lives on a separate nested class and is not touched.
 *
 * <p>Trade-off: vex idle random moves start less often (fewer idle air-path starts). Once a
 * random move does start, target radius and move speed are unchanged. Charge-attack cadence
 * and all combat urgency (including hurt response) are out of scope — no panic/flee exception
 * required for this idle wander gate.
 *
 * <p>Unit: {@code perf.vex_random_move_chance} (gated by mixin plugin / FeatureUnits).
 *
 * @see BeeWanderChanceMixin sibling nested idle-wander nextInt chance {@code @ModifyConstant}
 * @see EndermanTakeIntervalMixin sibling canUse {@code reducedTickDelay} nextInt stretch
 * @see GhastWanderRadiusMixin sibling flying idle-wander nested-goal pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Vex$VexRandomMoveGoal")
public abstract class VexRandomMoveChanceMixin {

    /**
     * Stretches the idle random-move chance base (vanilla {@code reducedTickDelay(7)} →
     * {@code reducedTickDelay(10)}).
     *
     * @param original vanilla constant (always 7 at the matched site)
     * @return stretched nextInt bound base for VexRandomMoveGoal canUse chance
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 7))
    private int omnifix$rarerVexRandomMove(int original) {
        return 10;
    }
}
