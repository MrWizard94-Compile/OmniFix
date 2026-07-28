package org.omnifix.mixin.vanilla;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Client-side companion to {@link SpectatorStuckEffectsMixin}.
 *
 * <p>Mirrors server cleanup on the local player when the client is notified of a SPECTATOR
 * game-mode change (MC-206705, MC-215530, MC-217716, MC-193343, MC-119754).
 */
@Mixin(LocalPlayer.class)
public abstract class SpectatorStuckEffectsClientMixin {

    /** Same UUID as {@code LivingEntity.SPEED_MODIFIER_SOUL_SPEED_UUID}. */
    private static final UUID SOUL_SPEED_UUID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");

    @Inject(method = "onGameModeChanged", at = @At("TAIL"))
    private void omnifix$clearClientStuckState(GameType gameType, CallbackInfo ci) {
        if (gameType != GameType.SPECTATOR) {
            return;
        }
        LocalPlayer self = (LocalPlayer) (Object) this;

        self.stopUsingItem();
        self.setTicksFrozen(0);
        self.removeEffect(MobEffects.CONFUSION);

        AttributeInstance movement = self.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null && movement.getModifier(SOUL_SPEED_UUID) != null) {
            movement.removeModifier(SOUL_SPEED_UUID);
        }

        if (self.isFallFlying()) {
            self.stopFallFlying();
        }
        self.setDeltaMovement(Vec3.ZERO);
    }
}
