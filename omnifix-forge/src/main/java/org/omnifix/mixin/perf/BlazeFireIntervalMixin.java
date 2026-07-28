package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Blaze$BlazeAttackGoal#tick} drives small-fireball volleys via
 * {@code attackTime} countdown assignments. When in ranged LOS range, step 1 sets
 * {@code attackTime = 60} and charges the blaze; steps 2–4 fire one small fireball each
 * with {@code attackTime = 6} between shots; after the volley {@code attackTime = 100}
 * and charge clears. Dense fortress packs therefore spawn three {@code SmallFireball}
 * entities per blaze on a short charge/cooldown cadence, inflating projectile AI and
 * collision work.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on {@code tick} only:
 * <ul>
 *   <li>{@code attackTime = 60} start-charge → {@code 90} (+50%)</li>
 *   <li>{@code attackTime = 100} post-volley recovery → {@code 130} (+30 ticks)</li>
 * </ul>
 * Audit (MC 1.20.1 {@code Blaze$BlazeAttackGoal#tick}):
 * <ul>
 *   <li>{@code attackTime = 60} charge-up — sole {@code int} {@code 60} in method</li>
 *   <li>{@code attackTime = 100} post-volley — sole {@code int} {@code 100} in method</li>
 *   <li>{@code attackTime = 20} melee close-range — different int, <strong>untouched</strong></li>
 *   <li>{@code attackTime = 6} between-shot spacing — different int, <strong>untouched</strong></li>
 *   <li>{@code attackStep} compares ({@code 1}, {@code 4}), {@code lastSeen < 5}, look
 *       floats — other constants, out of scope</li>
 * </ul>
 * No ordinal needed: each rewritten value is unique in {@code tick}. Melee and
 * within-volley cadence stay vanilla so close-range urgency and volley feel are preserved.
 *
 * <p>Trade-off: blazes charge and recover between volleys more slowly (fewer small-fireball
 * spawns per minute). Once a volley starts, inter-shot spacing ({@code 6}) and close-range
 * melee ({@code 20}) are unchanged. Panic / flee-from-hurt goals are unrelated; blaze
 * {@code isOnFire()} mirrors charge state and is not throttled by this unit.
 *
 * <p>Unit: {@code perf.blaze_fire_interval}
 *
 * @see GhastFireballChargeMixin sibling fireball-charge {@code @ModifyConstant} pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 * @see EndermanTakeIntervalMixin sibling nested-goal {@code targets = "...$Inner"} pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Blaze$BlazeAttackGoal")
public abstract class BlazeFireIntervalMixin {

    /**
     * Stretches the charge-up timer before the first fireball of a volley
     * (vanilla {@code attackTime = 60} → {@code 90}).
     *
     * @param original vanilla constant (always 60 at the matched site)
     * @return stretched charge-up ticks
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 60))
    private int omnifix$longerChargeUp(int original) {
        return 90;
    }

    /**
     * Stretches the post-volley recovery before the next charge cycle
     * (vanilla {@code attackTime = 100} → {@code 130}).
     *
     * @param original vanilla constant (always 100 at the matched site)
     * @return stretched post-volley cooldown ticks
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 100))
    private int omnifix$longerPostVolleyCooldown(int original) {
        return 130;
    }
}
