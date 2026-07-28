package org.omnifix.mixin.vanilla;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-183776 — After opening the gamemode switcher with F3+F4, F3 must be pressed twice to toggle
 * the debug overlay.
 *
 * <p>Root cause: {@link KeyboardHandler#handleDebugKeys} returns {@code true} after opening
 * {@code GameModeSwitcherScreen}, so {@code handledDebugKey} stays set and the F3 release path
 * does not toggle the overlay. Returning {@code false} after {@code setScreen} matches other
 * non-consuming debug paths and restores single-press F3 behaviour.
 */
@Mixin(KeyboardHandler.class)
public abstract class F3DoubleMixin {

    @Inject(
            method = "handleDebugKeys",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void omnifix$f3F4DoesNotConsumeDebugKey(int key, CallbackInfoReturnable<Boolean> cir) {
        // Only the F3+F4 branch opens a screen from handleDebugKeys on 1.20.1.
        cir.setReturnValue(false);
    }
}
