package org.omnifix.mixin.perf;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Avoid O(n) byId list growth on every registry registration.
 */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryGrowMixin {

    @Redirect(
            method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectList;size(I)V",
                    remap = false
            )
    )
    private void omnifix$growPowerOfTwo(ObjectList<?> list, int size) {
        if (list instanceof ObjectArrayList && size > list.size()) {
            int requested = size;
            int p2 = Integer.highestOneBit(size);
            if (p2 != size) {
                size = p2 << 1;
            }
            ((ObjectArrayList<?>) list).ensureCapacity(size);
            while (list.size() < requested) {
                list.add(null);
            }
        } else {
            list.size(size);
        }
    }
}
