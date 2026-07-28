package org.omnifix.entity;

/**
 * Attribute supplier interning is valuable during entity type registration (boot) but not worth
 * the lock overhead for late rebuilds after registries settle. Flip after common setup.
 */
public final class AttributeSupplierLaunchGate {

    private static volatile boolean launchComplete;

    private AttributeSupplierLaunchGate() {}

    public static boolean isLaunchComplete() {
        return launchComplete;
    }

    public static void markLaunchComplete() {
        launchComplete = true;
    }
}
