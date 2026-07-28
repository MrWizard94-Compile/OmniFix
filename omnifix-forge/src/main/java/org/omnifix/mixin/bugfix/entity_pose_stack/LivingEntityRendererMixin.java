package org.omnifix.mixin.bugfix.entity_pose_stack;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Restores pose-stack balance if a mod cancels {@link RenderLivingEvent.Pre} after pushing poses
 * (Forge #9118 class fix).
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z",
                    ordinal = 0))
    private boolean omnifix$fireCheckingPoseStack(IEventBus instance, Event event) {
        if (!(event instanceof RenderLivingEvent<?, ?> livingEvent)) {
            return instance.post(event);
        }
        PoseStack stack = livingEvent.getPoseStack();
        int size = ((PoseStackAccessor) stack).getPoseStack().size();
        if (instance.post(event)) {
            // Pop the stack if someone pushed it in the event
            while (((PoseStackAccessor) stack).getPoseStack().size() > size) {
                stack.popPose();
            }
            return true;
        }
        return false;
    }
}
