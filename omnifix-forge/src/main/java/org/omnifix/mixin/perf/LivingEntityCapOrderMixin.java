package org.omnifix.mixin.perf;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

/**
 * LivingEntity.getCapability checks isAlive() before capability identity for ITEM_HANDLER.
 * isAlive() is relatively expensive (indirection); equality-check the capability first and only
 * call isAlive when the requested cap is ITEM_HANDLER.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityCapOrderMixin {

    @Redirect(
            method = "getCapability",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isAlive()Z"
            )
    )
    private <T> boolean omnifix$checkAliveAfterCap(
            LivingEntity entity,
            Capability<T> capability,
            @Nullable Direction facing
    ) {
        return capability == ForgeCapabilities.ITEM_HANDLER && entity.isAlive();
    }
}
