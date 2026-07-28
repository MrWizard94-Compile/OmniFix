package org.omnifix.mixin.perf;

import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

/**
 * ForgeRegistry.add scans nextClearBit for every entry — O(n) per registration.
 * Cache expected next free bit; invalidate on sync/clear/block.
 */
@Mixin(value = ForgeRegistry.class, remap = false)
public abstract class ForgeRegistryBitCacheMixin {

    @Unique
    private int omnifix$expectedNextBit = -1;

    @Redirect(
            method = "add(ILnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;Ljava/lang/String;)I",
            at = @At(value = "INVOKE", target = "Ljava/util/BitSet;nextClearBit(I)I")
    )
    private int omnifix$useCachedBit(BitSet availabilityMap, int minimum) {
        int bit = availabilityMap.nextClearBit(
                omnifix$expectedNextBit != -1 ? omnifix$expectedNextBit : minimum);
        omnifix$expectedNextBit = bit + 1;
        return bit;
    }

    @Inject(method = {"sync", "clear", "block"}, at = @At("HEAD"))
    private void omnifix$clearBitCache(CallbackInfo ci) {
        omnifix$expectedNextBit = -1;
    }

    @Redirect(
            method = "add(ILnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;Ljava/lang/String;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;trace(Lorg/apache/logging/log4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"
            ),
            require = 0
    )
    private void omnifix$skipTrace(
            Logger logger,
            Marker marker,
            String s,
            Object o,
            Object o1,
            Object o2,
            Object o3,
            Object o4
    ) {
        // no-op: per-registration TRACE is pure noise at scale
    }
}
