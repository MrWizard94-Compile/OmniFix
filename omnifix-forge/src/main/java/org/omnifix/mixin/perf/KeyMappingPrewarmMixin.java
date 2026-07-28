package org.omnifix.mixin.perf;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Resolve KeyMapping translated components on the main thread when search trees are built,
 * so later off-thread access does not hit GLFW/lazy load and crash.
 */
@Mixin(Minecraft.class)
public abstract class KeyMappingPrewarmMixin {

    @Inject(method = "createSearchTrees", at = @At("RETURN"))
    private void omnifix$prewarmKeyMappings(CallbackInfo ci) {
        GLFWErrorCallback oldCb = GLFW.glfwSetErrorCallback(null);
        try {
            for (KeyMapping mapping : KeyMapping.ALL.values()) {
                mapping.getTranslatedKeyMessage();
            }
        } finally {
            GLFW.glfwSetErrorCallback(oldCb);
        }
    }
}
