package org.omnifix.mixin.net;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla {@link ServerGamePacketListenerImpl} keep-alive uses {@code LATENCY_CHECK_INTERVAL}
 * of 15&nbsp;000&nbsp;ms: after that interval with a pending challenge, the player is kicked with
 * {@code disconnect.timeout}.
 *
 * <p>Large-pack play sessions (heavy chunk/registry traffic, main-thread freezes) routinely miss
 * the 15&nbsp;s reply window even when the connection is healthy. Login and Netty read timeouts
 * are handled by {@link ServerLoginTimeoutMixin} / {@link ReadTimeoutHandlerMixin}; this unit
 * covers the play-phase keep-alive specifically.
 *
 * <p>Raises the interval to 60&nbsp;s. Connectivity-class symptom; reimplemented from the public
 * vanilla constant only.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePlayTimeoutMixin {

    private static final long VANILLA_KEEPALIVE_MS = 15_000L;
    private static final long OMNIFIX_KEEPALIVE_MS = 60_000L;

    @ModifyConstant(method = "tick", constant = @Constant(longValue = VANILLA_KEEPALIVE_MS), require = 0)
    private long omnifix$raiseKeepAliveInterval(long original) {
        return original == VANILLA_KEEPALIVE_MS ? OMNIFIX_KEEPALIVE_MS : original;
    }
}
