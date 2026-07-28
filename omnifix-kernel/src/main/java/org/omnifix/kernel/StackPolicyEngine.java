package org.omnifix.kernel;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Resolves which OmniFix domains are active for the detected pack profile.
 * Platform code registers mod-presence probes before {@link #resolve()} runs.
 */
public final class StackPolicyEngine {

    private static final Set<StackDomain> ACTIVE = EnumSet.noneOf(StackDomain.class);
    private static StackProfile profile = StackProfile.GENERIC;
    private static boolean diagnostics = Boolean.getBoolean("omnifix.diagnostics");

    private StackPolicyEngine() {}

    public static void registerProbe(StackDomain domain, BooleanSupplier probe) {
        if (probe.getAsBoolean()) {
            ACTIVE.add(domain);
        }
    }

    public static void setProfile(StackProfile resolved) {
        profile = resolved;
    }

    public static StackProfile getProfile() {
        return profile;
    }

    public static boolean isDomainActive(StackDomain domain) {
        return ACTIVE.contains(domain);
    }

    public static boolean isDiagnosticsEnabled() {
        return diagnostics;
    }

    public static void resolve() {
        if (ACTIVE.contains(StackDomain.VALKYRIEN_SKIES)
                && ACTIVE.contains(StackDomain.IMMERSIVE_PORTALS)) {
            profile = StackProfile.HEAVY_PHYSICS_PORTAL;
        } else {
            profile = StackProfile.GENERIC;
        }
    }
}