package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Turtle$TurtleLayEggGoal#tick} completes egg placement when
 * {@code layEggCounter > adjustedTickDelay(200)}. While laying, each tick also
 * increments the counter and (via {@code Turtle#aiStep}) may emit dig particles every
 * 5 ticks. Dense beach packs of gravid turtles therefore stay in the laying-egg AI
 * branch with particle/game-event work on a ~10s cadence; stretching the completion
 * threshold cuts how often full lay cycles finish (and how often turtles re-enter
 * post-lay breed cooldown / re-path work) under high entity density.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code tick} only rewrites the sole {@code int}
 * literal {@code 200} → {@code 300} ({@code adjustedTickDelay} lay-complete threshold,
 * +50%). Intentionally untouched: dig particle cadence in {@code Turtle#aiStep}
 * ({@code layEggCounter % 5}), {@code setInLoveTime(600)} post-lay love, egg-count
 * roll, go-home / go-to-water / panic / travel goals, and home proximity gates on
 * {@code canUse}/{@code canContinueToUse}.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code Turtle$TurtleLayEggGoal#tick}):
 * <ul>
 *   <li>{@code layEggCounter < 1} start-laying gate — int {@code 1}, untouched</li>
 *   <li>{@code layEggCounter > adjustedTickDelay(200)} complete threshold — <strong>only</strong>
 *       int {@code 200} in method (this injector)</li>
 *   <li>{@code nextInt(4) + 1} egg count roll — ints {@code 4}/{@code 1}, untouched</li>
 *   <li>{@code setBlock(..., 3)} block update flags — int {@code 3}, untouched</li>
 *   <li>{@code setInLoveTime(600)} post-lay love — int {@code 600}, untouched</li>
 *   <li>sound pitch floats {@code 0.3F}/{@code 0.9F}/{@code 0.2F} — out of scope</li>
 * </ul>
 * No other {@code 200} exists in {@code tick}, so {@code ModifyConstant} on
 * {@code intValue = 200} is safe without ordinal narrowing.
 *
 * <p>Trade-off: egg-laying animation/process lasts longer (200→300 ticks before
 * {@code adjustedTickDelay}, +50%; fewer complete lays per minute under density). Dig
 * particles still fire every 5 counter ticks while laying; post-lay love time and egg
 * stack size are unchanged. Panic / go-home / breed goals are out of scope.
 *
 * <p>Unit: {@code perf.turtle_lay_egg_duration} (gated by mixin plugin / FeatureUnits).
 *
 * @see TurtleGoHomeChanceMixin sibling nested Turtle goal {@code @ModifyConstant} pattern
 * @see BeeHiveLocateMixin sibling nested goal {@code 200→300} duration/cooldown pattern
 * @see GhastFireballChargeMixin sibling nested-goal charge/duration {@code @ModifyConstant}
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleLayEggGoal")
public abstract class TurtleLayEggDurationMixin {

    /**
     * Stretches the lay-complete threshold (vanilla {@code adjustedTickDelay(200)} →
     * {@code adjustedTickDelay(300)}).
     *
     * @param original vanilla constant (always 200 at the matched site)
     * @return stretched lay-egg counter threshold base in ticks
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 200))
    private int omnifix$longerTurtleLayEgg(int original) {
        return 300;
    }
}
