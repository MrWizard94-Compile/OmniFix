package org.omnifix.mixin.perf;

import net.minecraft.world.entity.vehicle.MinecartHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: when {@link net.minecraft.world.level.block.entity.HopperBlockEntity#suckInItems}
 * finds nothing above, {@link MinecartHopper#suckInItems} still runs a second
 * {@code getEntitiesOfClass} on the minecart AABB every tick. Empty farms re-scan constantly.
 *
 * <p>Same-tick empty memo per minecart entity id (independent of the block-hopper empty cache).
 */
@Mixin(MinecartHopper.class)
public abstract class MinecartHopperEmptyCacheMixin {

    @Unique
    private long omnifix$emptyAtGameTime = Long.MIN_VALUE;

    @Inject(method = "suckInItems", at = @At("HEAD"), cancellable = true)
    private void omnifix$skipKnownEmpty(CallbackInfoReturnable<Boolean> cir) {
        MinecartHopper self = (MinecartHopper) (Object) this;
        if (self.level().isClientSide) {
            return;
        }
        if (self.level().getGameTime() == this.omnifix$emptyAtGameTime) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "suckInItems", at = @At("RETURN"))
    private void omnifix$rememberEmpty(CallbackInfoReturnable<Boolean> cir) {
        MinecartHopper self = (MinecartHopper) (Object) this;
        if (self.level().isClientSide) {
            return;
        }
        if (!cir.getReturnValueZ()) {
            this.omnifix$emptyAtGameTime = self.level().getGameTime();
        } else {
            this.omnifix$emptyAtGameTime = Long.MIN_VALUE;
        }
    }
}
