package org.omnifix.mixin.vanilla;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-81773 — Server/common path: finishing a use-item (bow/trident release) while spectator still
 * spawns projectiles.
 *
 * <p>Cancels {@link LivingEntity#releaseUsingItem()} for spectators and stops the use instead so
 * items never finish. Client companion: {@link SpectatorProjectileClientMixin}.
 */
@Mixin(LivingEntity.class)
public abstract class SpectatorProjectileMixin {

    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void omnifix$cancelReleaseInSpectator(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player && player.isSpectator()) {
            // stopUsingItem cancels without finishing (does not call releaseUsingItem).
            self.stopUsingItem();
            ci.cancel();
        }
    }
}
