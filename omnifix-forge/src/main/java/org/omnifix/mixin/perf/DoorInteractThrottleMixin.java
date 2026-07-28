package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Unit: {@code perf.door_interact_throttle}.
 *
 * <p>Root cause: {@link DoorInteractGoal#canUse} probes horizontal collision and door block state
 * on every goal-selector evaluation for villagers/mobs that open (or inherit) doors. Throttling
 * to every third mob tick cuts redundant door-probe cost under dense pathing pressure.
 *
 * <p>{@code OpenDoorGoal} inherits {@code canUse}; {@code BreakDoorGoal#canUse} calls
 * {@code super.canUse()}. A separate {@code BreakDoorThrottleMixin} also throttles break-door
 * {@code canUse} — stacked throttling is intentional and harmless (still every 3rd tick when both
 * fire, or one layer alone when only this mixin applies to the super path).
 *
 * <p>Trade-off: door open/break start may lag up to ~2 ticks. Active door interaction
 * ({@code tick}/{@code canContinueToUse}) is unchanged once the goal is running. No panic/flee
 * or fire-urgency path is affected.
 */
@Mixin(DoorInteractGoal.class)
public abstract class DoorInteractThrottleMixin {

    @Shadow
    protected Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleDoorInteract(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
