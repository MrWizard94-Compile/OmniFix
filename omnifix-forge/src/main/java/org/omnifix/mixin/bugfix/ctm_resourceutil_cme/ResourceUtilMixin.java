package org.omnifix.mixin.bugfix.ctm_resourceutil_cme;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * CTM {@code ResourceUtil.metadataCache} is a plain map touched from multiple threads → rare CME.
 * Synchronize after class init when not already concurrent.
 */
@Pseudo
@Mixin(targets = "team.chisel.ctm.client.util.ResourceUtil", remap = false)
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ResourceUtilMixin {

    @Shadow
    @Final
    @Mutable
    private static Map metadataCache;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void omnifix$syncMetadataCache(CallbackInfo ci) {
        if (metadataCache != null && !(metadataCache instanceof ConcurrentMap)) {
            metadataCache = Collections.synchronizedMap(metadataCache);
        }
    }
}
