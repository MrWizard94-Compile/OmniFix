package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link RandomLookAroundGoal#canUse} rolls {@code nextFloat() < 0.02F} every tick while
 * idle, starting short random head-turn goals across almost all mobs. Scaling the threshold by
 * {@code 2/3} (~0.01333F) cuts idle look activations by about one third without touching look
 * duration or yaw selection once the goal is running.
 *
 * <p>Trade-off: idle random looks occur ~1/3 less often for nearly all mobs. No urgency path
 * (panic / fire / hurt flee) is affected — this goal is cosmetic idle behavior only.
 */
@Mixin(RandomLookAroundGoal.class)
public abstract class RandomLookProbabilityMixin {

    @ModifyConstant(method = "canUse", constant = @Constant(floatValue = 0.02F))
    private float omnifix$reduceLookProbability(float original) {
        return 0.02F * (2.0F / 3.0F);
    }
}
