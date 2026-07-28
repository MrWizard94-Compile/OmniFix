package org.omnifix.mixin.net;

import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Login-phase S2C custom queries ({@link ClientboundCustomQueryPacket}) share the same 1&nbsp;MiB
 * {@code MAX_PAYLOAD_SIZE} as play custom payloads. Forge handshake / {@code LoginWrapper} traffic
 * that exceeds the limit disconnects during mod negotiation on large packs.
 *
 * <p>Raises the decode ceiling to 16&nbsp;MiB.
 */
@Mixin(ClientboundCustomQueryPacket.class)
public abstract class ClientboundCustomQuerySizeMixin {

    private static final int VANILLA_MAX = 1_048_576;
    private static final int OMNIFIX_MAX = 16_777_216; // 16 MiB

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            constant = @Constant(intValue = VANILLA_MAX),
            require = 0)
    private int omnifix$raiseLoginQueryPayloadLimit(int original) {
        return original == VANILLA_MAX ? OMNIFIX_MAX : original;
    }
}
