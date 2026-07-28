package org.omnifix.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Soft-fail runner for non-essential reflection patches. Failures log and never abort launch.
 */
public final class SafeRun {

    private static final Logger LOGGER = LogUtils.getLogger();

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    private SafeRun() {}

    public static void run(ThrowingRunnable action, String errorMessage) {
        try {
            action.run();
        } catch (Throwable t) {
            LOGGER.error("[OmniFix] {}", errorMessage, t);
        }
    }
}
