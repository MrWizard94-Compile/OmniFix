package org.omnifix.mixin.perf;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * AttributeSupplier stores instances in Guava ImmutableMap with heavy wrappers. Replace with a
 * compact OpenHashMap — insertion order is irrelevant for attribute lookup.
 */
@Mixin(AttributeSupplier.class)
public abstract class AttributeSupplierCompactMixin {

    @Shadow
    @Final
    @Mutable
    private Map<Attribute, AttributeInstance> instances;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$useCompactMap(Map<Attribute, AttributeInstance> instances, CallbackInfo ci) {
        this.instances = new Object2ObjectOpenHashMap<>(this.instances);
    }
}
