package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link CatSitOnBlockGoal#canUse} walks nearby blocks looking for a valid sit
 * target every goal-selector evaluation for every cat. Throttling to every third tick cuts
 * block/state probes without removing cat sit behaviour.
 *
 * <p>Trade-off: cats may start sitting on blocks up to ~2 ticks later.
 */
@Mixin(CatSitOnBlockGoal.class)
public abstract class CatSitThrottleMixin {

    @Shadow
    @Final
    private Cat cat;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleCatSit(CallbackInfoReturnable<Boolean> cir) {
        if ((this.cat.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
