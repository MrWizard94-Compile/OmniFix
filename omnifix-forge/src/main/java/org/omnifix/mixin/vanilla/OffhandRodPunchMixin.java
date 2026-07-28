package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-116379 — Punching while a cast fishing rod is in the off-hand makes the line appear detached.
 *
 * <p>Root cause: {@link FishingHookRenderer#render} always applies {@link Player#getAttackAnim} to
 * the first-person line attachment offset. When the cast rod is in the off-hand the main-hand
 * attack swing yanks that offset away from the rod. Zero the attack anim contribution whenever the
 * main hand cannot cast a fishing rod (Forge {@link ToolActions#FISHING_ROD_CAST}), matching the
 * renderer's own off-hand flip logic.
 */
@Mixin(FishingHookRenderer.class)
public abstract class OffhandRodPunchMixin {

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttackAnim(F)F"
            )
    )
    private float omnifix$noSwingWhenRodOffhand(float attackAnim, FishingHook hook) {
        Player owner = hook.getPlayerOwner();
        if (owner == null) {
            return attackAnim;
        }
        // Mirror FishingHookRenderer hand-side flip: non-castable main hand means rod is off-hand.
        if (!owner.getMainHandItem().canPerformAction(ToolActions.FISHING_ROD_CAST)) {
            return 0.0F;
        }
        return attackAnim;
    }
}
