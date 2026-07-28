package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.omnifix.entity.AttributeInstanceTemplates;
import org.omnifix.entity.AttributeSupplierLaunchGate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Canonicalize identical AttributeInstance templates while entity suppliers are built at launch.
 */
@Mixin(AttributeSupplier.Builder.class)
public abstract class AttributeSupplierBuilderDedupMixin {

    @Shadow
    @Final
    private Map<Attribute, AttributeInstance> builder;

    @Inject(
            method = "build",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/util/Map;)Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier;"
            )
    )
    private void omnifix$deduplicateInstances(CallbackInfoReturnable<AttributeSupplier> cir) {
        if (AttributeSupplierLaunchGate.isLaunchComplete()) {
            return;
        }
        this.builder.replaceAll((a, i) -> AttributeInstanceTemplates.intern(i));
    }
}
