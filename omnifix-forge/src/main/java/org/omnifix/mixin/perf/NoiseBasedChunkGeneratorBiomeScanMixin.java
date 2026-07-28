package org.omnifix.mixin.perf;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.omnifix.chunk.ExtendedPalettedContainer;
import org.omnifix.worldgen.ExtendedSurfaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.NoSuchElementException;
import java.util.Set;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorBiomeScanMixin {

    @Unique
    @SuppressWarnings("unchecked")
    private static void omnifix$accumulate(Set<ResourceKey<Biome>> chunkBiomes, LevelChunkSection section) {
        var palette = ((ExtendedPalettedContainer<Holder<Biome>>) section.getBiomes()).omnifix$getPalette();
        if (palette.getSize() == 1) {
            chunkBiomes.add(palette.valueFor(0).unwrapKey().orElseThrow());
        } else {
            section.getBiomes().getAll(holder -> chunkBiomes.add(holder.unwrapKey().orElseThrow()));
        }
    }

    @Unique
    private static Set<ResourceKey<Biome>> omnifix$obtainBiomes(WorldGenRegion region, int chunkRadius) {
        Set<ResourceKey<Biome>> chunkBiomes = new ReferenceOpenHashSet<>();
        var center = region.getCenter();
        for (int z = center.z - chunkRadius; z <= center.z + chunkRadius; z++) {
            for (int x = center.x - chunkRadius; x <= center.x + chunkRadius; x++) {
                var chunk = region.getChunk(x, z);
                for (var section : chunk.getSections()) {
                    omnifix$accumulate(chunkBiomes, section);
                }
            }
        }
        return chunkBiomes;
    }

    @Inject(
            method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/blending/Blender;)V"
            )
    )
    private void omnifix$findNearbyBiomes(
            WorldGenRegion level,
            StructureManager structureManager,
            RandomState random,
            ChunkAccess chunk,
            CallbackInfo ci
    ) {
        try {
            ExtendedSurfaceContext.COMPUTED_POSSIBLE_BIOMES.set(omnifix$obtainBiomes(level, 1));
        } catch (NoSuchElementException ignored) {
        }
    }
}
