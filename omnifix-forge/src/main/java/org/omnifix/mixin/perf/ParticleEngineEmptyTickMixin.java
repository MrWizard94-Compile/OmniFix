package org.omnifix.mixin.perf;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;

/**
 * Root cause: {@link ParticleEngine#tick} always iterates the particle render-type map and
 * pending queues even when nothing is active. Skipping the full tick when empty removes a
 * steady client hot path in quiet scenes.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineEmptyTickMixin {

    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;

    @Shadow
    @Final
    private Queue<TrackingEmitter> trackingEmitters;

    @Shadow
    @Final
    private Queue<Particle> particlesToAdd;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void omnifix$skipEmptyParticleTick(CallbackInfo ci) {
        if (!this.particlesToAdd.isEmpty() || !this.trackingEmitters.isEmpty()) {
            return;
        }
        for (Queue<Particle> queue : this.particles.values()) {
            if (queue != null && !queue.isEmpty()) {
                return;
            }
        }
        ci.cancel();
    }
}
