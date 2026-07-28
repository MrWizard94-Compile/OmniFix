package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link MeleeAttackGoal#canUse} early-outs with
 * {@code if (i - this.lastCanUseCheck < COOLDOWN_BETWEEN_CAN_USE_CHECKS)} where the cooldown is
 * the inlined long {@code 20L}. Idle / retargeting mobs still re-enter {@code canUse} often enough
 * that failed path creation storms dominate AI time when many melee mobs lack a reachable target.
 *
 * <p>Trade-off: melee goals re-evaluate {@code canUse} (and thus may call {@code createPath}) ~50%
 * less often (20→30 game ticks). Active combat {@code tick}/{@code checkAndPerformAttack} cadence
 * is unchanged; only the inter-check gate on starting the goal is stretched.
 *
 * <p>Unit: {@code perf.melee_canuse_cooldown}
 */
@Mixin(MeleeAttackGoal.class)
public abstract class MeleeCanUseCooldownMixin {

    /**
     * Stretches vanilla {@code COOLDOWN_BETWEEN_CAN_USE_CHECKS} (20L) to 30L inside {@code canUse}.
     *
     * @param original vanilla constant (always 20L at the matched site)
     * @return stretched canUse re-check interval in game ticks
     */
    @ModifyConstant(method = "canUse", constant = @Constant(longValue = 20L))
    private long omnifix$longerCanUseCooldown(long original) {
        return 30L;
    }
}
