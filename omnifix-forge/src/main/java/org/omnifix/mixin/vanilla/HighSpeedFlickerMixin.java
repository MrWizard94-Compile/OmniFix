package org.omnifix.mixin.vanilla;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * MC-111516 — Player flickers / turns invisible when flying at high speeds (elytra).
 *
 * <p>Root cause: {@link PlayerRenderer#setupRotations} banks the model with
 * {@code Math.acos(dot)} where {@code dot} is a normalized product of velocity and look
 * vectors. Float error at high speed can push the product slightly above 1.0, so
 * {@link Math#acos} returns NaN and subsequent pose multiplies corrupt the model matrix.
 * Clamp the acos argument into the valid domain.
 */
@Mixin(PlayerRenderer.class)
public abstract class HighSpeedFlickerMixin {

    @ModifyArg(
            method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;acos(D)D")
    )
    private double omnifix$clampElytraAcos(double value) {
        if (value > 1.0D) {
            return 1.0D;
        }
        if (value < -1.0D) {
            return -1.0D;
        }
        return value;
    }
}
