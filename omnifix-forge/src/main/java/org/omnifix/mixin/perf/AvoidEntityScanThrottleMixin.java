package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link AvoidEntityGoal#canUse} performs an inflated AABB {@code getEntitiesOfClass}
 * + nearest-entity + path create on every goal-selector evaluation for cows/pigs/etc. Running the
 * scan only every third mob tick preserves flee behavior with lower steady-state entity-query cost.
 *
 * <p>Trade-off: mobs may notice threats up to ~2 ticks later.
 */
@Mixin(AvoidEntityGoal.class)
public abstract class AvoidEntityScanThrottleMixin {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleAvoidScan(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
