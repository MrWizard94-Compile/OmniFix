package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-55347 — Title / subtitle overlays persist after disconnect or world change.
 *
 * <p>Root cause: {@link Minecraft#clearLevel(Screen)} resets narrator and game mode but never calls
 * {@link Gui#clear()}, so {@code title}/{@code subtitle}/{@code titleTime} survive into the next
 * session until their timers naturally expire.
 */
@Mixin(Minecraft.class)
public abstract class TitleClearMixin {

    @Shadow @Final public Gui gui;

    @Inject(
            method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void omnifix$clearTitleOnLeaveWorld(Screen screen, CallbackInfo ci) {
        if (this.gui != null) {
            this.gui.clear();
        }
    }
}
