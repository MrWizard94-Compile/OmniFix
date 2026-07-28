package org.omnifix.mixin.perf;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Skip fetchChoiceType during BE register so DFU is not force-loaded at bootstrap. */
@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeDfuSkipMixin {

    @Redirect(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/Util;fetchChoiceType(Lcom/mojang/datafixers/DSL$TypeReference;Ljava/lang/String;)Lcom/mojang/datafixers/types/Type;"
            )
    )
    private static Type<?> omnifix$skipSchemaCheck(DSL.TypeReference ref, String s) {
        return null;
    }
}
