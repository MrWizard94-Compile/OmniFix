package org.omnifix.mixin.bugfix.unsafe_modded_shape_caches;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe replacement for Cyclic's shape cache map. See {@link ShapeCacheRSMixin}.
 *
 * <p>Optional target: applied only when Cyclic is present (plugin gate) and silently skipped if the
 * class shape differs ({@code require = 0}).
 */
@Pseudo
@Mixin(targets = "com.lothrazar.cyclic.block.cable.ShapeCache", remap = false)
public class ShapeCacheCyclicMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Raw Map: Pseudo target is not compile-visible; parameterized shadows crash Mixin AP field validation.
    @Shadow
    @Final
    @Mutable
    private static Map CACHE = new ConcurrentHashMap();

    @Inject(method = "<clinit>", at = @At("RETURN"), require = 0)
    private static void omnifix$notify(CallbackInfo ci) {
        LOGGER.info("Made Cyclic shape cache map thread-safe");
    }
}
