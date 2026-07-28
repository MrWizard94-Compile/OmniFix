package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import org.objectweb.asm.Opcodes;
import org.omnifix.render.font.LazyGlyphProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Lazy soft-load unihex glyph providers via codec wrap. */
@Mixin(GlyphProviderType.class)
public abstract class GlyphProviderTypeLazyMixin {

    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/minecraft/client/gui/font/providers/UnihexProvider$Definition;CODEC:Lcom/mojang/serialization/MapCodec;"
            )
    )
    private static MapCodec<? extends GlyphProviderDefinition> omnifix$lazyUnihex(
            MapCodec<? extends GlyphProviderDefinition> codec
    ) {
        return LazyGlyphProvider.wrap(codec);
    }
}
