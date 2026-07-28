package org.omnifix.mixin.vanilla;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-263865 — Pressing F11 toggles fullscreen and updates the in-memory option, but never writes
 * {@code options.txt}. Relaunching Minecraft restores the previous fullscreen state.
 *
 * <p>Root cause: {@link KeyboardHandler#keyPress} handles {@code keyFullscreen} with
 * {@code window.toggleFullScreen()} + {@code options.fullscreen().set(...)} and returns without
 * {@link net.minecraft.client.Options#save()}. The video-settings screen path saves; the hotkey path
 * does not.
 */
@Mixin(KeyboardHandler.class)
public abstract class FullscreenStateMixin {

    @Shadow @Final private Minecraft minecraft;

    /**
     * Persist options immediately after the fullscreen hotkey toggles the window.
     * Soft require: key-handling shapes vary under some input mods.
     */
    @Inject(
            method = "keyPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/Window;toggleFullScreen()V",
                    shift = At.Shift.AFTER
            ),
            require = 0
    )
    private void omnifix$saveFullscreenAfterHotkey(long windowHandle, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (this.minecraft != null && this.minecraft.options != null) {
            // toggleFullScreen is only used on the F11/keyFullscreen path in keyPress (1.20.1).
            this.minecraft.options.save();
        }
    }
}
