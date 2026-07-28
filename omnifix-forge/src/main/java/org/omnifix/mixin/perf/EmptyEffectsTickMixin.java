package org.omnifix.mixin.perf;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Root cause: {@link LivingEntity#tickEffects} always walks {@code activeEffects}. When the map is
 * empty and {@code effectsDirty} is false, the walk and dirty-handler are pure no-ops — cancel.
 *
 * <p>When effects are removed, vanilla sets {@code effectsDirty} so invisibility/glow update still
 * runs for one tick before this skip engages.
 */
@Mixin(LivingEntity.class)
public abstract class EmptyEffectsTickMixin {

    @Shadow
    @Final
    private Map<MobEffect, MobEffectInstance> activeEffects;

    @Shadow
    private boolean effectsDirty;

    @Inject(method = "tickEffects", at = @At("HEAD"), cancellable = true)
    private void omnifix$skipEmptyEffects(CallbackInfo ci) {
        if (this.activeEffects.isEmpty() && !this.effectsDirty) {
            ci.cancel();
        }
    }
}
