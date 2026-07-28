package org.omnifix.mixin.vanilla;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-132878 — Armor stands destroyed by explosion/fire do not show breaking plank particles.
 *
 * <p>Root cause: the player-break path calls {@code showBreakingParticles()} before kill, but
 * explosion ({@code hurt → brokenByAnything}) and fire ({@code causeDamage → brokenByAnything})
 * skip that call. Invoke the particle helper on those paths (soft require for inject stability).
 */
@Mixin(ArmorStand.class)
public abstract class ArmorStandDeathParticlesMixin {

    @Shadow
    protected abstract void showBreakingParticles();

    @Inject(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/decoration/ArmorStand;brokenByAnything(Lnet/minecraft/world/damagesource/DamageSource;)V"
            ),
            require = 0
    )
    private void omnifix$particlesOnExplosionBreak(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.showBreakingParticles();
    }

    @Inject(
            method = "causeDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/decoration/ArmorStand;brokenByAnything(Lnet/minecraft/world/damagesource/DamageSource;)V"
            ),
            require = 0
    )
    private void omnifix$particlesOnFireBreak(DamageSource source, float amount, CallbackInfo ci) {
        this.showBreakingParticles();
    }
}
