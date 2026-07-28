package org.omnifix.mixin.net;

import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla {@link ServerboundCustomPayloadPacket} decode path enforces
 * {@code MAX_PAYLOAD_SIZE = 32_767}. Client→server Forge channel messages that exceed that throw
 * {@code IllegalArgumentException: Payload may not be larger than 32767 bytes}.
 *
 * <p>Raises the ceiling to 16&nbsp;MiB (aligned with clientbound / compression units). Independent
 * reimplementation from the public vanilla constant only.
 */
@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class ServerboundCustomPayloadSizeMixin {

    private static final int VANILLA_MAX = 32_767;
    private static final int OMNIFIX_MAX = 16_777_216; // 16 MiB

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            constant = @Constant(intValue = VANILLA_MAX),
            require = 0)
    private int omnifix$raiseServerboundPayloadLimit(int original) {
        return original == VANILLA_MAX ? OMNIFIX_MAX : original;
    }
}
