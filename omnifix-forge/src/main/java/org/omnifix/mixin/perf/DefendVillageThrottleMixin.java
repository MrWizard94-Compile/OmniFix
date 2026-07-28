package org.omnifix.mixin.perf;

import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link DefendVillageTargetGoal#canUse} scans nearby villagers and players in a large
 * AABB every evaluation. Dense village golem packs pay heavy entity queries. Throttle to every
 * third golem tick.
 *
 * <p>Trade-off: golems may acquire village threats up to ~2 ticks later.
 */
@Mixin(DefendVillageTargetGoal.class)
public abstract class DefendVillageThrottleMixin {

    @Shadow
    @Final
    private IronGolem golem;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleDefendVillage(CallbackInfoReturnable<Boolean> cir) {
        if ((this.golem.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
