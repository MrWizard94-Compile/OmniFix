package org.omnifix.mixin.vanilla;

import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks chat open so {@link LinuxChatTCharMixin} can drop the spurious first character.
 */
@Mixin(ChatScreen.class)
public abstract class LinuxChatTOpenMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$markChatOpened(String initial, CallbackInfo ci) {
        LinuxChatTHelper.onChatScreenOpened();
    }
}
