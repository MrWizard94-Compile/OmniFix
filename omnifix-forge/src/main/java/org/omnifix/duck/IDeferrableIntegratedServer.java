package org.omnifix.duck;

import net.minecraft.resources.ResourceLocation;
import org.omnifix.kernel.OmniFixConstants;

/**
 * Integrated server can avoid full ticks until the client finishes applying join-time packets
 * (recipes, tags, …). A sentinel custom payload marks that point.
 */
public interface IDeferrableIntegratedServer {

    ResourceLocation CLIENT_LOAD_SENTINEL =
            new ResourceLocation(OmniFixConstants.MOD_ID, "mark_client_load_finished");

    void omnifix$markClientLoadFinished();
}
