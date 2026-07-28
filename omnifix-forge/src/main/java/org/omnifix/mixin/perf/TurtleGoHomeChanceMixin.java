package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Turtle$TurtleGoHomeGoal#canUse} rolls
 * {@code nextInt(reducedTickDelay(700)) != 0} (fail-fast random gate) before home-path
 * work. Beach turtles that are not already going home re-evaluate this on every
 * goal-selector pass; dense coastal packs therefore probe go-home pathfinding on a
 * ~1/700 cadence whenever navigation is idle, burning AI time without improving
 * combat, panic, or lay-egg urgency.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code canUse} only rewrites the sole {@code int}
 * literal {@code 700} → {@code 1000} (nextInt gate base: ~1/700 → ~1/1000 before
 * {@code reducedTickDelay}). Audit (MC 1.20.1 mapped {@code Turtle$TurtleGoHomeGoal}):
 * <ul>
 *   <li>{@code canUse}: sole {@code int 700} — {@code nextInt(reducedTickDelay(700)) != 0}</li>
 *   <li>{@code canContinueToUse}/{@code start}/{@code tick}: other ints live in different
 *       methods — out of scope</li>
 *   <li>Lay-egg, panic, and breed goals live on separate nested classes — not touched</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 700} in {@code canUse} is therefore safe
 * without ordinal narrowing.
 *
 * <p>Trade-off: turtles attempt go-home less often (fewer home-path starts). Once a go-home
 * does start, home destination, path speed, and continuation logic are unchanged. Panic /
 * flee-from-hurt and lay-egg goals are out of scope — no fire/hurt urgency exception is
 * required for this idle random gate.
 *
 * <p>Unit: {@code perf.turtle_go_home_chance} (gated by mixin plugin / FeatureUnits).
 *
 * @see VexRandomMoveChanceMixin sibling nested nextInt({@code reducedTickDelay}) chance
 * @see BeeWanderChanceMixin sibling nested idle canUse nextInt chance pattern
 * @see EndermanLeaveIntervalMixin sibling canUse {@code reducedTickDelay} nextInt stretch
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleGoHomeGoal")
public abstract class TurtleGoHomeChanceMixin {

    /**
     * Stretches the go-home random-gate base (vanilla {@code reducedTickDelay(700)} →
     * {@code reducedTickDelay(1000)}).
     *
     * @param original vanilla constant (always 700 at the matched site)
     * @return stretched nextInt bound base for TurtleGoHomeGoal canUse chance
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 700))
    private int omnifix$rarerTurtleGoHome(int original) {
        return 1000;
    }
}
