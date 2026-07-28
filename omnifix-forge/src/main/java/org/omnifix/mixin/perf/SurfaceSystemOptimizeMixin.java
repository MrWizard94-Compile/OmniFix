package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.omnifix.worldgen.ChunkBiomeLookup;
import org.omnifix.worldgen.ExtendedSurfaceContext;
import org.omnifix.worldgen.PrefetchingBlockColumn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = SurfaceSystem.class, priority = 2000)
public abstract class SurfaceSystemOptimizeMixin {

    @Unique
    private static final ThreadLocal<ChunkBiomeLookup> OMNIFIX$LOOKUP =
            ThreadLocal.withInitial(ChunkBiomeLookup::new);

    @Unique
    private static final ThreadLocal<PrefetchingBlockColumn> OMNIFIX$COLUMN = new ThreadLocal<>();

    @ModifyArg(
            method = "buildSurface",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/SurfaceRules$Context;<init>(Lnet/minecraft/world/level/levelgen/SurfaceSystem;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/NoiseChunk;Ljava/util/function/Function;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;)V"
            ),
            index = 4
    )
    private Function<BlockPos, Holder<Biome>> omnifix$useFasterLookup(
            Function<BlockPos, Holder<Biome>> biomeGetter,
            @Local(ordinal = 0, argsOnly = true) BiomeManager manager,
            @Local(ordinal = 0, argsOnly = true) ChunkAccess chunk,
            @Share("chunkBiomeLookup") LocalRef<ChunkBiomeLookup> lookupRef
    ) {
        if (manager.getClass() == BiomeManager.class) {
            var lookup = OMNIFIX$LOOKUP.get();
            BiomeManagerAccessor accessor = (BiomeManagerAccessor) manager;
            lookup.prepare(accessor.omnifix$getBiomeSource(), accessor.omnifix$getZoomSeed(), chunk, manager);
            lookupRef.set(lookup);
            return lookup;
        }
        lookupRef.set(null);
        return biomeGetter;
    }

    @Inject(
            method = "buildSurface",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;apply(Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0
            )
    )
    private void omnifix$injectBiomesOnContext(
            CallbackInfo ci,
            @Local(ordinal = 0) SurfaceRules.Context surfacerules$context
    ) {
        ((ExtendedSurfaceContext) (Object) surfacerules$context).omnifix$applyPossibleBiomes();
    }

    @Inject(method = "buildSurface", at = @At("TAIL"))
    private void omnifix$finishAndDisposeLookups(
            RandomState randomState,
            BiomeManager biomeManager,
            Registry<Biome> biomes,
            boolean useLegacyRandomSource,
            WorldGenerationContext context,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            SurfaceRules.RuleSource ruleSource,
            CallbackInfo ci
    ) {
        OMNIFIX$LOOKUP.get().dispose();
        var column = OMNIFIX$COLUMN.get();
        if (column != null) {
            column.dispose();
        }
    }

    @Redirect(
            method = "buildSurface",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/biome/BiomeManager;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"
            )
    )
    private Holder<Biome> omnifix$useFasterLookupRedirect(
            BiomeManager instance,
            BlockPos pos,
            @Share("chunkBiomeLookup") LocalRef<ChunkBiomeLookup> lookupRef
    ) {
        var lookup = lookupRef.get();
        if (lookup != null) {
            return lookup.apply(pos);
        }
        return instance.getBiome(pos);
    }

    @Inject(
            method = "buildSurface",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/SurfaceRules$Context;<init>(Lnet/minecraft/world/level/levelgen/SurfaceSystem;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/NoiseChunk;Ljava/util/function/Function;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;)V"
            )
    )
    private void omnifix$captureRealBlockColumn(
            CallbackInfo ci,
            @Local(ordinal = 0) LocalRef<BlockColumn> column,
            @Local(ordinal = 0, argsOnly = true) ChunkAccess chunk,
            @Share("prefetchColumn") LocalRef<PrefetchingBlockColumn> prefetchRef
    ) {
        var prefetching = OMNIFIX$COLUMN.get();
        if (prefetching == null || prefetching.getExpectedHeight() != chunk.getHeight()) {
            prefetching = new PrefetchingBlockColumn(chunk.getHeight());
            OMNIFIX$COLUMN.set(prefetching);
        }
        column.set(prefetching);
        prefetchRef.set(prefetching);
    }

    @Inject(
            method = "buildSurface",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;setZ(I)Lnet/minecraft/core/BlockPos$MutableBlockPos;",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void omnifix$prefetchBlockArray(
            RandomState randomState,
            BiomeManager biomeManager,
            Registry<Biome> biomes,
            boolean useLegacyRandomSource,
            WorldGenerationContext context,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            SurfaceRules.RuleSource ruleSource,
            CallbackInfo ci,
            @Local(ordinal = 0) BlockColumn column,
            @Local(ordinal = 0) BlockPos.MutableBlockPos cursor
    ) {
        ((PrefetchingBlockColumn) column).prefetch(chunk, cursor.getX() & 15, cursor.getZ() & 15);
    }
}
