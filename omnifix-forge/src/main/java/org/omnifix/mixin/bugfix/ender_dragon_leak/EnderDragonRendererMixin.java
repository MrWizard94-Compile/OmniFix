package org.omnifix.mixin.bugfix.ender_dragon_leak;

import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clears the dragon model entity reference after render so a previous client world cannot leak
 * through the retained EnderDragon instance (access transformer makes {@code entity} public).
 */
@Mixin(EnderDragonRenderer.class)
public abstract class EnderDragonRendererMixin {
    @Shadow
    @Final
    private EnderDragonRenderer.DragonModel model;

    /**
     * Prevent leaking the client world through the entity reference.
     */
    @Inject(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FF"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void omnifix$clearDragonEntityReference(CallbackInfo ci) {
        this.model.entity = null;
    }
}
