package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.bytes.ByteList;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.omnifix.render.font.CompactUnihexContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.font.providers.UnihexProvider$ShortContents")
public abstract class UnihexShortContentsCompactMixin {

    @Inject(
            method = "read",
            at = @At(
                    value = "NEW",
                    target = "([S)Lnet/minecraft/client/gui/font/providers/UnihexProvider$ShortContents;"
            ),
            cancellable = true
    )
    private static void omnifix$useCompact(
            int index,
            ByteList byteList,
            CallbackInfoReturnable<UnihexProvider.LineData> cir,
            @Local(ordinal = 0) short[] contents
    ) {
        if (contents.length == 16) {
            cir.setReturnValue(new CompactUnihexContents.Shorts(contents));
        }
    }
}
