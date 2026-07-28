package org.omnifix.dfu;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Periodically clears DFU rewrite caches after idle so memory does not grow unbounded after upgrades.
 */
public final class DFUBlaster {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Map<?, ?>> TRACKED_MAPS = buildTrackedMaps();
    private static final long DELAY_TIME = TimeUnit.SECONDS.toNanos(60);
    private static final AtomicLong NEXT_WAKE_TIME = new AtomicLong(System.nanoTime() + DELAY_TIME);

    private DFUBlaster() {}

    private static List<Map<?, ?>> buildTrackedMaps() {
        List<Map<?, ?>> list = new ArrayList<>();
        tryAdd(list, "com.mojang.datafixers.DSL$Instances", "TAGGED_CHOICE_TYPE_CACHE");
        tryAdd(list, "com.mojang.datafixers.functions.Fold", "HMAP_APPLY_CACHE");
        tryAdd(list, "com.mojang.datafixers.types.Type", "REWRITE_CACHE");
        return list;
    }

    @SuppressWarnings("unchecked")
    private static void tryAdd(List<Map<?, ?>> list, String className, String fieldName) {
        try {
            Class<?> clz = Class.forName(className);
            Field field = clz.getDeclaredField(fieldName);
            field.setAccessible(true);
            if (Map.class.isAssignableFrom(field.getType())) {
                list.add((Map<?, ?>) field.get(null));
            } else {
                LOGGER.error("[OmniFix] Field {} on class {} is not a map", fieldName, className);
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[OmniFix] Error tracking DFU field {} on class {}", fieldName, className, e);
        }
    }

    public static void blastMaps() {
        new CleanerThread().start();
    }

    public static void kick() {
        NEXT_WAKE_TIME.set(System.nanoTime() + DELAY_TIME);
    }

    static final class CleanerThread extends Thread {
        CleanerThread() {
            setName("OmniFix-DFU-cleaner");
            setPriority(1);
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                long waitTime = NEXT_WAKE_TIME.get() - System.nanoTime();
                if (waitTime > 0) {
                    LockSupport.parkNanos(waitTime);
                }
                long lastStamp = NEXT_WAKE_TIME.get();
                if (System.nanoTime() >= lastStamp) {
                    TRACKED_MAPS.forEach(Map::clear);
                    NEXT_WAKE_TIME.compareAndSet(lastStamp, System.nanoTime() + DELAY_TIME);
                }
            }
        }
    }
}
