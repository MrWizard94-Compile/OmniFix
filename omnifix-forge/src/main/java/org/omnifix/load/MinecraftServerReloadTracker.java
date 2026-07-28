package org.omnifix.load;

/** Tracks overlapping server resource reloads so ingredient fast-paths can respect tag bind timing. */
public final class MinecraftServerReloadTracker {

    public static int ACTIVE_RELOADS;

    private MinecraftServerReloadTracker() {}

    public static boolean isReloadActive() {
        return ACTIVE_RELOADS > 0;
    }
}
