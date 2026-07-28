package org.omnifix.mixin.perf;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link OwnerHurtByTargetGoal#canUse} re-reads the owner's last-hurt-by target,
 * timestamp, and {@code wantsToAttack} gates on every goal-selector evaluation for every tamed
 * pet. Pack farms (wolves, cats) pay that owner/target walk each tick even when the owner has
 * not been attacked recently. Throttling to every other pet tick halves steady-state canUse cost.
 *
 * <p>Policy: {@code @Inject canUse HEAD} cancels with {@code false} when
 * {@code (tameAnimal.tickCount & 1) != 0} so evaluation runs only on even entity ticks.
 *
 * <p>Trade-off: pets may assist against the owner's attacker up to 1 tick later.
 *
 * <p>Unit: {@code perf.owner_hurt_by_throttle} (gated by mixin plugin / FeatureUnits).
 *
 * @see AvoidEntityScanThrottleMixin sibling {@code canUse HEAD} throttle pattern
 * @see FollowOwnerRepathMixin sibling pet-goal interval pattern
 */
@Mixin(OwnerHurtByTargetGoal.class)
public abstract class OwnerHurtByThrottleMixin {

    @Shadow
    @Final
    private TamableAnimal tameAnimal;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleOwnerHurtBy(CallbackInfoReturnable<Boolean> cir) {
        if ((this.tameAnimal.tickCount & 1) != 0) {
            cir.setReturnValue(false);
        }
    }
}
