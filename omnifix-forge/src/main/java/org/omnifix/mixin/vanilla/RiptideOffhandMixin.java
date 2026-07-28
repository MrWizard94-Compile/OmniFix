package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-127970 — Riptide with an item in the off-hand applies the spin-attack first-person pose to
 * that off-hand item.
 *
 * <p>Root cause: {@link ItemInHandRenderer#renderArmWithItem} checks
 * {@link AbstractClientPlayer#isAutoSpinAttack()} for both hands. Restrict the riptide pose to the
 * hand that actually holds a trident.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class RiptideOffhandMixin {

    @ModifyExpressionValue(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;isAutoSpinAttack()Z"
            )
    )
    private boolean omnifix$riptidePoseOnlyForTrident(
            boolean autoSpin,
            AbstractClientPlayer player,
            float partialTicks,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equippedProgress,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight
    ) {
        return autoSpin && stack.is(Items.TRIDENT);
    }
}
