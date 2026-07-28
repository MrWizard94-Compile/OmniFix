package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: when {@link RangedCrossbowAttackGoal} finishes charging, it sets
 * {@code attackDelay = 20 + mob.getRandom().nextInt(20)} (range 20–39 ticks) before firing.
 * Pillager packs and other crossbow users then re-aim / hold charged state on that cadence;
 * dense raids amplify the post-charge idle work and volley frequency.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code tick} rewrites every {@code int} literal {@code 20}
 * to {@code 30}. In 1.20.1 those two literals are exactly the base delay and the {@code nextInt}
 * bound on the same assignment, so the delay becomes {@code 30 + nextInt(30)} (30–59 ticks, +50%).
 * Charge duration ({@code CrossbowItem.getChargeDuration}) is untouched.
 *
 * <p>Trade-off: pillagers / crossbow users fire slightly less often after each charge. Combat still
 * works; pathing-to-target and look logic stay as vanilla. Panic / flee-from-hurt / fire-urgency
 * goals are unrelated and untouched.
 *
 * <p>Unit: {@code perf.ranged_crossbow_delay}
 */
@Mixin(RangedCrossbowAttackGoal.class)
public abstract class RangedCrossbowDelayMixin {

    /**
     * Both the post-charge base ({@code 20 + …}) and {@code nextInt(20)} bound become 30.
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
    private int omnifix$longerAttackDelay(int original) {
        return 30;
    }
}
