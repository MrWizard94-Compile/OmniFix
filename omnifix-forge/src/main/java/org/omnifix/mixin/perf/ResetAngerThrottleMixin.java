package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link ResetUniversalAngerTargetGoal#canUse} is polled every goal-selector pass for
 * neutral mobs (wolves, bees, endermen, iron golems, etc.). When it returns true, {@code start}
 * forgets the current target and may alert nearby same-type mobs with an entity scan. Running
 * {@code canUse} only every third mob tick cuts steady-state goal evaluation without removing
 * universal-anger reset once the goal actually starts.
 *
 * <p>Trade-off: anger-reset / same-type alert may fire up to ~2 ticks later after a new hurt.
 * Alerts still run fully when the goal starts (throttled acquisition only).
 */
@Mixin(ResetUniversalAngerTargetGoal.class)
public abstract class ResetAngerThrottleMixin {

    /**
     * Vanilla field is {@code private final T mob} with {@code T extends Mob & NeutralMob};
     * shadowed as {@link Mob} (runtime erasure of the first bound).
     */
    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleResetAnger(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
