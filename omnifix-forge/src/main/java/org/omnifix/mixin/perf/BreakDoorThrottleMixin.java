package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link BreakDoorGoal#canUse} (via {@code DoorInteractGoal}) checks horizontal
 * collision, door block state, griefing gamerule, and difficulty every goal-selector evaluation
 * for zombies that can break doors. Throttling to every third mob tick cuts redundant door-probe
 * cost during dense zombie/raid pressure.
 *
 * <p>Trade-off: door-break goals may start up to ~2 ticks later. Active break progress
 * ({@code tick}/{@code canContinueToUse}) is unchanged once the goal is running. No panic/flee
 * or fire-urgency path is affected.
 */
@Mixin(BreakDoorGoal.class)
public abstract class BreakDoorThrottleMixin {

    /** Inherited from {@code DoorInteractGoal} (not {@code final} on the parent). */
    @Shadow
    protected Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleBreakDoor(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
