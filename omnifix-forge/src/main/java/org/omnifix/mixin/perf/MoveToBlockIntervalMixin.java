package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link MoveToBlockGoal#nextStartTick} schedules the next block search as
 * {@code reducedTickDelay(200 + random.nextInt(200))}. Both {@code sipush 200} constants become
 * 300 so animals/farm mobs re-scan target blocks 50% less often after a failed or completed move.
 *
 * <p>Trade-off: crop/farm goals (e.g. rabbits, turtles, foxes) may wait slightly longer between
 * block searches; path quality when a search does run is unchanged.
 */
@Mixin(MoveToBlockGoal.class)
public abstract class MoveToBlockIntervalMixin {

    /**
     * Replaces both vanilla {@code 200} literals in {@code nextStartTick} (base delay and
     * {@code nextInt} bound) with {@code 300}.
     *
     * @param original vanilla constant value (always 200 for matched sites)
     * @return stretched interval base / random bound
     */
    @ModifyConstant(
            method = "nextStartTick(Lnet/minecraft/world/entity/PathfinderMob;)I",
            constant = @Constant(intValue = 200)
    )
    private int omnifix$longerMoveToBlockInterval(int original) {
        return 300;
    }
}
