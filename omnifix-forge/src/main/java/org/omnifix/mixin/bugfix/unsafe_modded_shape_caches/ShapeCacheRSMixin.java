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
 * Some mods use a custom shape cache with a non-thread-safe map. There is no reason why this wouldn't cause crashes
 * in vanilla as well if getShape was called on two threads at once. We solve it by making the map thread-safe.
 *
 * <p>Optional target: applied only when Refined Storage is present (plugin gate) and silently skipped if the
 * class shape differs ({@code require = 0}). Raw Map type is required so Mixin AP can process this {@code @Pseudo}
 * mixin when the target is not on the compile classpath.
 */
@Pseudo
@Mixin(targets = "com.refinedmods.refinedstorage.block.shape.ShapeCache", remap = false)
public class ShapeCacheRSMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Raw Map: Pseudo target is not compile-visible; parameterized shadows crash Mixin AP field validation.
    @Shadow
    @Final
    @Mutable
    private static Map CACHE = new ConcurrentHashMap();

    @Inject(method = "<clinit>", at = @At("RETURN"), require = 0)
    private static void omnifix$notify(CallbackInfo ci) {
        LOGGER.info("Made Refined Storage shape cache map thread-safe");
    }
}
