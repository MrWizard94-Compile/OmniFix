package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: {@link StrollThroughVillageGoal#canUse} rolls against the constructor interval
 * before village section scans / pathfinding. Raising the interval reduces failed path starts
 * on dense packs without changing path quality when a stroll does start.
 *
 * <p>Trade-off: idle mobs wait slightly longer between village strolls.
 */
@Mixin(StrollThroughVillageGoal.class)
public abstract class StrollVillageIntervalMixin {

    private static final int OMNIFIX$MAX_INTERVAL = 240;

    /**
     * Constructor receives the raw interval. Bump by +50%, cap at {@link #OMNIFIX$MAX_INTERVAL}.
     */
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/PathfinderMob;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static int omnifix$bumpVillageStrollInterval(int interval) {
        if (interval <= 0) {
            return interval;
        }
        long bumped = interval + (interval / 2L);
        return (int) Math.min(OMNIFIX$MAX_INTERVAL, bumped);
    }
}
