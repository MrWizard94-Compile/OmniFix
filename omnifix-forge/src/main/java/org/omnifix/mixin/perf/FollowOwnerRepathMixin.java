package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while following, pets repath ({@code moveTo} / teleport check) every
 * {@code adjustedTickDelay(10)} ticks. Bumping the base to 15 reduces pathfinder pressure for
 * packs with many dogs/cats following players.
 *
 * <p>Trade-off: pets repath slightly less often while already following.
 */
@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerRepathMixin {

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
    private int omnifix$slowerRepath(int original) {
        return 15;
    }
}
