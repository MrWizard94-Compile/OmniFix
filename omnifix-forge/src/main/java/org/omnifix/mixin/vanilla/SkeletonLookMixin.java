package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-121706 — Skeletons (and other bow users) do not look at their target while strafing.
 *
 * <p>Root cause: {@link RangedBowAttackGoal#tick} uses {@link Mob#lookAt} when
 * {@code strafingTime > -1}, but only drives {@link net.minecraft.world.entity.ai.control.LookControl}
 * when not strafing. Body/head aim therefore lags during strafe. After each {@code lookAt}, also
 * push the look control toward the same target with the same max angles.
 */
@Mixin(RangedBowAttackGoal.class)
public abstract class SkeletonLookMixin {

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;lookAt(Lnet/minecraft/world/entity/Entity;FF)V"
            )
    )
    private void omnifix$lookControlWhileStrafing(
            Mob mob,
            Entity target,
            float maxYRotIncrease,
            float maxXRotIncrease,
            Operation<Void> original
    ) {
        original.call(mob, target, maxYRotIncrease, maxXRotIncrease);
        mob.getLookControl().setLookAt(target, maxYRotIncrease, maxXRotIncrease);
    }
}
