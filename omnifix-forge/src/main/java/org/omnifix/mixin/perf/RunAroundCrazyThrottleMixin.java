package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link RunAroundLikeCrazyGoal#canUse} is evaluated every goal pass for untamed horses.
 * Throttle to every third tick.
 *
 * <p>Trade-off: crazy-run reaction may lag up to ~2 ticks.
 */
@Mixin(RunAroundLikeCrazyGoal.class)
public abstract class RunAroundCrazyThrottleMixin {

    @Shadow
    @Final
    private AbstractHorse horse;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleCrazyRun(CallbackInfoReturnable<Boolean> cir) {
        if ((this.horse.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
