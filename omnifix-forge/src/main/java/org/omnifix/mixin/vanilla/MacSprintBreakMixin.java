package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * MC-59810 — Cannot break/attack while sprinting on macOS.
 *
 * <p>Root cause: {@link MouseHandler#onPress} contains intentional OSX logic that remaps
 * left-click + control-modifier to right-click (GUI context-menu emulation). Default sprint is
 * Ctrl, so sprint+left becomes right-click and never starts a break/attack.
 *
 * <p>Fix: while in-world (no screen/overlay), strip {@link GLFW#GLFW_MOD_CONTROL} from the mouse
 * event mods so the remapper never fires. Keyboard still sees Ctrl for sprint. GUI screens keep
 * the vanilla Ctrl+left → right conversion for context menus.
 */
@Mixin(MouseHandler.class)
public abstract class MacSprintBreakMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(method = "onPress", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int omnifix$stripControlModInWorld(int mods) {
        if (!Minecraft.ON_OSX) {
            return mods;
        }
        if (this.minecraft.screen == null && this.minecraft.getOverlay() == null) {
            return mods & ~GLFW.GLFW_MOD_CONTROL;
        }
        return mods;
    }
}
