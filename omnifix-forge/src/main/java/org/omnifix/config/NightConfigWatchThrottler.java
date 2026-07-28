package org.omnifix.config;

import com.electronwill.nightconfig.core.file.FileWatcher;
import com.google.common.collect.ForwardingCollection;
import com.google.common.collect.ForwardingMap;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Throttles NightConfig's file-watcher loop. Unthrottled watch iterations burn CPU and allocate
 * heavily via repeated {@code values().iterator()} calls
 * (see night-config PR #144 / ModernFix-class reports).
 *
 * <p>Injects a 1s park on the watcher thread each loop, while leaving the launch/main thread
 * unblocked so early registration stays responsive.
 */
public final class NightConfigWatchThrottler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long DELAY_NS = TimeUnit.MILLISECONDS.toNanos(1000);

    private NightConfigWatchThrottler() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void throttle() {
        Map watchedDirs = ObfuscationReflectionHelper.getPrivateValue(
                FileWatcher.class, FileWatcher.defaultInstance(), "watchedDirs");
        if (watchedDirs == null) {
            LOGGER.warn("[OmniFix] NightConfig watchedDirs field missing; throttle skipped");
            return;
        }
        Thread launchThread = Thread.currentThread();
        Map watchedDirsWrapper = new ForwardingMap() {
            @Override
            protected Map delegate() {
                return watchedDirs;
            }

            private Collection cachedValues;

            @Override
            public Collection values() {
                if (cachedValues == null) {
                    Collection values = super.values();
                    cachedValues = new ForwardingCollection() {
                        @Override
                        protected Collection delegate() {
                            return values;
                        }

                        @Override
                        public Iterator iterator() {
                            // iterator() is called at the start of each watch-loop iteration.
                            if (Thread.currentThread() != launchThread) {
                                LockSupport.parkNanos(DELAY_NS);
                            }
                            return super.iterator();
                        }
                    };
                }
                return cachedValues;
            }
        };
        // Force-load forwarding-collection classes on the launch thread so the watcher thread
        // does not trigger ModLauncher ConcurrentModificationException while mutating plugins.
        //noinspection StatementWithEmptyBody
        for (Object ignored : watchedDirsWrapper.values()) {
        }
        ObfuscationReflectionHelper.setPrivateValue(
                FileWatcher.class, FileWatcher.defaultInstance(), watchedDirsWrapper, "watchedDirs");
        LOGGER.info("[OmniFix] NightConfig file-watcher throttle applied (1s park)");
    }
}
