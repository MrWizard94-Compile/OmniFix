package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link MoveThroughVillageGoal#canUse} runs village POI manager queries every
 * goal-selector evaluation for villagers/iron golems that use it. Throttling to every third tick
 * cuts POI load in dense villages.
 *
 * <p>Trade-off: path-through-village starts up to ~2 ticks later.
 */
@Mixin(MoveThroughVillageGoal.class)
public abstract class MoveThroughVillageThrottleMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleVillagePoi(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
