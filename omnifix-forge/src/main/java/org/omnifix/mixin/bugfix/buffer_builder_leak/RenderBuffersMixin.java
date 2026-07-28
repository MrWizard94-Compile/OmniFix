package org.omnifix.mixin.bugfix.buffer_builder_leak;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents leaking BufferBuilders when {@code put} is called for a RenderType that is already
 * present (e.g. signSheet vs hangingSignSheet sharing identity in 1.20.1).
 */
@Mixin(RenderBuffers.class)
public class RenderBuffersMixin {
    /**
     * @author embeddedt (ModernFix) / OmniFix port
     * @reason put() may be called for multiple instances of the same render type. This leaks the
     * previous BufferBuilder if one is already in the map.
     */
    @Inject(method = "put", at = @At("HEAD"), cancellable = true)
    private static void omnifix$preventBufferLeak(
            Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> mapBuilders,
            RenderType renderType,
            CallbackInfo ci) {
        if (mapBuilders.containsKey(renderType)) {
            ci.cancel();
        }
    }
}
