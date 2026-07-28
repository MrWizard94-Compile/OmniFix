package org.omnifix.mixin.vanilla;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Clears "stuck" player state when entering spectator mode.
 *
 * <p>Covers Mojira:
 * <ul>
 *   <li>MC-215531 — carved pumpkin overlay remains in spectator</li>
 *   <li>MC-217716 — nausea (confusion) overlay remains in spectator</li>
 *   <li>MC-215530 — powder-snow freeze overlay remains in spectator</li>
 *   <li>MC-193343 — soul speed attribute remains in spectator</li>
 *   <li>MC-206705 — spyglass use continues in spectator</li>
 *   <li>MC-119754 — elytra firework boost continues in spectator</li>
 *   <li>MC-69216 — fishing rod cast remains active in spectator</li>
 * </ul>
 *
 * <p>Root cause: {@link ServerPlayer#setGameMode} updates abilities and dismounts, but never
 * cancels active item use, freeze ticks, status effects, soul-speed modifiers, fall-flying, or
 * fishing hooks. Client overlays (pumpkin helmet, freeze, nausea) then keep rendering from leftover
 * state. Clearing on the SPECTATOR transition is the single root-cause fix.
 */
@Mixin(ServerPlayer.class)
public abstract class SpectatorStuckEffectsMixin {

    /** Same UUID as {@code LivingEntity.SPEED_MODIFIER_SOUL_SPEED_UUID}. */
    private static final UUID SOUL_SPEED_UUID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");

    @Inject(
            method = "setGameMode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;onUpdateAbilities()V"
            )
    )
    private void omnifix$clearStuckStateOnSpectator(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        if (gameType != GameType.SPECTATOR) {
            return;
        }

        ServerPlayer self = (ServerPlayer) (Object) this;

        // MC-206705: spyglass / any using-item visual
        self.stopUsingItem();

        // MC-215530: powder snow freeze overlay / ticks
        self.setTicksFrozen(0);

        // MC-217716: nausea overlay is driven by the CONFUSION effect
        self.removeEffect(MobEffects.CONFUSION);

        // MC-193343: soul speed transient modifier
        AttributeInstance movement = self.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null && movement.getModifier(SOUL_SPEED_UUID) != null) {
            movement.removeModifier(SOUL_SPEED_UUID);
        }

        // MC-119754: stop elytra glide + kill residual firework boost velocity
        if (self.isFallFlying()) {
            self.stopFallFlying();
        }
        self.setDeltaMovement(Vec3.ZERO);

        // MC-69216: reel in / discard cast fishing hook
        FishingHook hook = self.fishing;
        if (hook != null) {
            hook.discard();
        }

        // MC-215531: pumpkin is armor-slot driven on the client; zeroing use + freeze + effects
        // covers server-synced state. Client Gui mixin skips the helmet overlay for spectators.
    }
}
