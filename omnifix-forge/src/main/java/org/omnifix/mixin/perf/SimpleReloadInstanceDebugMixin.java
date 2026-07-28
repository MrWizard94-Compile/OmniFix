package org.omnifix.mixin.perf;

import net.minecraft.server.packs.resources.SimpleReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Enable profiled resource reloads via system property {@code omnifix.debugReloader=true}
 * (same role as ModernFix's modernfix.debugReloader).
 */
@Mixin(SimpleReloadInstance.class)
public abstract class SimpleReloadInstanceDebugMixin {

    private static final boolean OMNIFIX$DEBUG_RELOADER = Boolean.getBoolean("omnifix.debugReloader");

    @ModifyVariable(method = "create", at = @At("HEAD"), argsOnly = true)
    private static boolean omnifix$enableDebugReloader(boolean profiled) {
        return profiled || OMNIFIX$DEBUG_RELOADER;
    }
}
