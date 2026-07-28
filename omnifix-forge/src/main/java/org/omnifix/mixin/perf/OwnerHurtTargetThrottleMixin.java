package org.omnifix.mixin.perf;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link OwnerHurtTargetGoal#canUse} re-reads the owner's last-hurt target and runs
 * {@code canAttack} / {@code wantsToAttack} every goal-selector evaluation for every tamed pet.
 * Packs of dogs/cats pay that owner-assist check continuously even when the owner is not fighting.
 * Running acquisition only on even pet ticks halves steady-state assist-target evaluation.
 *
 * <p>Hard law: this is assist acquisition only (owner's last hurt mob). It does not touch panic,
 * flee-from-hurt, or fire urgency paths. Once the goal starts, {@code start}/{@code tick} stay
 * full-rate.
 *
 * <p>Trade-off: pets may assist the owner's attack up to 1 tick later (~50&nbsp;ms).
 *
 * <p>Unit: {@code perf.owner_hurt_target_throttle}
 *
 * @see AvoidEntityScanThrottleMixin sibling {@code canUse} HEAD throttle pattern
 * @see FollowOwnerRepathMixin sibling pet-goal cost reduction
 */
@Mixin(OwnerHurtTargetGoal.class)
public abstract class OwnerHurtTargetThrottleMixin {

    @Shadow
    @Final
    private TamableAnimal tameAnimal;

    /**
     * Rejects {@code canUse} on odd entity ticks so owner-assist acquisition runs every other tick.
     *
     * @param cir cancelable return for {@link OwnerHurtTargetGoal#canUse}
     */
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleOwnerHurtTarget(CallbackInfoReturnable<Boolean> cir) {
        if ((this.tameAnimal.tickCount & 1) != 0) {
            cir.setReturnValue(false);
        }
    }
}
