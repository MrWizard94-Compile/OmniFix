package org.omnifix.mixin.vanilla;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-100991 — Killing a mob by reeling it into lethal damage with a fishing rod does not credit
 * the player (scoreboard kills / statistics / combat tracker).
 *
 * <p>Root cause: {@link FishingHook#pullEntity} only applies velocity; it never records damage
 * attributed to the rod owner. Record a zero-amount thrown combat entry (and last-hurt-by player)
 * at the setDeltaMovement inject point so a subsequent lethal fall/impact credits the angler.
 */
@Mixin(FishingHook.class)
public abstract class FishingKillCountMixin {

    @Inject(
            method = "pullEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void omnifix$creditFishingPull(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        FishingHook self = (FishingHook) (Object) this;
        Entity owner = self.getOwner();
        living.getCombatTracker().recordDamage(
                self.level().damageSources().thrown(self, owner),
                living.getHealth()
        );
        if (owner instanceof Player player) {
            living.setLastHurtByPlayer(player);
        }
    }
}
