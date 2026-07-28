package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-93384 — Drowning bubble particles spawn at the entity's feet instead of the head.
 *
 * <p>Root cause: on Forge 1.20.1, vanilla {@link LivingEntity#baseTick} drowning is short-circuited
 * ({@code if (false)}) and handled by {@link ForgeHooks#onLivingBreathe}, which places bubbles at
 * {@code getY() + random} without eye height. Offset particle Y by {@link LivingEntity#getEyeHeight()}.
 */
@Mixin(ForgeHooks.class)
public abstract class DrownBubblesMixin {

    @WrapOperation(
            method = "onLivingBreathe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private static void omnifix$drownBubblesAtEyes(
            Level level,
            ParticleOptions options,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            Operation<Void> original,
            LivingEntity entity,
            int consumeAirAmount,
            int refillAirAmount
    ) {
        original.call(level, options, x, y + (double) entity.getEyeHeight(), z, xSpeed, ySpeed, zSpeed);
    }
}
