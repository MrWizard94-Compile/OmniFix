package org.omnifix.mixin.vanilla;

import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * MC-199467 — Certain entity animations (bee/bat wings, etc.) freeze after long world age.
 *
 * <p>Root cause: {@link Mth#sin}/{@link Mth#cos} index a 65536-entry LUT with
 * {@code (int)(radians * scale)}. When {@code radians} grows with {@code tickCount} over long
 * lifetimes, float mantissa precision can no longer resolve successive angles, so the LUT index
 * sticks. Reduce the argument modulo {@link Mth#TWO_PI} so the input stays in a precise range.
 */
@Mixin(Mth.class)
public abstract class EntityAnimFreezeMixin {

    @ModifyVariable(method = "sin", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static float omnifix$wrapSinRadians(float value) {
        return value % Mth.TWO_PI;
    }

    @ModifyVariable(method = "cos", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static float omnifix$wrapCosRadians(float value) {
        return value % Mth.TWO_PI;
    }
}
