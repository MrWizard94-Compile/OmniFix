package org.omnifix.mixin.net;

import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla {@link ClientboundCustomPayloadPacket} enforces {@code MAX_PAYLOAD_SIZE = 1_048_576}
 * (1&nbsp;MiB) on both the send constructor ({@code writerIndex} check) and the decode constructor
 * ({@code readableBytes} check). Forge {@code SimpleChannel} / custom mod channels ride this packet
 * type, so large-pack registry/config dumps throw
 * {@code IllegalArgumentException: Payload may not be larger than 1048576 bytes}.
 *
 * <p>Raises both ceilings to 16&nbsp;MiB to match {@code net.compression_size}. Independent
 * reimplementation from the public vanilla constant only.
 */
@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadSizeMixin {

    private static final int VANILLA_MAX = 1_048_576;
    private static final int OMNIFIX_MAX = 16_777_216; // 16 MiB

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = VANILLA_MAX), require = 0)
    private int omnifix$raiseClientboundPayloadLimit(int original) {
        return original == VANILLA_MAX ? OMNIFIX_MAX : original;
    }
}
