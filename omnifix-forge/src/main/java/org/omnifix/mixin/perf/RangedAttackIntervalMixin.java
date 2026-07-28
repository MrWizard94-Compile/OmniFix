package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: {@link RangedAttackGoal} stores constructor {@code attackIntervalMin}/{@code attackIntervalMax}
 * and re-rolls the next volley delay from that range after each shot. Dense packs of skeletons,
 * pillagers, strays, blazes (via custom goals), etc. re-path / re-aim on that cadence. Raising both
 * bounds by +50% (cap 80) cuts projectile-AI work without changing projectile damage or aim logic.
 *
 * <p>The 4-arg convenience ctor chains into the full
 * {@code (RangedAttackMob, double, int, int, float)} ctor with min==max, so two {@code ModifyVariable}
 * handlers on the full ctor cover every construction path.
 *
 * <p>Trade-off: ranged mobs fire volleys ~50% less often (slower DPS cadence). Non-positive intervals
 * are left unchanged so callers that disable the roll stay intact. Panic / flee / fire-urgency goals
 * are unaffected (this only widens the ranged attack timer).
 *
 * <p>Unit: {@code perf.ranged_attack_interval}
 */
@Mixin(RangedAttackGoal.class)
public abstract class RangedAttackIntervalMixin {

    private static final int OMNIFIX$MAX_INTERVAL = 80;

    /**
     * Full constructor receives raw min interval before storage. Bump by +50%, cap at
     * {@link #OMNIFIX$MAX_INTERVAL}.
     */
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/monster/RangedAttackMob;DIIF)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static int omnifix$bumpAttackIntervalMin(int attackIntervalMin) {
        return omnifix$bumpInterval(attackIntervalMin);
    }

    /**
     * Full constructor receives raw max interval before storage. Bump by +50%, cap at
     * {@link #OMNIFIX$MAX_INTERVAL}.
     */
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/monster/RangedAttackMob;DIIF)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1
    )
    private static int omnifix$bumpAttackIntervalMax(int attackIntervalMax) {
        return omnifix$bumpInterval(attackIntervalMax);
    }

    private static int omnifix$bumpInterval(int interval) {
        if (interval <= 0) {
            return interval;
        }
        long bumped = interval + (interval / 2L);
        return (int) Math.min(OMNIFIX$MAX_INTERVAL, bumped);
    }
}
