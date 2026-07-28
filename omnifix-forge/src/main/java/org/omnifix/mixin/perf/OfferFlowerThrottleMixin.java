package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link OfferFlowerGoal#canUse} scans for nearby villagers every goal evaluation on
 * iron golems. Throttle to every third tick.
 *
 * <p>Trade-off: flower-offer animations may start slightly later.
 */
@Mixin(OfferFlowerGoal.class)
public abstract class OfferFlowerThrottleMixin {

    @Shadow
    @Final
    private IronGolem golem;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleOfferFlower(CallbackInfoReturnable<Boolean> cir) {
        if ((this.golem.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
