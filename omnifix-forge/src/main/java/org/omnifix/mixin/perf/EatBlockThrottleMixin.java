package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link EatBlockGoal#canUse} rolls a rare random gate then samples grass / grass-block
 * states at the mob feet every goal-selector evaluation (sheep, goats, etc.). Evaluating only every
 * third mob tick cuts block-state lookups and random rolls while keeping graze frequency intact.
 *
 * <p>Trade-off: grass-eat goals may start up to ~2 ticks later. Not urgency-critical (no panic/flee).
 */
@Mixin(EatBlockGoal.class)
public abstract class EatBlockThrottleMixin {

    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleEatBlock(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
