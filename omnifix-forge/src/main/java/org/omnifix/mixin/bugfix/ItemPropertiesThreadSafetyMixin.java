package org.omnifix.mixin.bugfix;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;

/** Concurrent-safe ItemProperties maps for multi-threaded mod registration. */
@Mixin(value = ItemProperties.class, priority = 700)
public abstract class ItemPropertiesThreadSafetyMixin {

    @Shadow
    @Final
    @Mutable
    private static Map<ResourceLocation, ItemPropertyFunction> GENERIC_PROPERTIES;

    @Shadow
    @Final
    @Mutable
    private static Map<Item, Map<ResourceLocation, ItemPropertyFunction>> PROPERTIES;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void omnifix$useConcurrentMaps(CallbackInfo ci) {
        GENERIC_PROPERTIES = Collections.synchronizedMap(GENERIC_PROPERTIES);
        PROPERTIES = Collections.synchronizedMap(PROPERTIES);
    }
}
