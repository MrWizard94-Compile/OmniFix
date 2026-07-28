package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-30391 — Chickens, blazes, and the wither spawn land particles despite falling slowly.
 *
 * <p>Root cause: {@link LivingEntity#checkFallDamage} spawns block impact particles when
 * {@code fallDistance > 3}, on ground, and the landed block is not air. Slow-fall entities
 * (chicken glide, blaze hover, wither flight) still accumulate fall distance and trip that
 * branch when they finally touch a block.
 *
 * <p>Fix: treat the landed block as air for those entity types so the particle branch is skipped
 * without changing soul-speed / damage fall handling on other entities.
 */
@Mixin(LivingEntity.class)
public abstract class SlowFallParticlesMixin {

    @ModifyExpressionValue(
            method = "checkFallDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"
            )
    )
    private boolean omnifix$treatSlowFallAsAir(boolean isAir) {
        LivingEntity self = (LivingEntity) (Object) this;
        return isAir
                || self instanceof Chicken
                || self instanceof Blaze
                || self instanceof WitherBoss;
    }
}
