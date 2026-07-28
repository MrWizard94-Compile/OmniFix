package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-105068 — Blocking with a shield still plays the entity's normal hurt sound.
 *
 * <p>Root cause: {@link LivingEntity#hurt} fully blocks damage via {@code isDamageSourceBlocked}
 * ({@code flag == true}) and broadcasts entity event 29 (shield block sound), but still calls
 * {@link LivingEntity#playHurtSound} whenever {@code flag1} is set. Skip the hurt sound when the
 * hit was fully blocked so only the shield block sound is heard.
 */
@Mixin(LivingEntity.class)
public abstract class ShieldHurtSoundMixin {

    @WrapWithCondition(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;playHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)V"
            )
    )
    private boolean omnifix$skipHurtSoundWhenFullyBlocked(
            LivingEntity self,
            DamageSource source,
            @Local(ordinal = 0) boolean fullyBlocked
    ) {
        return !fullyBlocked;
    }
}
