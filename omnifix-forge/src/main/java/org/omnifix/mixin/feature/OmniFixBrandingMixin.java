package org.omnifix.mixin.feature;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.internal.BrandingControl;
import org.omnifix.kernel.OmniFixConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

/**
 * Client: appends {@code OmniFix <version>} to Forge {@link BrandingControl} so F3 / brand lists
 * show that OmniFix is active.
 */
@Mixin(value = BrandingControl.class, remap = false, priority = 1100)
public class OmniFixBrandingMixin {

    @Inject(
            method = "computeBranding",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/ModList;get()Lnet/minecraftforge/fml/ModList;"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 0)
    private static void omnifix$addBranding(CallbackInfo ci, ImmutableList.Builder<String> builder) {
        Optional<? extends ModContainer> container =
                ModList.get().getModContainerById(OmniFixConstants.MOD_ID);
        if (container.isPresent()) {
            builder.add("OmniFix " + container.get().getModInfo().getVersion().toString());
        }
    }
}
