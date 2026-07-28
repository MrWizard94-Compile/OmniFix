package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-237493 — telemetry cannot be fully disabled. Force {@code allowsTelemetry} false for modded clients.
 */
@Mixin(value = Minecraft.class, priority = 1100)
public abstract class TelemetryDisableMinecraftMixin {

    @Inject(method = "allowsTelemetry", at = @At("HEAD"), cancellable = true)
    private void omnifix$noTelemetry(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
