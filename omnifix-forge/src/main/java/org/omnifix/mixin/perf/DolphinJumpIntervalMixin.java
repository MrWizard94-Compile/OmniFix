package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.DolphinJumpGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: {@link DolphinJumpGoal#canUse} rolls against the constructor interval before
 * water-surface / path checks that drive jump attempts. Raising the interval reduces how often
 * idle dolphins re-evaluate jumps without changing jump quality when an attempt does start.
 *
 * <p>Trade-off: dolphins wait slightly longer between surface jump attempts (cosmetic AI cadence).
 * No effect on panic / flee goals.
 *
 * <p>Unit: {@code perf.dolphin_jump_interval}
 */
@Mixin(DolphinJumpGoal.class)
public abstract class DolphinJumpIntervalMixin {

    private static final int OMNIFIX$MAX_INTERVAL = 240;

    /**
     * Constructor receives the raw interval. Bump by +50%, cap at {@link #OMNIFIX$MAX_INTERVAL}.
     * Non-positive intervals are left unchanged so callers that disable the roll stay intact.
     */
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/animal/Dolphin;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static int omnifix$bumpDolphinJumpInterval(int interval) {
        if (interval <= 0) {
            return interval;
        }
        long bumped = interval + (interval / 2L);
        return (int) Math.min(OMNIFIX$MAX_INTERVAL, bumped);
    }
}
