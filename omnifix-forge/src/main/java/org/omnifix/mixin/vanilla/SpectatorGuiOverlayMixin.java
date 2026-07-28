package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client GUI overlays for spectator mode:
 * MC-215531 (pumpkin blur), MC-215530 (powder-snow freeze outline).
 */
@Mixin(Gui.class)
public abstract class SpectatorGuiOverlayMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "render", at = @At("HEAD"))
    private void omnifix$clearSpectatorFreezeTicks(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && player.isSpectator() && player.getTicksFrozen() > 0) {
            player.setTicksFrozen(0);
        }
    }

    /**
     * {@link Gui#renderTextureOverlay} draws pumpkin blur and powder-snow outline.
     * Skip both while spectating (pumpkin is armor-slot driven and must not be stripped).
     */
    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void omnifix$skipSpectatorTextureOverlays(GuiGraphics graphics, ResourceLocation texture, float alpha, CallbackInfo ci) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && player.isSpectator()) {
            ci.cancel();
        }
    }
}
