package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: {@link RandomStrollGoal#canUse} rolls {@code nextInt(reducedTickDelay(interval))}
 * before pathfinding. Default interval is 120. Raising it reduces failed path starts on dense mob
 * packs without changing path quality when a stroll does start.
 *
 * <p>Trade-off: idle mobs wait slightly longer between random walks.
 */
@Mixin(RandomStrollGoal.class)
public abstract class RandomStrollIntervalMixin {

    private static final int OMNIFIX$MAX_INTERVAL = 240;

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/PathfinderMob;DIZ)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static int omnifix$bumpStrollInterval(int interval) {
        if (interval <= 0) {
            return interval;
        }
        long bumped = interval + (interval / 2L);
        return (int) Math.min(OMNIFIX$MAX_INTERVAL, bumped);
    }
}
