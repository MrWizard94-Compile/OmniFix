package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link FleeSunGoal#canUse} re-evaluates day time, fire immunity, sky light, and a
 * hide-position path search every goal-selector pass for undead/sun-sensitive mobs. Throttling to
 * every third mob tick preserves shade-seeking with lower steady-state pathfinding cost.
 *
 * <p>Trade-off: mobs may begin fleeing sun up to ~2 ticks later.
 */
@Mixin(FleeSunGoal.class)
public abstract class FleeSunThrottleMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleFleeSun(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
