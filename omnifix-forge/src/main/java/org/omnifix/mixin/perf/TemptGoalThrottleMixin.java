package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link TemptGoal#canUse} calls {@code Level.getNearestPlayer} every evaluation when
 * not calming down. Every other tick is enough for food-follow responsiveness on farms.
 *
 * <p>Trade-off: animals may notice held food ~1 tick later. {@code calmDown} still decrements on
 * every evaluation so post-tempt cooldowns stay correct.
 */
@Mixin(TemptGoal.class)
public abstract class TemptGoalThrottleMixin {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    private int calmDown;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleTempt(CallbackInfoReturnable<Boolean> cir) {
        // Let calmDown decrement in vanilla body every tick.
        if (this.calmDown > 0) {
            return;
        }
        if ((this.mob.tickCount & 1) != 0) {
            cir.setReturnValue(false);
        }
    }
}
