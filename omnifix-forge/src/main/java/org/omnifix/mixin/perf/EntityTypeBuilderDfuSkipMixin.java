package org.omnifix.mixin.perf;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Skip fetchChoiceType during entity build so DFU is not force-loaded at bootstrap. */
@Mixin(EntityType.Builder.class)
public abstract class EntityTypeBuilderDfuSkipMixin {

    @Redirect(
            method = "build",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/Util;fetchChoiceType(Lcom/mojang/datafixers/DSL$TypeReference;Ljava/lang/String;)Lcom/mojang/datafixers/types/Type;"
            )
    )
    private Type<?> omnifix$skipSchemaCheck(DSL.TypeReference ref, String s) {
        return null;
    }
}
