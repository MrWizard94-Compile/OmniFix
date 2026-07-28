package org.omnifix.chunk;

import net.minecraft.world.level.chunk.Palette;

public interface ExtendedPalettedContainer<T> {
    Palette<T> omnifix$getPalette();
}
