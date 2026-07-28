package org.omnifix.mixin.net;

import net.minecraft.network.Varint21LengthFieldPrepender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Send-side twin of {@link Varint21FrameSizeMixin}: vanilla rejects frames whose varint length
 * encoding needs more than 3 bytes ({@code unable to fit X into 3}). Raised to 5 bytes so large
 * custom payloads that pass encode/compression can still be framed.
 */
@Mixin(Varint21LengthFieldPrepender.class)
public abstract class Varint21LengthFieldPrependerSizeMixin {

    private static final int VANILLA_MAX_BYTES = 3;
    private static final int OMNIFIX_MAX_BYTES = 5;

    @ModifyConstant(method = "encode", constant = @Constant(intValue = VANILLA_MAX_BYTES), require = 0)
    private int omnifix$raisePrependerLengthBytes(int original) {
        return original == VANILLA_MAX_BYTES ? OMNIFIX_MAX_BYTES : original;
    }
}
