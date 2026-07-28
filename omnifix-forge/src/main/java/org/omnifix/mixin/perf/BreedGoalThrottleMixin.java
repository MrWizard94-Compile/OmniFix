package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: while in love, {@link BreedGoal#canUse} runs {@code getNearbyEntities} partner scans
 * every goal evaluation. Every-other-tick is enough for breeding; dense love farms pay less scan cost.
 *
 * <p>Trade-off: partners may pair ~1 tick later.
 */
@Mixin(BreedGoal.class)
public abstract class BreedGoalThrottleMixin {

    @Shadow
    @Final
    protected Animal animal;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleBreed(CallbackInfoReturnable<Boolean> cir) {
        if ((this.animal.tickCount & 1) != 0) {
            cir.setReturnValue(false);
        }
    }
}
