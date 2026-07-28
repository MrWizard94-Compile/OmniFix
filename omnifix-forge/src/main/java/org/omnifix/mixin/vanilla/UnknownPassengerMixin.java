package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-90683 — "Received passengers for unknown entity" (historically "Received unknown passenger")
 * spam when vehicle/passenger entities straddle client render distance.
 *
 * <p>Root cause: {@link ClientPacketListener#handleSetEntityPassengersPacket} logs a warn whenever
 * the vehicle entity id is not yet present in the client level. Passengers outside the client's
 * entity tracking window make this a common, non-actionable race; the packet is already a no-op
 * when the vehicle is missing ({@code if (entity == null)}). Suppress only this method's warn so
 * other packet-listener diagnostics keep working. Soft {@code require = 0} if another mod rewrites
 * the passenger packet handler.
 */
@Mixin(ClientPacketListener.class)
public abstract class UnknownPassengerMixin {

    @WrapWithCondition(
            method = "handleSetEntityPassengersPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V",
                    remap = false
            ),
            require = 0
    )
    private boolean omnifix$suppressUnknownPassengerWarn(Logger logger, String message) {
        // Only this method has a single warn; keep the hook narrow and always suppress it.
        return false;
    }
}
