package org.omnifix.mixin.net;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla disconnects clients that take longer than 30s (600 ticks) in the login listener.
 * Large Forge packs routinely exceed that during registry/mod channel negotiation.
 *
 * <p>Raises the budget to 120 seconds (2400 ticks). Connectivity-class fix; complete and local.
 */
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginTimeoutMixin {

    private static final int VANILLA_LOGIN_TICKS = 600;
    private static final int OMNIFIX_LOGIN_TICKS = 2400;

    @ModifyConstant(method = "tick", constant = @Constant(intValue = VANILLA_LOGIN_TICKS), require = 0)
    private int omnifix$extendLoginTimeout(int original) {
        return original == VANILLA_LOGIN_TICKS ? OMNIFIX_LOGIN_TICKS : original;
    }
}
