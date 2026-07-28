package org.omnifix.mixin.perf;

import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.omnifix.duck.IDeferrableIntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After placeNewPlayer finishes queueing join packets on a memory (integrated) connection, send
 * a sentinel so the client can resume full integrated-server ticks.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListSuspendMixin {

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void omnifix$sendConfigFinishedSentinel(Connection connection, ServerPlayer player, CallbackInfo ci) {
        if (connection.isMemoryConnection()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            player.connection.send(
                    new ClientboundCustomPayloadPacket(IDeferrableIntegratedServer.CLIENT_LOAD_SENTINEL, buf));
        }
    }
}
