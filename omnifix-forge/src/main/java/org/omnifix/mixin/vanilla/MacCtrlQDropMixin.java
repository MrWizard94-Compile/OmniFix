package org.omnifix.mixin.vanilla;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MC-22882 — On macOS, {@link Screen#hasControlDown()} maps to Super/Cmd. Drop-all
 * ({@code player.drop(true)}) therefore requires Cmd+Q, which the OS also uses to quit.
 *
 * <p>For the drop keybind path only, use physical Ctrl keys on OSX so Ctrl+Q drops the stack.
 */
@Mixin(Minecraft.class)
public abstract class MacCtrlQDropMixin {

    @Redirect(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;hasControlDown()Z",
                    ordinal = 0
            )
    )
    private boolean omnifix$macDropUsesPhysicalCtrl() {
        if (Minecraft.ON_OSX) {
            long window = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                    || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
        }
        return Screen.hasControlDown();
    }
}
