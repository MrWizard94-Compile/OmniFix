package org.omnifix.mixin.net;

import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Login-phase C2S custom query responses decode through a private lambda that hard-caps payload
 * length at 1&nbsp;MiB ({@code MAX_PAYLOAD_SIZE}). Large Forge handshake replies trip
 * {@code Payload may not be larger than 1048576 bytes} during login.
 *
 * <p>Raises the lambda ceiling to 16&nbsp;MiB. Targets {@code lambda$new$0} as present in official
 * 1.20.1 bytecode.
 */
@Mixin(ServerboundCustomQueryPacket.class)
public abstract class ServerboundCustomQuerySizeMixin {

    private static final int VANILLA_MAX = 1_048_576;
    private static final int OMNIFIX_MAX = 16_777_216; // 16 MiB

    /**
     * The 1&nbsp;MiB check lives only in the constructor's private static lambda
     * ({@code lambda$new$0} on official 1.20.1). Method name is not SRG-mapped; use
     * {@code remap = false} and a broad method match so AP/runtime both resolve it.
     */
    @ModifyConstant(
            method = {"lambda$new$0", "*"},
            constant = @Constant(intValue = VANILLA_MAX),
            require = 0,
            remap = false)
    private static int omnifix$raiseLoginQueryReplyLimit(int original) {
        return original == VANILLA_MAX ? OMNIFIX_MAX : original;
    }
}
