package org.omnifix.kernel;

/**
 * Detected modpack profile that drives which FeatureUnits and compat layers are active.
 */
public enum StackProfile {
    /** Valkyrien Skies + Immersive Portals + Embeddium-class renderer (Base Wars). */
    HEAVY_PHYSICS_PORTAL,
    /** Generic Forge performance stack without VS/IP hard requirements. */
    GENERIC,
}