package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.animal.horse.Llama;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link LlamaFollowCaravanGoal#canUse} performs inflated AABB
 * {@code getEntities} (inflate 9,4,9) llama scans when the mob is not leashed /
 * already in a caravan, on every goal-selector evaluation. Throttling acquisition
 * to every third llama tick cuts steady-state entity-section query cost.
 *
 * <p>Trade-off: caravan join may start ~2 ticks later. Active caravan follow
 * ({@code tick}/{@code canContinueToUse}) is unchanged once the goal is running.
 * No panic/flee or fire-urgency path is affected.
 */
@Mixin(LlamaFollowCaravanGoal.class)
public abstract class LlamaCaravanThrottleMixin {

    @Shadow
    @Final
    public Llama llama;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleLlamaCaravan(CallbackInfoReturnable<Boolean> cir) {
        if ((this.llama.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
