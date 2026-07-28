package org.omnifix.mixin.vanilla;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-129909 (mode switch path) — cancel active item use when entering spectator so eating does
 * not continue for a few ticks after F3+F4 / {@code /gamemode spectator}.
 */
@Mixin(ServerPlayer.class)
public abstract class SpectatorConsumeGameModeMixin {

    @Inject(
            method = "setGameMode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;stopRiding()V"
            )
    )
    private void omnifix$stopUsingOnSpectator(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        // stopRiding is only invoked on the SPECTATOR branch of setGameMode (1.20.1).
        ServerPlayer self = (ServerPlayer) (Object) this;
        self.stopUsingItem();
    }
}
