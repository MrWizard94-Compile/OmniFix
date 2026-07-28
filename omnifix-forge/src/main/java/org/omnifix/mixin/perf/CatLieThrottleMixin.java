package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link CatLieOnBedGoal#canUse} (and its {@code MoveToBlockGoal} bed search) runs on
 * every goal-selector evaluation for cats. Dense villages with many cats re-scan for beds
 * constantly. Throttling to every third tick cuts block-search cost while preserving lie-on-bed.
 *
 * <p>Trade-off: cats may start lying on a bed up to ~2 ticks later.
 */
@Mixin(CatLieOnBedGoal.class)
public abstract class CatLieThrottleMixin {

    @Shadow
    @Final
    private Cat cat;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleCatLie(CallbackInfoReturnable<Boolean> cir) {
        if ((this.cat.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
