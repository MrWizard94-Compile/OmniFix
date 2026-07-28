package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Unit: {@code perf.try_find_water_throttle}
 *
 * <p>Root cause: {@link TryFindWaterGoal#canUse} walks nearby block positions looking for water
 * every goal-selector pass for dry-ground turtles, frogs, and similar amphibious mobs. That
 * horizontal water-block seek is pure waste when the mob is already wet or nowhere near water;
 * throttling to every third mob tick cuts redundant scans under dense amphibian pressure.
 *
 * <p>Trade-off: turtles/frogs water seek may start up to ~2 ticks later. Active water-path
 * progress ({@code tick}/{@code canContinueToUse}) is unchanged once the goal is running. No
 * panic/flee or fire-urgency path is affected.
 */
@Mixin(TryFindWaterGoal.class)
public abstract class TryFindWaterThrottleMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleTryFindWater(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
