package org.omnifix.mixin.vanilla;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-81773 — Client path: mouse-up after entering spectator still sends RELEASE_USE_ITEM and
 * finishes the held use-item via {@link MultiPlayerGameMode#releaseUsingItem}.
 *
 * <p>Stop the use locally and skip the packet / finish so bows and tridents do not fire.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class SpectatorProjectileClientMixin {

    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void omnifix$cancelClientReleaseInSpectator(Player player, CallbackInfo ci) {
        if (player != null && player.isSpectator()) {
            player.stopUsingItem();
            ci.cancel();
        }
    }
}
