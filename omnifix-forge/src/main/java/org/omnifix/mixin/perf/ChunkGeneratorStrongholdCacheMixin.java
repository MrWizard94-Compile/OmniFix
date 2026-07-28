package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import org.omnifix.duck.IChunkGeneratorStrongholdCache;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Disk-cache concentric-ring structure positions (strongholds) and compute them on a dedicated
 * pool so they do not starve the main worker pool during world open.
 */
@Mixin(ChunkGeneratorStructureState.class)
public abstract class ChunkGeneratorStrongholdCacheMixin implements IChunkGeneratorStrongholdCache {

    @Unique
    private static final Logger OMNIFIX$LOGGER = LogUtils.getLogger();

    @Unique
    private static final String OMNIFIX$CACHE_FILENAME = "omnifix_stronghold_cache_v2.nbt";

    @Shadow
    @Final
    private long concentricRingsSeed;

    @Shadow
    @Final
    private BiomeSource biomeSource;

    @Unique
    private Path omnifix$dimensionPath;

    @Unique
    private MinecraftServer omnifix$server;

    @Unique
    private SoftReference<Map<String, List<ChunkPos>>> omnifix$cachedPositions = new SoftReference<>(null);

    @Override
    public void omnifix$setStrongholdCachePath(Path cachePath, MinecraftServer server) {
        this.omnifix$dimensionPath = cachePath;
        this.omnifix$server = server;
    }

    @WrapMethod(method = "generateRingPositions")
    private CompletableFuture<List<ChunkPos>> omnifix$cacheRingPositions(
            Holder<StructureSet> structureSet,
            ConcentricRingsStructurePlacement placement,
            Operation<CompletableFuture<List<ChunkPos>>> original,
            @Share("threadPool") LocalRef<ExecutorService> threadPoolRef
    ) {
        if (this.omnifix$server == null || this.omnifix$dimensionPath == null) {
            return original.call(structureSet, placement);
        }

        String cacheKey = omnifix$makeCacheKey(placement);
        if (cacheKey != null) {
            List<ChunkPos> cached = omnifix$readFromCache(cacheKey);
            if (cached != null) {
                OMNIFIX$LOGGER.debug("[OmniFix] Using cached stronghold positions for {}", cacheKey);
                return CompletableFuture.completedFuture(List.copyOf(cached));
            }
        }

        MinecraftServer server = this.omnifix$server;
        ExecutorService strongholdPool = Executors.newFixedThreadPool(
                Math.max(1, Runtime.getRuntime().availableProcessors() - 2));
        threadPoolRef.set(strongholdPool);
        try {
            return original.call(structureSet, placement).thenApplyAsync(positions -> {
                if (server.isRunning() && cacheKey != null) {
                    omnifix$writeToCache(cacheKey, positions);
                }
                return positions;
            }, Util.ioPool());
        } finally {
            strongholdPool.shutdown();
        }
    }

    @Redirect(
            method = "generateRingPositions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/Util;backgroundExecutor()Ljava/util/concurrent/ExecutorService;"
            )
    )
    private ExecutorService omnifix$useDedicatedService(
            @Share("threadPool") LocalRef<ExecutorService> threadPoolRef
    ) {
        ExecutorService pool = threadPoolRef.get();
        return pool != null ? pool : Util.backgroundExecutor();
    }

    @Unique
    private String omnifix$makeCacheKey(ConcentricRingsStructurePlacement placement) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, this.omnifix$server.registryAccess());
        String placementKey = ConcentricRingsStructurePlacement.CODEC.encodeStart(ops, placement)
                .result().map(Tag::toString).orElse(null);
        String biomeSourceKey = BiomeSource.CODEC.encodeStart(ops, this.biomeSource)
                .result().map(Tag::toString).orElse(null);
        if (placementKey == null || biomeSourceKey == null) {
            OMNIFIX$LOGGER.warn("[OmniFix] Failed to create stronghold cache key");
            return null;
        }
        String data = placementKey + ";biomes=" + biomeSourceKey + ";seed=" + this.concentricRingsSeed;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    @Unique
    private synchronized List<ChunkPos> omnifix$readFromCache(String cacheKey) {
        return omnifix$getOrLoadCache().get(cacheKey);
    }

    @Unique
    private synchronized void omnifix$writeToCache(String cacheKey, List<ChunkPos> positions) {
        Map<String, List<ChunkPos>> cache = omnifix$getOrLoadCache();
        cache.put(cacheKey, List.copyOf(positions));
        omnifix$cachedPositions = new SoftReference<>(cache);
        omnifix$saveCacheFile(cache);
    }

    @Unique
    private Map<String, List<ChunkPos>> omnifix$getOrLoadCache() {
        Map<String, List<ChunkPos>> cache = omnifix$cachedPositions.get();
        if (cache != null) {
            return cache;
        }
        cache = omnifix$loadCacheFile();
        omnifix$cachedPositions = new SoftReference<>(cache);
        return cache;
    }

    @Unique
    private Map<String, List<ChunkPos>> omnifix$loadCacheFile() {
        Path file = omnifix$dimensionPath.resolve(OMNIFIX$CACHE_FILENAME);
        if (!Files.exists(file)) {
            return new HashMap<>();
        }
        try {
            CompoundTag root = NbtIo.readCompressed(file.toFile());
            Map<String, List<ChunkPos>> result = new HashMap<>();
            for (String key : root.getAllKeys()) {
                if (root.contains(key, Tag.TAG_INT_ARRAY)) {
                    int[] data = root.getIntArray(key);
                    if (data.length >= 2 && data.length % 2 == 0) {
                        List<ChunkPos> positions = new ArrayList<>(data.length / 2);
                        for (int i = 0; i < data.length; i += 2) {
                            positions.add(new ChunkPos(data[i], data[i + 1]));
                        }
                        result.put(key, positions);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            OMNIFIX$LOGGER.warn("[OmniFix] Failed to read stronghold cache, will recompute", e);
            return new HashMap<>();
        }
    }

    @Unique
    private void omnifix$saveCacheFile(Map<String, List<ChunkPos>> cache) {
        CompoundTag root = new CompoundTag();
        for (Map.Entry<String, List<ChunkPos>> entry : cache.entrySet()) {
            List<ChunkPos> positions = entry.getValue();
            int[] data = new int[positions.size() * 2];
            for (int i = 0; i < positions.size(); i++) {
                ChunkPos pos = positions.get(i);
                data[i * 2] = pos.x;
                data[i * 2 + 1] = pos.z;
            }
            root.putIntArray(entry.getKey(), data);
        }
        Path file = omnifix$dimensionPath.resolve(OMNIFIX$CACHE_FILENAME);
        try {
            NbtIo.writeCompressed(root, file.toFile());
        } catch (Exception e) {
            OMNIFIX$LOGGER.warn("[OmniFix] Failed to write stronghold cache", e);
        }
    }
}
