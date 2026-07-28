package org.omnifix.mixin.perf;

import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.omnifix.chunk.ExtendedPalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerPaletteMixin<T> implements ExtendedPalettedContainer<T> {

    @Shadow
    private volatile PalettedContainer.Data<T> data;

    @Override
    public Palette<T> omnifix$getPalette() {
        return this.data.palette();
    }
}
