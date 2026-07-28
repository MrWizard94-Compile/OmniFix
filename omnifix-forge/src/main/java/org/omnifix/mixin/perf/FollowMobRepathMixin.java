package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while following, {@link FollowMobGoal#tick} repaths every
 * {@code adjustedTickDelay(10)} ticks. Raise base delay to 15 to cut pathfinder pressure.
 */
@Mixin(FollowMobGoal.class)
public abstract class FollowMobRepathMixin {

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
    private int omnifix$slowerRepath(int original) {
        return 15;
    }
}
