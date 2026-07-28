package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.omnifix.render.FastItemRenderType;
import org.omnifix.render.RenderState;
import org.omnifix.render.SimpleItemModelView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GUI item render of vanilla SimpleBakedModel only needs camera-facing quads.
 */
@Mixin(value = ItemRenderer.class, priority = 600)
public abstract class ItemRendererFastGuiMixin {

    @Unique
    private ItemDisplayContext omnifix$transformType;

    @Unique
    private final SimpleItemModelView omnifix$modelView = new SimpleItemModelView();

    @Inject(method = "render", at = @At("HEAD"))
    private void omnifix$markRenderingType(
            ItemStack itemStack,
            ItemDisplayContext transformType,
            boolean leftHand,
            PoseStack matrixStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            BakedModel model,
            CallbackInfo ci
    ) {
        this.omnifix$transformType = transformType;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
            ),
            index = 0
    )
    private BakedModel omnifix$useSimpleWrappedItemModel(
            BakedModel model,
            ItemStack stack,
            int combinedLight,
            int combinedOverlay,
            PoseStack matrixStack,
            VertexConsumer buffer,
            @Local(ordinal = 0) BakedModel originalModel
    ) {
        if (originalModel != null && originalModel.getClass() != SimpleBakedModel.class) {
            return model;
        }
        if (!RenderState.IS_RENDERING_LEVEL
                && !stack.isEmpty()
                && model.getClass() == SimpleBakedModel.class
                && omnifix$transformType == ItemDisplayContext.GUI) {
            FastItemRenderType type;
            ItemTransform transform = model.getTransforms().gui;
            if (transform == ItemTransform.NO_TRANSFORM) {
                type = FastItemRenderType.SIMPLE_ITEM;
            } else if (stack.getItem() instanceof BlockItem && omnifix$isBlockTransforms(transform)) {
                type = FastItemRenderType.SIMPLE_BLOCK;
            } else {
                return model;
            }
            omnifix$modelView.setItem(model);
            omnifix$modelView.setType(type);
            return omnifix$modelView;
        }
        return model;
    }

    @Unique
    private static boolean omnifix$isBlockTransforms(ItemTransform transform) {
        return transform.rotation.x() == 30f
                && transform.rotation.y() == 225f
                && transform.rotation.z() == 0f;
    }
}
