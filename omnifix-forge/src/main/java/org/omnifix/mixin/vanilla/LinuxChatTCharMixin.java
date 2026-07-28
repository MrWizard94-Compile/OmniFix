package org.omnifix.mixin.vanilla;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-122477 — On some Linux desktops, GLFW delivers key-press and char events on separate polls.
 * Chat can open on the key poll, then the char poll types {@code t} or {@code /} into the field.
 * Suppress the first char after ChatScreen construction while that screen is still active.
 */
@Mixin(KeyboardHandler.class)
public abstract class LinuxChatTCharMixin {

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void omnifix$suppressOpenChatChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ChatScreen)) {
            return;
        }
        if (LinuxChatTHelper.consumeSuppressedChar()) {
            ci.cancel();
        }
    }
}
