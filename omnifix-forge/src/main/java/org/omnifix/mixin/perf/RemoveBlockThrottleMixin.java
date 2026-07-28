package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link RemoveBlockGoal#canUse} (and its {@code MoveToBlockGoal} search) scans for
 * target blocks on every goal-selector evaluation (e.g. rabbits eating carrot crops). Throttling
 * to every third mob tick cuts block-search cost while preserving remove-block behavior.
 *
 * <p>Trade-off: remove-block goals may start up to ~2 ticks later.
 */
@Mixin(RemoveBlockGoal.class)
public abstract class RemoveBlockThrottleMixin {

    @Shadow
    @Final
    private Mob removerMob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleRemoveBlock(CallbackInfoReturnable<Boolean> cir) {
        if ((this.removerMob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
