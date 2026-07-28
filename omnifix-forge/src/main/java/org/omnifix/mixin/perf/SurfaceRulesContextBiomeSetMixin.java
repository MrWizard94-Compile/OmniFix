package org.omnifix.mixin.perf;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.Nullable;
import org.omnifix.worldgen.ExtendedSurfaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(SurfaceRules.Context.class)
public abstract class SurfaceRulesContextBiomeSetMixin implements ExtendedSurfaceContext {

    @Unique
    @Nullable
    private Set<ResourceKey<Biome>> omnifix$possibleBiomes;

    @Override
    public void omnifix$applyPossibleBiomes() {
        this.omnifix$possibleBiomes = ExtendedSurfaceContext.COMPUTED_POSSIBLE_BIOMES.get();
        ExtendedSurfaceContext.COMPUTED_POSSIBLE_BIOMES.remove();
    }

    @Override
    public @Nullable Set<ResourceKey<Biome>> omnifix$getPossibleBiomes() {
        return this.omnifix$possibleBiomes;
    }
}
