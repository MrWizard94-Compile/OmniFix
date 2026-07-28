package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-231097 — Holding use continues movement slowdown after the used item is dropped.
 *
 * <p>Forge already calls {@code stopUsingItem()} for the main-hand last/entire-stack drop path.
 * This mixin additionally routes through {@code MultiPlayerGameMode#releaseUsingItem} when use is
 * still active after {@code Inventory#removeFromSelected}, covering residual client use state and
 * ensuring the release packet is sent.
 */
@Mixin(LocalPlayer.class)
public abstract class UseSlowAfterDropMixin {

    @Shadow @Final protected Minecraft minecraft;

    @Shadow
    public abstract boolean isUsingItem();

    @Inject(
            method = "drop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER
            )
    )
    private void omnifix$releaseUseAfterDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (this.isUsingItem() && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.releaseUsingItem((LocalPlayer) (Object) this);
        }
    }
}
