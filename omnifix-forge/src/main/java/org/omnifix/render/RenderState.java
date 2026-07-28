package org.omnifix.render;

/** Tracks whether GameRenderer is inside renderLevel (not GUI/HUD item render). */
public final class RenderState {
    public static boolean IS_RENDERING_LEVEL;

    private RenderState() {}
}
