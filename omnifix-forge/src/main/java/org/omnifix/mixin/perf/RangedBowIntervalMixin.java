package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: {@link RangedBowAttackGoal} fires bow volleys using constructor
 * {@code attackIntervalMin} (skeletons default 20). Each ready volley drives look/strafe/path
 * work while drawing; dense skeleton packs amplify that cadence.
 *
 * <p>Policy: bump {@code attackIntervalMin} by +50% at construction, cap at 60. Non-positive
 * values are left unchanged so callers that disable the interval stay intact. The
 * {@code Monster}-typed constructors chain into
 * {@code <init>(Lnet/minecraft/world/entity/Mob;DIF)V}, so one {@code ModifyVariable} covers
 * all vanilla bow-user construction paths.
 *
 * <p>Trade-off: bow users shoot slightly less often (e.g. 20 → 30 ticks). Combat still works;
 * urgency goals (panic / flee-from-hurt) are unrelated and untouched.
 *
 * <p>Unit: {@code perf.ranged_bow_interval}
 */
@Mixin(RangedBowAttackGoal.class)
public abstract class RangedBowIntervalMixin {

    private static final int OMNIFIX$MAX_INTERVAL = 60;

    /**
     * Full {@code (Mob, double, int, float)} ctor receives the raw min interval. Bump by +50%,
     * cap at {@link #OMNIFIX$MAX_INTERVAL}.
     */
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/Mob;DIF)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static int omnifix$bumpAttackIntervalMin(int attackIntervalMin) {
        if (attackIntervalMin <= 0) {
            return attackIntervalMin;
        }
        long bumped = attackIntervalMin + (attackIntervalMin / 2L);
        return (int) Math.min(OMNIFIX$MAX_INTERVAL, bumped);
    }
}
