package org.omnifix.compat.createportals.mixin;

import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.Portal;

/**
 * Seated train passengers must not be stolen by IP while Create re-seats them on the far-side
 * dimensional carriage. Without this, riders double-teleport or fall through the portal mouth.
 */
@Mixin(Portal.class)
public abstract class MixinPortalBlockTrainPassengers {

    @Inject(
            method = "canTeleportEntity",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void omnifix$blockTrainPassengers(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Entity vehicle = entity.getVehicle();
        if (vehicle instanceof CarriageContraptionEntity) {
            cir.setReturnValue(false);
        }
    }
}
