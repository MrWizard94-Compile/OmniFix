package org.omnifix.mixin.perf;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Root cause: {@link LivingEntity#activeEffects} is a plain HashMap consulted every tick in
 * {@code tickEffects}/add/remove. Reference-keyed open hashing is a better fit for identity-keyed
 * {@link MobEffect} keys under dense entity counts.
 */
@Mixin(LivingEntity.class)
public abstract class EffectsOpenHashMapMixin {

    @Shadow
    @Final
    @Mutable
    private Map<MobEffect, MobEffectInstance> activeEffects;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$useOpenHashEffects(EntityType<? extends LivingEntity> type, Level level, CallbackInfo ci) {
        this.activeEffects = new Reference2ObjectOpenHashMap<>(this.activeEffects);
    }
}
