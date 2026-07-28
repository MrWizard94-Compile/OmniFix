package org.omnifix.mixin.vanilla;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-206922 — Items dropped by entities killed by lightning are destroyed by the same bolt
 * almost immediately.
 *
 * <p>Root cause: {@link Entity#thunderHit} runs for every entity in the bolt AABB, including
 * freshly spawned {@link ItemEntity} drops. Cancel lightning processing for item entities that
 * are only a few ticks old so kill drops survive. Applied on {@link Entity} because
 * {@code ItemEntity} does not override {@code thunderHit} on 1.20.1.
 */
@Mixin(Entity.class)
public abstract class LightningDropsMixin {

    /** Vanilla drop entities are hit by the bolt in the same tick / few ticks of spawn. */
    private static final int IMMUNE_TICKS = 8;

    @Shadow public int tickCount;

    @Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
    private void omnifix$protectFreshDropsFromLightning(ServerLevel level, LightningBolt bolt, CallbackInfo ci) {
        if ((Object) this instanceof ItemEntity && this.tickCount <= IMMUNE_TICKS) {
            ci.cancel();
        }
    }
}
