package org.omnifix.mixin.vanilla;

import net.minecraft.client.renderer.entity.FishingHookRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * MC-4490 — Fishing line is not attached to the rod in third person while crouching.
 *
 * <p>Root cause: {@link FishingHookRenderer#render} applies a fixed Y offset of {@code -0.1875}
 * when the owner is crouching. Player crouch pose lowers the held rod further than that constant,
 * so the line endpoint hangs above the rod tip. Use a deeper crouch offset so the line meets the
 * rod in third person.
 */
@Mixin(FishingHookRenderer.class)
public abstract class FishingLineCrouchMixin {

    @ModifyConstant(method = "render", constant = @Constant(floatValue = -0.1875F))
    private float omnifix$deeperCrouchLineOffset(float original) {
        return -0.2875F;
    }
}
