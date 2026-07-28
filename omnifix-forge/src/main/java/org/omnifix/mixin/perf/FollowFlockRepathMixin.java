package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while schooling, {@link FollowFlockLeaderGoal#tick} repaths to the flock leader
 * every {@code adjustedTickDelay(10)} ticks. Raise base delay to 15 to cut pathfinder pressure
 * in dense tropical-fish / cod schools.
 *
 * <p>Trade-off: schooling fish repath to leader slightly less often.
 *
 * <p>Unit: {@code perf.follow_flock_repath}
 */
@Mixin(FollowFlockLeaderGoal.class)
public abstract class FollowFlockRepathMixin {

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
    private int omnifix$slowerRepath(int original) {
        return 15;
    }
}
