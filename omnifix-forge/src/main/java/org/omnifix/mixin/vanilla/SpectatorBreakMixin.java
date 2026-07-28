package org.omnifix.mixin.vanilla;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-46766 — Switching to spectator mid-mine leaves the block-break crack overlay and hit sound
 * loop active.
 *
 * <p>Root cause: {@link MultiPlayerGameMode#setLocalMode} updates abilities but never clears
 * in-progress destroy state ({@code isDestroying}/{@code destroyProgress}). Calling
 * {@link MultiPlayerGameMode#stopDestroyBlock()} resets progress and {@code destroyBlockProgress}.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class SpectatorBreakMixin {

    @Shadow
    public abstract void stopDestroyBlock();

    @Inject(method = "setLocalMode(Lnet/minecraft/world/level/GameType;)V", at = @At("TAIL"))
    private void omnifix$stopDestroyOnSpectator(GameType type, CallbackInfo ci) {
        if (type == GameType.SPECTATOR) {
            this.stopDestroyBlock();
        }
    }

    @Inject(
            method = "setLocalMode(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V",
            at = @At("TAIL")
    )
    private void omnifix$stopDestroyOnSpectatorDual(GameType local, GameType previous, CallbackInfo ci) {
        if (local == GameType.SPECTATOR) {
            this.stopDestroyBlock();
        }
    }
}
