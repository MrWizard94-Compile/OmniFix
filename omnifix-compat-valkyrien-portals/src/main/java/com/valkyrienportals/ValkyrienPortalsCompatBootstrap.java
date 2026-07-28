package com.valkyrienportals;

import com.mojang.logging.LogUtils;
import com.valkyrienportals.transit.PortalShipTransit;
import com.valkyrienportals.transit.PortalShipVisibility;
import net.minecraftforge.common.MinecraftForge;
import org.omnifix.kernel.StackDomain;
import org.omnifix.kernel.StackPolicyEngine;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;
import org.slf4j.Logger;

/**
 * Valkyrien Skies + Immersive Portals compat layer (ported from the standalone
 * {@code valkyrienportals} mod into OmniFix). The original project at
 * {@code C:/WPAI/Gaming/Minecraft/Mods-1.20.1-Forge/Valkyrien Portals} is left untouched.
 */
public final class ValkyrienPortalsCompatBootstrap {

    public static final String LEGACY_MOD_ID = "valkyrienportals";

    private static final Logger LOGGER = LogUtils.getLogger();

    private ValkyrienPortalsCompatBootstrap() {}

    public static void init() {
        if (!StackPolicyEngine.isDomainActive(StackDomain.VALKYRIEN_SKIES)
                || !StackPolicyEngine.isDomainActive(StackDomain.IMMERSIVE_PORTALS)) {
            LOGGER.info("[OmniFix/VP] VS+IP compat inactive (missing dependency).");
            return;
        }

        // Server half of the VS+IP layer: remote-ship visibility through portals and ship transit.
        // Registered here (not @Mod.EventBusSubscriber) so the classes — which import VS/IP types —
        // are only ever loaded when both mods are confirmed present and the FeatureUnit is enabled.
        if (FeatureUnitRegistry.isActive(FeatureUnits.VP_SHIP_VIS)) {
            MinecraftForge.EVENT_BUS.register(PortalShipVisibility.class);
            LOGGER.info("[OmniFix/VP] Portal ship visibility handler registered.");
        }
        if (FeatureUnitRegistry.isActive(FeatureUnits.VP_SHIP_TRANSIT)) {
            MinecraftForge.EVENT_BUS.register(PortalShipTransit.class);
            LOGGER.info("[OmniFix/VP] Portal ship transit handler registered.");
        }

        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.VP_FRUSTUM)) {
            if (classPresent("com.bawnorton.mixinsquared.api.MixinCanceller")) {
                LOGGER.info("[OmniFix/VP] MixinSquared present; frustum dead-loop canceller SPI is available.");
            } else {
                LOGGER.error("[OmniFix/VP] MixinSquared is NOT on the classpath. OmniFix jar-in-jars it; "
                        + "if you stripped nested jars, restore them or the VS/IP frustum boot crash will occur.");
            }
        } else {
            LOGGER.info("[OmniFix/VP] FeatureUnit {} disabled; frustum canceller will no-op if invoked.",
                    FeatureUnits.VP_FRUSTUM);
        }
    }

    private static boolean classPresent(String binaryName) {
        try {
            Class.forName(binaryName, false, ValkyrienPortalsCompatBootstrap.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
