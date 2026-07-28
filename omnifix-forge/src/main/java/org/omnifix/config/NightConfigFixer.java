package org.omnifix.config;

import com.electronwill.nightconfig.core.file.FileWatcher;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.omnifix.util.SafeRun;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Prevents NightConfig watch-thread config reloads from racing Forge's main-thread config events.
 * Change handlers are queued and applied only when the player runs {@code /ofc} (client) or
 * {@code /ofsrc} (server).
 *
 * <p>Root cause: NightConfig fires change handlers on its own watcher thread while Forge may also
 * post {@code ModConfigEvent}s on the game thread, producing concurrent map mutation / CME crashes.
 */
public final class NightConfigFixer {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Queued change handlers awaiting explicit reload. */
    public static final LinkedHashSet<Runnable> CONFIGS_TO_RELOAD = new LinkedHashSet<>();

    private NightConfigFixer() {}

    public static void monitorFileWatcher() {
        SafeRun.run(() -> {
            FileWatcher watcher = FileWatcher.defaultInstance();
            Field field = FileWatcher.class.getDeclaredField("watchedFiles");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<Path, ?> theMap = (ConcurrentHashMap<Path, ?>) field.get(watcher);
            field.set(watcher, new MonitoringMap(theMap));
            LOGGER.info("[OmniFix] Applied NightConfig watchedFiles corruption / race patch");
        }, "replacing NightConfig watchedFiles map");
    }

    public static void runReloads() {
        List<Runnable> runnables;
        synchronized (CONFIGS_TO_RELOAD) {
            runnables = new ArrayList<>(CONFIGS_TO_RELOAD);
            CONFIGS_TO_RELOAD.clear();
        }
        for (Runnable r : runnables) {
            try {
                r.run();
            } catch (RuntimeException e) {
                LOGGER.error("[OmniFix] Config reload handler failed", e);
            }
        }
        LOGGER.info("[OmniFix] Processed {} deferred config reload(s)", runnables.size());
    }

    static final class MonitoringMap extends ConcurrentHashMap<Path, Object> {
        private static final Class<?> WATCHED_FILE;
        private static final Field CHANGE_HANDLER;

        static {
            try {
                WATCHED_FILE = Class.forName("com.electronwill.nightconfig.core.file.FileWatcher$WatchedFile");
                CHANGE_HANDLER = ObfuscationReflectionHelper.findField(WATCHED_FILE, "changeHandler");
            } catch (ClassNotFoundException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        MonitoringMap(ConcurrentHashMap<Path, ?> oldMap) {
            super(oldMap);
        }

        @Override
        public Object computeIfAbsent(Path key, Function<? super Path, ?> mappingFunction) {
            return super.computeIfAbsent(key, path -> {
                Object watchedFile = mappingFunction.apply(path);
                try {
                    Runnable changeHandler = (Runnable) CHANGE_HANDLER.get(watchedFile);
                    CHANGE_HANDLER.set(watchedFile, new MonitoringConfigTracker(changeHandler));
                } catch (ReflectiveOperationException e) {
                    LOGGER.error("[OmniFix] Failed to wrap NightConfig change handler", e);
                }
                return watchedFile;
            });
        }
    }

    static final class MonitoringConfigTracker implements Runnable {
        private final Runnable configTracker;

        MonitoringConfigTracker(Runnable r) {
            this.configTracker = r;
        }

        @Override
        public void run() {
            synchronized (CONFIGS_TO_RELOAD) {
                if (CONFIGS_TO_RELOAD.isEmpty()) {
                    LOGGER.info(
                            "[OmniFix] Config file change detected. Use /{} to apply reloads safely.",
                            FMLLoader.getDist().isDedicatedServer() ? "ofsrc" : "ofc");
                }
                CONFIGS_TO_RELOAD.add(configTracker);
            }
        }
    }
}
