package org.omnifix.mixin.perf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.omnifix.duck.IDeferrableIntegratedServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Consume OmniFix client-load sentinel and release the integrated server tick gate on the client
 * thread (after earlier packets have finished applying).
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerSuspendMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void omnifix$detectClientLoadSentinel(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.getIdentifier().equals(IDeferrableIntegratedServer.CLIENT_LOAD_SENTINEL)) {
            this.minecraft.executeIfPossible(() -> {
                packet.getData().release();
                if (this.minecraft.hasSingleplayerServer()) {
                    ((IDeferrableIntegratedServer) this.minecraft.getSingleplayerServer())
                            .omnifix$markClientLoadFinished();
                }
            });
            ci.cancel();
        }
    }
}
