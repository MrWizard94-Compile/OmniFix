package org.omnifix.mixin.perf;

import net.minecraft.server.Bootstrap;
import net.minecraftforge.network.NetworkConstants;
import org.omnifix.classloading.ManifestCompactor;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;
import org.omnifix.load.ModWorkManagerQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Early bootstrap hooks: ModWorkManager park queue, jar manifest digests, Forge NetworkConstants classload.
 */
@Mixin(Bootstrap.class)
public abstract class BootstrapEarlyMixin {

    @Shadow
    private static boolean isBootstrapped;

    @Inject(method = "bootStrap", at = @At("HEAD"))
    private static void omnifix$earlyBootstrap(CallbackInfo ci) {
        if (isBootstrapped) {
            return;
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_MOD_WORK_QUEUE)) {
            ModWorkManagerQueue.replace();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_MANIFEST_COMPACT)) {
            ManifestCompactor.compactManifests();
        }
    }

    /**
     * Forge #9505 — force NetworkConstants class initialization during bootstrap so later
     * concurrent first-touches do not race ModLauncher.
     */
    @Inject(method = "bootStrap", at = @At("RETURN"))
    private static void omnifix$classloadNetworkConstants(CallbackInfo ci) {
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_NETWORK_CONSTANTS_INIT)) {
            NetworkConstants.init();
        }
    }
}
