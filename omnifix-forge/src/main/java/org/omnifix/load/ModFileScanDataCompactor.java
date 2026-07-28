package org.omnifix.load;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shrinks {@link ModFileScanData} retained after discovery: drops annotation noise nobody queries
 * (Kotlin/Scala/Mixin/JetBrains/javax OnlyIn) and interns {@link Type}/member strings across mods.
 * Large packs free tens of MB of permanent scan metadata.
 */
public final class ModFileScanDataCompactor {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Field ANNOTATIONS_FIELD = tryGetField("annotations");
    private static final Field CLASSES_FIELD = tryGetField("classes");
    private static final ObjectOpenHashSet<Type> TYPES = new ObjectOpenHashSet<>();

    private ModFileScanDataCompactor() {}

    private static Field tryGetField(String name) {
        try {
            Field f = ModFileScanData.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            LOGGER.error("[OmniFix] Unable to access '{}' on ModFileScanData", name, e);
            return null;
        }
    }

    public static void compact() {
        if (ANNOTATIONS_FIELD == null || CLASSES_FIELD == null) {
            return;
        }
        int files = 0;
        for (var file : ModList.get().getModFiles()) {
            var scanResult = file.getFile().getScanResult();
            try {
                compact(scanResult, file.getFile().getFileName());
                files++;
            } catch (Throwable e) {
                LOGGER.error("[OmniFix] Error compacting scan data for {}", file.getFile().getFileName(), e);
            }
        }
        TYPES.clear();
        TYPES.trim();
        LOGGER.info("[OmniFix] Compacted ModFileScanData for {} mod file(s)", files);
    }

    private static void compact(ModFileScanData data, String fileName) {
        ObjectOpenHashSet<String> memberNames = new ObjectOpenHashSet<>();
        var annotationSet = data.getAnnotations().stream().filter(a -> {
            String clzName = a.annotationType().getClassName();
            return !clzName.startsWith("kotlin.jvm.")
                    && !clzName.startsWith("scala.reflect.")
                    && !clzName.startsWith("org.spongepowered.asm.mixin.")
                    && !clzName.startsWith("com.llamalad7.mixinextras.")
                    && !clzName.contains("org.jetbrains.annotations.")
                    && !clzName.contains("javax.annotation.")
                    && !clzName.endsWith("kotlin.Metadata")
                    && !clzName.equals("net.minecraftforge.api.distmarker.OnlyIn");
        }).map(a -> new ModFileScanData.AnnotationData(
                TYPES.addOrGet(a.annotationType()),
                a.targetType(),
                TYPES.addOrGet(a.clazz()),
                memberNames.addOrGet(a.memberName()),
                a.annotationData().entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> {
                    Object annValue = e.getValue();
                    if (annValue instanceof ArrayList<?> list) {
                        // Some mods rely on ArrayList identity/impl — only trim capacity.
                        list.trimToSize();
                    }
                    return annValue;
                }))
        )).collect(ImmutableSet.toImmutableSet());
        if (annotationSet.size() < data.getAnnotations().size()) {
            LOGGER.debug(
                    "[OmniFix] Removed {} unneeded annotations from {}",
                    data.getAnnotations().size() - annotationSet.size(),
                    fileName);
        }
        var classSet = data.getClasses().stream().map(c -> new ModFileScanData.ClassData(
                TYPES.addOrGet(c.clazz()),
                TYPES.addOrGet(c.parent()),
                c.interfaces().stream().map(TYPES::addOrGet).collect(ImmutableSet.toImmutableSet())
        )).collect(ImmutableSet.toImmutableSet());
        try {
            ANNOTATIONS_FIELD.set(data, annotationSet);
            CLASSES_FIELD.set(data, classSet);
        } catch (Exception e) {
            LOGGER.error("[OmniFix] Error replacing fields on ModFileScanData for {}", fileName, e);
        }
    }
}
