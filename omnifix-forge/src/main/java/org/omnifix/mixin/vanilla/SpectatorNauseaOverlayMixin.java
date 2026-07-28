package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-217716 — nausea (confusion) overlay while in spectator.
 * Server clears the effect; this skips residual client-side intensity frames.
 */
@Mixin(GameRenderer.class)
public abstract class SpectatorNauseaOverlayMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderConfusionOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void omnifix$skipSpectatorNausea(GuiGraphics graphics, float intensity, CallbackInfo ci) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && player.isSpectator()) {
            ci.cancel();
        }
    }
}
