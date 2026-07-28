package org.omnifix.mixin.perf;

import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link Slime#getJumpDelay} returns {@code random.nextInt(20) + 10} (10–29 ticks)
 * between ground landings and the next hop. Swarms re-evaluate movement, path look-ahead, and
 * attack-window timing every jump; dense slime packs therefore burn AI ticks on hop cadence
 * even when idle or not targeting a player.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on {@code getJumpDelay} only:
 * <ul>
 *   <li>{@code nextInt(20)} exclusive upper bound → {@code 30} (+50%)</li>
 *   <li>{@code + 10} minimum offset → {@code 15} (+50%)</li>
 * </ul>
 * Resulting delay: vanilla {@code [10, 29]} → OmniFix {@code [15, 44]} ticks.
 * Audit (MC 1.20.1 {@code Slime#getJumpDelay}): the method body is solely
 * {@code return this.random.nextInt(20) + 10;} — the two int literals are unique, so no
 * ordinal is required. {@code MagmaCube} overrides {@code getJumpDelay} as
 * {@code super.getJumpDelay() * 4} and therefore inherits the longer base delay
 * (further multiplied by four). Other {@code Slime} constants ({@code size} scales,
 * particle counts, {@code dealDamage} ranges) live in different methods and are untouched.
 *
 * <p>Trade-off: slimes (and magma cubes that multiply this delay) jump less often, reducing
 * hop-driven AI and collision work. Jump impulse, target acquisition, split-on-death, and
 * damage-on-contact logic are unchanged. Not a panic / flee goal — no fire/hurt urgency path
 * is gated on jump delay.
 *
 * <p>Unit: {@code perf.slime_jump_delay}
 *
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 * @see BeePollinateCooldownMixin sibling dual {@code nextInt} bound stretch pattern
 * @see BlazeFireIntervalMixin sibling dual {@code @ModifyConstant} on one method
 */
@Mixin(Slime.class)
public abstract class SlimeJumpDelayMixin {

    /**
     * Stretches the {@code nextInt} exclusive upper bound (vanilla 20 → 30).
     *
     * @param original vanilla constant (always 20 at the matched site)
     * @return stretched exclusive upper bound for jump delay roll
     */
    @ModifyConstant(method = "getJumpDelay", constant = @Constant(intValue = 20))
    private int omnifix$longerJumpDelayRange(int original) {
        return 30;
    }

    /**
     * Stretches the minimum delay offset (vanilla 10 → 15).
     *
     * @param original vanilla constant (always 10 at the matched site)
     * @return stretched minimum ticks between jumps
     */
    @ModifyConstant(method = "getJumpDelay", constant = @Constant(intValue = 10))
    private int omnifix$longerJumpDelayOffset(int original) {
        return 15;
    }
}
