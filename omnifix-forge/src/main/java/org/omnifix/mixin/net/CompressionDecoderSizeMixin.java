package org.omnifix.mixin.net;

import net.minecraft.network.CompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla {@link CompressionDecoder} enforces hard ceilings on compressed-packet handling.
 *
 * <p>On 1.20.1 (official mappings):
 * <ul>
 *   <li>{@code MAXIMUM_COMPRESSED_LENGTH = 2_097_152} (2&nbsp;MiB) — public field; may be inlined
 *       by remappers / other injectors</li>
 *   <li>{@code MAXIMUM_UNCOMPRESSED_LENGTH = 8_388_608} (8&nbsp;MiB) — the active check inside
 *       {@code decode} when {@code validateDecompressed} is true</li>
 * </ul>
 *
 * Large Forge packs routinely exceed these limits (registry dumps, recipe graphs), causing
 * {@code DecoderException} disconnects.
 *
 * <p>Both constants are raised to 16&nbsp;MiB — enough for heavy packs without unbounded memory
 * risk. Connectivity-class symptom; reimplemented from public vanilla constants only
 * (Connectivity is ARR — do not copy its sources).
 */
@Mixin(CompressionDecoder.class)
public abstract class CompressionDecoderSizeMixin {

    private static final int VANILLA_COMPRESSED_MAX = 2_097_152;
    private static final int VANILLA_UNCOMPRESSED_MAX = 8_388_608;
    private static final int OMNIFIX_MAX = 16_777_216; // 16 MiB

    /**
     * Raise the inlined 2&nbsp;MiB constant if present (field / older decode paths).
     */
    @ModifyConstant(method = "decode", constant = @Constant(intValue = VANILLA_COMPRESSED_MAX), require = 0)
    private int omnifix$raiseCompressedSizeLimit(int original) {
        return original == VANILLA_COMPRESSED_MAX ? OMNIFIX_MAX : original;
    }

    /**
     * Raise the active 8&nbsp;MiB uncompressed validation ceiling used by 1.20.1 {@code decode}.
     */
    @ModifyConstant(method = "decode", constant = @Constant(intValue = VANILLA_UNCOMPRESSED_MAX), require = 0)
    private int omnifix$raiseUncompressedSizeLimit(int original) {
        return original == VANILLA_UNCOMPRESSED_MAX ? OMNIFIX_MAX : original;
    }
}
