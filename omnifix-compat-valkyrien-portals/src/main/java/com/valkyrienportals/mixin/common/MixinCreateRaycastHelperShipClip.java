package com.valkyrienportals.mixin.common;

import com.simibubi.create.foundation.utility.RaycastHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

/**
 * Clockwork wanderwand (and other Create tools) call {@link RaycastHelper#rayTraceRange}, which
 * uses {@link Level#clip}. VS already attempts to ship-aware clip Level, but tool selection still
 * fails on ship blocks when clip paths miss or short-circuit.
 *
 * <p>Root cause: vanilla-space ray must consult ship colliders via {@code clipIncludeShips}.
 * Redirect Create's helper explicitly so wanderwand select/bind hit ship blocks.
 */
@Mixin(value = RaycastHelper.class, remap = false)
public abstract class MixinCreateRaycastHelperShipClip {

    @Redirect(
            method = "rayTraceRange",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;",
                    remap = true
            ),
            require = 0
    )
    private static BlockHitResult omnifix$clipIncludeShips(Level level, ClipContext context) {
        return RaycastUtilsKt.clipIncludeShips(level, context);
    }
}
