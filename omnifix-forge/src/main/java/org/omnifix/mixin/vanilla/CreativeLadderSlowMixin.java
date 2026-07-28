package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-12829 — Creative / ability flight is slowed by ladders, vines, and scaffolding because
 * {@link LivingEntity#onClimbable()} never checks {@code abilities.flying}.
 *
 * <p>Root cause: {@code onClimbable} early-returns only for spectators. The same early-return path
 * is reused when the living entity is a flying player so climbable blocks no longer clamp flight
 * velocity via {@code handleOnClimbable}.
 */
@Mixin(LivingEntity.class)
public abstract class CreativeLadderSlowMixin {

    /**
     * {@code onClimbable} does {@code if (isSpectator()) return false}. Expanding that predicate
     * to flying players makes the method return false and skips climb slowdown.
     */
    @ModifyExpressionValue(
            method = "onClimbable",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isSpectator()Z"
            )
    )
    private boolean omnifix$treatFlyingAsNotClimbable(boolean isSpectator) {
        if (isSpectator) {
            return true;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        return self instanceof Player player && player.getAbilities().flying;
    }
}
