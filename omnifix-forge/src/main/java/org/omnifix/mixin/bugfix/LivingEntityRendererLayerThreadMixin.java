package org.omnifix.mixin.bugfix;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mods that call addLayer off-thread cause CMEs; defer to the client thread.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererLayerThreadMixin<T extends Entity, M extends EntityModel<T>> {

    @Unique
    private static final Logger OMNIFIX$LOGGER = LogUtils.getLogger();

    @Shadow
    public abstract boolean addLayer(RenderLayer<T, M> layer);

    @WrapMethod(method = "addLayer")
    private boolean omnifix$handleOffThreadLayerAdd(RenderLayer<T, M> layer, Operation<Boolean> original) {
        if (!Minecraft.getInstance().isSameThread()) {
            OMNIFIX$LOGGER.error("[OmniFix] LivingEntityRenderer.addLayer called on wrong thread", new Exception());
            Minecraft.getInstance().tell(() -> this.addLayer(layer));
            return true;
        }
        return original.call(layer);
    }
}
