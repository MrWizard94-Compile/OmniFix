package org.omnifix.mixin.vanilla;

import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.client.telemetry.TelemetryEventSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Complements {@link TelemetryDisableMinecraftMixin}: never create a live telemetry sender.
 */
@Mixin(value = ClientTelemetryManager.class, priority = 1500)
public abstract class TelemetryDisableSenderMixin {

    @Inject(method = "createEventSender", at = @At("HEAD"), cancellable = true)
    private void omnifix$disabledSender(CallbackInfoReturnable<TelemetryEventSender> cir) {
        cir.setReturnValue(TelemetryEventSender.DISABLED);
    }
}
