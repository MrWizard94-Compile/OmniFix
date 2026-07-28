package org.omnifix.mixin.vanilla;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-119417 — A spectator can occupy a bed (and count toward night skip) after switching modes
 * while sleeping, and can otherwise remain "in bed" state.
 *
 * <p>Root cause: {@link ServerPlayer#setGameMode} does not wake the player, and
 * {@link ServerPlayer#startSleepInBed} does not reject spectators.
 */
@Mixin(ServerPlayer.class)
public abstract class SpectatorBedMixin {

    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
    private void omnifix$blockSpectatorSleep(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.isSpectator()) {
            cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
        }
    }

    @Inject(
            method = "setGameMode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;onUpdateAbilities()V"
            )
    )
    private void omnifix$wakeSpectatorFromBed(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        if (gameType != GameType.SPECTATOR) {
            return;
        }
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.isSleeping()) {
            // Wake without resetting the "time since rest" sleep timer (second arg false).
            self.stopSleepInBed(false, false);
        }
    }
}
