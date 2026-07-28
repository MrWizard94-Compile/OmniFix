package org.omnifix.mixin;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.tree.ClassNode;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;
import org.omnifix.resources.ReloadExecutor;
import org.slf4j.Logger;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.LoggerAdapterDefault;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.service.MixinServiceAbstract;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * After launch, force mixin audit then clear {@link ClassInfo} cache / mixin ClassNodes to reclaim
 * large permanent Mixin memory. Default off — can break late-loading mixin consumers.
 */
public final class ClassInfoManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean hasRun;
    private static final List<Runnable> LOGGERS_TO_RESTORE = new ArrayList<>();

    private ClassInfoManager() {}

    public static void clearIfEnabled() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_CLEAR_MIXIN_CLASSINFO) || hasRun) {
            return;
        }
        hasRun = true;
        ReloadExecutor.get().execute(ClassInfoManager::doClear);
    }

    private static Field accessible(Field f) {
        f.setAccessible(true);
        return f;
    }

    @SuppressWarnings("unchecked")
    private static void changeLoggerAndRestoreLater(Map<String, ILogger> map, ILogger newLogger) {
        ILogger oldLogger = map.put("mixin.audit", newLogger);
        LOGGERS_TO_RESTORE.add(() -> map.put("mixin.audit", oldLogger));
    }

    @SuppressWarnings("unchecked")
    private static void disableLoggers() throws ReflectiveOperationException {
        Field loggersField = accessible(MixinServiceAbstract.class.getDeclaredField("loggers"));
        changeLoggerAndRestoreLater(
                (Map<String, ILogger>) loggersField.get(null), new LoggerAdapterDefault("mixin.audit"));
        try {
            Class<?> fabricLogger = Class.forName("net.fabricmc.loader.impl.knot.MixinLogger");
            loggersField = accessible(fabricLogger.getDeclaredField("LOGGER_MAP"));
            changeLoggerAndRestoreLater(
                    (Map<String, ILogger>) loggersField.get(null), new LoggerAdapterDefault("mixin.audit"));
        } catch (ClassNotFoundException ignored) {
            // Forge-only.
        }
    }

    @SuppressWarnings("unchecked")
    private static void doClear() {
        Map<String, ClassInfo> classInfoCache;
        Field mixinField;
        Field stateField;
        Field classNodeField;
        try {
            disableLoggers();
            Field field = accessible(ClassInfo.class.getDeclaredField("cache"));
            classInfoCache = (Map<String, ClassInfo>) field.get(null);
            mixinField = accessible(ClassInfo.class.getDeclaredField("mixin"));
            Class<?> stateClz = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo$State");
            stateField =
                    accessible(Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo").getDeclaredField("state"));
            classNodeField = accessible(stateClz.getDeclaredField("classNode"));
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("[OmniFix] Unable to prepare ClassInfo clear", e);
            return;
        }
        MixinEnvironment.getDefaultEnvironment().audit();
        try {
            ClassNode emptyNode = new ClassNode();
            List<Map.Entry<String, ClassInfo>> entries = new ArrayList<>(classInfoCache.entrySet());
            for (Map.Entry<String, ClassInfo> entry : entries) {
                if ("java/lang/Object".equals(entry.getKey())) {
                    continue;
                }
                ClassInfo mixinClz = entry.getValue();
                if (mixinClz != null) {
                    try {
                        if (mixinClz.isMixin()) {
                            IMixinInfo theInfo = (IMixinInfo) mixinField.get(mixinClz);
                            Object state = stateField.get(theInfo);
                            if (state != null) {
                                classNodeField.set(state, emptyNode);
                            }
                        }
                    } catch (ReflectiveOperationException | RuntimeException e) {
                        LOGGER.debug("[OmniFix] ClassInfo clear skip {}", entry.getKey(), e);
                    }
                }
                classInfoCache.remove(entry.getKey());
            }
        } catch (RuntimeException e) {
            LOGGER.error("[OmniFix] ClassInfo clear failed", e);
        }
        LOGGERS_TO_RESTORE.forEach(Runnable::run);
        LOGGERS_TO_RESTORE.clear();
        LOGGER.warn("[OmniFix] Cleared Mixin ClassInfo data structures");
    }
}
