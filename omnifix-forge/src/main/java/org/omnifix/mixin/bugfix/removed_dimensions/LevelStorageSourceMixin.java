package org.omnifix.mixin.bugfix.removed_dimensions;

import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Allows worlds to load even when a dimension mod has been removed (Forge PR #8959 class fix).
 */
@Mixin(LevelStorageSource.class)
public class LevelStorageSourceMixin {
    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/DataResult;getOrThrow(ZLjava/util/function/Consumer;)Ljava/lang/Object;",
                    ordinal = 0),
            index = 0)
    private static boolean omnifix$alwaysAllowPartialDimensions(boolean flag) {
        return true;
    }
}
