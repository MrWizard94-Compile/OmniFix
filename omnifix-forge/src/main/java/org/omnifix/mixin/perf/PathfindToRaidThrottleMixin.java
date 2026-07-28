package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.PathfindToRaidGoal;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link PathfindToRaidGoal#canUse} re-evaluates raid membership and pathfinding toward
 * the active raid center on every goal-selector pass for pillagers/vindicators/etc. Dense raids
 * multiply that work across many raiders. Throttling to every third raider tick cuts steady-state
 * pathfinder pressure while preserving join-raid navigation.
 *
 * <p>Trade-off: raiders may begin pathing toward the raid up to ~2 ticks later. Does not touch
 * combat, panic, or flee goals.
 */
@Mixin(PathfindToRaidGoal.class)
public abstract class PathfindToRaidThrottleMixin {

    @Shadow
    @Final
    private Raider mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttlePathfindToRaid(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
