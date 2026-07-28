package org.omnifix.mixin.net;

import net.minecraft.network.PacketEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Companion ceiling for custom-payload size raises: {@link PacketEncoder} rejects any serialized
 * packet body larger than 8&nbsp;MiB ({@code 8_388_608}) with
 * {@code IllegalArgumentException: Packet too big}.
 *
 * <p>Without this raise, lifting {@code ClientboundCustomPayloadPacket} past 8&nbsp;MiB is useless.
 * Aligns with {@code net.compression_size} / {@code net.payload_split} at 16&nbsp;MiB.
 */
@Mixin(PacketEncoder.class)
public abstract class PacketEncoderSizeMixin {

    private static final int VANILLA_MAX = 8_388_608;
    private static final int OMNIFIX_MAX = 16_777_216; // 16 MiB

    @ModifyConstant(method = "encode", constant = @Constant(intValue = VANILLA_MAX), require = 0)
    private int omnifix$raiseEncodeSizeLimit(int original) {
        return original == VANILLA_MAX ? OMNIFIX_MAX : original;
    }
}
