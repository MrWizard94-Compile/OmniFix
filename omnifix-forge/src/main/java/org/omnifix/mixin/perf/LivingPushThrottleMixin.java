package org.omnifix.mixin.perf;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: every {@link LivingEntity} server tick calls {@link LivingEntity#pushEntities},
 * which runs {@code Level#getEntities} over the living AABB. Under dense farms (mob grinders,
 * breeders, iron farms) that scan dominates entity collision cost even when nothing is
 * co-located or pushable.
 *
 * <p>Throttle: on the <em>server</em>, non-player living entities skip {@code pushEntities} on
 * every other tick ({@code tickCount & 1 != 0}). Players always run full-rate so player–entity
 * collision feel is unchanged. Client is left alone — the client path already only scans
 * {@link Player}s, so server-only throttling is the high-value half.
 *
 * <p><b>Trade-off:</b> mob–mob / mob–entity push resolution and entity-cramming checks for
 * non-players run at half rate (~1 tick lag). Lithium-class mild behavioural change; cramming
 * itself is already probabilistic in vanilla ({@code random.nextInt(4) == 0}).
 */
@Mixin(LivingEntity.class)
public abstract class LivingPushThrottleMixin {

    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttlePushEntities(CallbackInfo ci) {
        // Players always full-rate (player collision feel)
        if ((Object) this instanceof Player) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        // Server-side non-players: every other tick
        if (!self.level().isClientSide && (self.tickCount & 1) != 0) {
            ci.cancel();
        }
    }
}
