package org.omnifix.mixin.vanilla;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-14923 — Integrated / singleplayer servers still run {@code detectRateSpam} and can disconnect
 * the owner with {@code disconnect.spam} (ops are exempt; the SP host is not always treated as op).
 *
 * <p>Root cause: {@link ServerGamePacketListenerImpl#detectRateSpam} only skips the kick for ops,
 * not for singleplayer. Cancel the entire rate-spam path on integrated servers.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class SpChatSpamMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "detectRateSpam", at = @At("HEAD"), cancellable = true)
    private void omnifix$noChatSpamKickOnIntegratedServer(CallbackInfo ci) {
        if (this.server.isSingleplayer()) {
            ci.cancel();
        }
    }
}
