package org.omnifix.mixin.vanilla;

import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Policy unit: treat chat as SECURE so unsigned/modded messages do not paint untrusted UI.
 * Toggleable via {@code feature.chat_signing_off}.
 */
@Mixin(ChatTrustLevel.class)
public abstract class ChatSigningOffMixin {

    @Inject(method = "evaluate", at = @At("HEAD"), cancellable = true)
    private static void omnifix$alwaysSecure(CallbackInfoReturnable<ChatTrustLevel> cir) {
        cir.setReturnValue(ChatTrustLevel.SECURE);
    }
}
