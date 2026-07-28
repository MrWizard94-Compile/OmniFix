package org.omnifix.mixin.net;

import net.minecraft.network.Varint21FrameDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla Netty framing reads at most 3 length bytes ({@code 21}-bit = {@code 2_097_151} max frame).
 * After custom-payload / encode ceilings are raised, poorly compressible large packets can exceed
 * that wire size and throw {@code CorruptedFrameException: length wider than 21-bit}.
 *
 * <p>Raises the length-byte budget from 3 → 5 (full Minecraft varint int width), allowing frames up
 * to {@code Integer.MAX_VALUE} in theory; practical caps remain the 16&nbsp;MiB payload/encode
 * limits. Symmetric raise lives in {@link Varint21LengthFieldPrependerSizeMixin}.
 */
@Mixin(Varint21FrameDecoder.class)
public abstract class Varint21FrameSizeMixin {

    private static final int VANILLA_MAX_BYTES = 3;
    private static final int OMNIFIX_MAX_BYTES = 5;

    @ModifyConstant(method = "decode", constant = @Constant(intValue = VANILLA_MAX_BYTES), require = 0)
    private int omnifix$raiseFrameLengthBytes(int original) {
        return original == VANILLA_MAX_BYTES ? OMNIFIX_MAX_BYTES : original;
    }
}
