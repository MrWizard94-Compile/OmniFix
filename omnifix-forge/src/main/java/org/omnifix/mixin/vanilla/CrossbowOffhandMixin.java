package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-227169 — A charged crossbow in the offhand breaks main-hand first-person rendering.
 *
 * <p>Root cause: {@code PlayerRenderer#getArmPose} returns {@code CROSSBOW_HOLD} (two-handed) for
 * any charged crossbow hand. In first person that offhand two-handed pose corrupts the main-hand
 * arm pose/render. Restrict charged-crossbow pose to the main hand while the local player is in
 * first person; third-person and other entities keep the vanilla pose.
 */
@Mixin(PlayerRenderer.class)
public abstract class CrossbowOffhandMixin {

    @ModifyExpressionValue(
            method = "getArmPose",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CrossbowItem;isCharged(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean omnifix$onlyMainHandCrossbowHold(
            boolean crossbowCharged,
            AbstractClientPlayer player,
            InteractionHand hand
    ) {
        if (!crossbowCharged) {
            return false;
        }
        Minecraft client = Minecraft.getInstance();
        // Allow offhand charged pose in third person / other cameras; suppress only first-person local.
        if (hand == InteractionHand.MAIN_HAND) {
            return true;
        }
        return client.cameraEntity != player || !client.options.getCameraType().isFirstPerson();
    }
}
