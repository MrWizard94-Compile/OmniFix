package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-31819 — Hunger saturation still depletes on Peaceful difficulty.
 *
 * <p>Root cause: {@link Player#causeFoodExhaustion} only skips exhaustion on the client side
 * ({@code level.isClientSide}). Peaceful already refills food, but exhaustion still drains
 * saturation first. Treat Peaceful like the client-side early-out so exhaustion is never applied.
 */
@Mixin(Player.class)
public abstract class PeacefulSaturationMixin extends LivingEntity {

    protected PeacefulSaturationMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @ModifyExpressionValue(
            method = "causeFoodExhaustion",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;isClientSide:Z"
            )
    )
    private boolean omnifix$noExhaustionOnPeaceful(boolean isClientSide) {
        return isClientSide || this.level().getDifficulty() == Difficulty.PEACEFUL;
    }
}
