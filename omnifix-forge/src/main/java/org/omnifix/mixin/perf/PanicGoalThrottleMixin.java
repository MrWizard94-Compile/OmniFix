package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link PanicGoal#canUse} re-evaluates last-hurt / fire state and may invoke
 * {@code lookForWater} (path search) on every goal-selector pass for animals that panic when
 * damaged or ignited. Halving evaluation frequency cuts steady-state pathfinder pressure for
 * large animal herds without removing panic behavior.
 *
 * <p>Hard law: never throttle when the mob is on fire so urgent water-search for extinguishing
 * stays on every goal evaluation.
 *
 * <p>Trade-off: non-fire panic (e.g. damage-triggered flee) may start up to ~1 tick later.
 * On-fire panic is unthrottled and unchanged.
 */
@Mixin(PanicGoal.class)
public abstract class PanicGoalThrottleMixin {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttlePanic(CallbackInfoReturnable<Boolean> cir) {
        // Never throttle when on fire (urgent lookForWater).
        if (this.mob.isOnFire()) {
            return;
        }
        if ((this.mob.tickCount & 1) != 0) {
            cir.setReturnValue(false);
        }
    }
}
