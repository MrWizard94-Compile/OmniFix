package org.omnifix.mixin.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * MC-197260 — Armor stand (and equipment) renders dark when its head is inside a solid block.
 *
 * <p>Root cause: entity lighting uses the block at the entity's feet. When an armor stand is
 * posed with its head in opaque geometry, that single sample is often light level 0. Take the
 * max packed light across feet-relative Y offsets so a free sample (below or above) can light
 * the stand.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class ArmorStandDarkMixin {

    private static final int[] Y_OFFSETS = {-1, 2, 3};

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int omnifix$armorStandNeighborLight(
            int packedLight,
            LivingEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLightArg
    ) {
        if (!(entity instanceof ArmorStand)) {
            return packedLight;
        }
        int best = packedLight;
        BlockPos base = entity.blockPosition();
        for (int dy : Y_OFFSETS) {
            BlockPos sample = base.offset(0, dy, 0);
            int samplePacked = LightTexture.pack(
                    entity.level().getBrightness(LightLayer.BLOCK, sample),
                    entity.level().getBrightness(LightLayer.SKY, sample)
            );
            if (samplePacked > best) {
                best = samplePacked;
            }
        }
        return best;
    }
}
