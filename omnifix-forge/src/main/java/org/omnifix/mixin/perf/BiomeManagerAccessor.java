package org.omnifix.mixin.perf;

import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeManager.class)
public interface BiomeManagerAccessor {
    @Accessor("biomeZoomSeed")
    long omnifix$getZoomSeed();

    @Accessor("noiseBiomeSource")
    BiomeManager.NoiseBiomeSource omnifix$getBiomeSource();
}
