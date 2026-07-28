package org.omnifix.mixin.bugfix;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clears Forge debug overlay chunk caches when leaving a client level so F3 text does not retain
 * section futures / world references after disconnect.
 */
@Mixin(Minecraft.class)
public abstract class DebugOverlayClearMixin {

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void omnifix$clearDebugOnLeave(ClientLevel level, CallbackInfo ci) {
        if (level != null) {
            return;
        }
        Minecraft self = (Minecraft) (Object) this;
        if (!(self.gui instanceof ForgeGui forgeGui)) {
            return;
        }
        try {
            DebugScreenOverlay overlay = ObfuscationReflectionHelper.getPrivateValue(
                    ForgeGui.class, forgeGui, "debugOverlay");
            if (overlay != null) {
                overlay.clearChunkCache();
            }
        } catch (RuntimeException ignored) {
            // Field rename / absent overlay — soft fail.
        }
    }
}
