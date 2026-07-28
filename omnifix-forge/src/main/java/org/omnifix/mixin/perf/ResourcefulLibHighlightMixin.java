package org.omnifix.mixin.perf;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ResourcefulLib: after {@code HighlightHandler.apply}, intern shared highlight geometry
 * (points, lines, line lists) so identical outlines do not retain distinct object graphs.
 * <p>
 * resourcefullib is optional and not on the compile classpath — all ResourcefulLib types
 * are accessed via pure reflection. Soft-fails (logs, no crash) if reflection or cache
 * shape does not match the expected {@code HashMap&lt;BlockState, Highlight&gt;} STATE_CACHE.
 */
@Pseudo
@Mixin(targets = "com.teamresourceful.resourcefullib.client.highlights.HighlightHandler", remap = false)
public abstract class ResourcefulLibHighlightMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void omnifix$deduplicateHighlights(CallbackInfo ci) {
        try {
            Class<?> handlerClz = this.getClass();
            Field stateCacheField = handlerClz.getDeclaredField("STATE_CACHE");
            stateCacheField.setAccessible(true);
            Object rawCache = stateCacheField.get(null);
            if (!(rawCache instanceof HashMap<?, ?> hashMap)) {
                LOGGER.debug(
                        "[OmniFix] ResourcefulLib STATE_CACHE unexpected type {}; skip highlight dedup",
                        rawCache == null ? "null" : rawCache.getClass().getName());
                return;
            }

            Class<?> highlightClz =
                    Class.forName("com.teamresourceful.resourcefullib.client.highlights.base.Highlight");
            Class<?> highlightLineClz =
                    Class.forName("com.teamresourceful.resourcefullib.client.highlights.base.HighlightLine");

            Method linesMethod = highlightClz.getMethod("lines");
            Method idMethod = highlightClz.getMethod("id");
            Method startMethod = highlightLineClz.getMethod("start");
            Method endMethod = highlightLineClz.getMethod("end");
            Method normalMethod = highlightLineClz.getMethod("normal");

            Constructor<?> lineCtor =
                    highlightLineClz.getConstructor(Vector3f.class, Vector3f.class, Vector3f.class);
            Constructor<?> highlightCtor =
                    highlightClz.getConstructor(ResourceLocation.class, List.class);

            ObjectOpenHashSet<Vector3f> pointCache = new ObjectOpenHashSet<>();
            ObjectOpenHashSet<Object> lineCache = new ObjectOpenHashSet<>();
            ObjectOpenHashSet<List<?>> listCache = new ObjectOpenHashSet<>();

            @SuppressWarnings("unchecked")
            Map<Object, Object> stateCache = (Map<Object, Object>) hashMap;

            stateCache.replaceAll((state, highlight) -> {
                if (highlight == null) {
                    return null;
                }
                try {
                    if (!highlightClz.isInstance(highlight)) {
                        return highlight;
                    }

                    @SuppressWarnings("unchecked")
                    List<Object> originalLines = (List<Object>) linesMethod.invoke(highlight);
                    if (originalLines == null) {
                        return highlight;
                    }

                    List<?> sharedList;
                    if (listCache.contains(originalLines)) {
                        sharedList = listCache.addOrGet(originalLines);
                    } else {
                        List<Object> rebuilt = new ArrayList<>(originalLines.size());
                        for (Object line : originalLines) {
                            if (line == null || !highlightLineClz.isInstance(line)) {
                                rebuilt.add(line);
                                continue;
                            }
                            Object deduped = line;
                            if (!lineCache.contains(line)) {
                                Vector3f start = (Vector3f) startMethod.invoke(line);
                                Vector3f end = (Vector3f) endMethod.invoke(line);
                                Vector3f normal = (Vector3f) normalMethod.invoke(line);
                                deduped = lineCtor.newInstance(
                                        pointCache.addOrGet(start),
                                        pointCache.addOrGet(end),
                                        pointCache.addOrGet(normal));
                            }
                            rebuilt.add(lineCache.addOrGet(deduped));
                        }
                        sharedList = listCache.addOrGet(List.copyOf(rebuilt));
                    }

                    Object id = idMethod.invoke(highlight);
                    return highlightCtor.newInstance(id, sharedList);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });

            LOGGER.info(
                    "[OmniFix] Deduplicated ResourcefulLib highlights ({} points, {} lines)",
                    pointCache.size(),
                    lineCache.size());
        } catch (Throwable t) {
            LOGGER.error("[OmniFix] Not applying ResourcefulLib highlight dedup due to reflection error", t);
        }
    }
}
