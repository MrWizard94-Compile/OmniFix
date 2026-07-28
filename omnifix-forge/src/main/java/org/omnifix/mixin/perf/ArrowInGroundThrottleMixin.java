package org.omnifix.mixin.perf;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: arrows stuck {@code inGround} still run a full {@link AbstractArrow#tick} every
 * server tick (collision, water, crit particles path). Under arrow farms that is pure waste for
 * the majority of lifetime until despawn/pickup.
 *
 * <p>Throttle: on server, while in ground, cancel every other tick after the first in-ground
 * tick so despawn counters and nearby pickup still advance on intervening ticks.
 */
@Mixin(AbstractArrow.class)
public abstract class ArrowInGroundThrottleMixin {

    @Shadow
    protected boolean inGround;

    @Shadow
    protected int inGroundTime;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleInGroundTick(CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (self.level().isClientSide) {
            return;
        }
        if (!this.inGround) {
            return;
        }
        // Keep first in-ground tick and every other thereafter for despawn/pickup cadence.
        if (this.inGroundTime > 0 && (this.inGroundTime & 1) == 1) {
            // Still advance inGroundTime so despawn math stays aligned with wall time roughly:
            // when we skip a full tick we manually bump the counter once.
            this.inGroundTime++;
            ci.cancel();
        }
    }
}
