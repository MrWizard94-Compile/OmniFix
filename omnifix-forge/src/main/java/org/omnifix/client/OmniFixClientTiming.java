package org.omnifix.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;

/**
 * Launch + world-join timing (ModernFix measure_time class).
 */
public final class OmniFixClientTiming {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static long worldLoadStartTime = -1L;
    private static int numRenderTicks;
    private static float gameStartTimeSeconds = -1f;
    private static boolean recipesUpdated;
    private static boolean tagsUpdated;

    private OmniFixClientTiming() {}

    public static void onGameLaunchFinish() {
        if (gameStartTimeSeconds >= 0) {
            return;
        }
        gameStartTimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000f;
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_MEASURE_TIME)) {
            LOGGER.warn("[OmniFix] Game took {} seconds to start", gameStartTimeSeconds);
        }
    }

    public static void markWorldLoadStart() {
        numRenderTicks = 0;
        worldLoadStartTime = System.nanoTime();
        recipesUpdated = false;
        tagsUpdated = false;
    }

    public static void onRecipesUpdated() {
        recipesUpdated = true;
    }

    public static void onTagsUpdated() {
        tagsUpdated = true;
    }

    public static void onRenderTickEnd() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_MEASURE_TIME)) {
            return;
        }
        if (recipesUpdated
                && tagsUpdated
                && worldLoadStartTime != -1
                && Minecraft.getInstance().player != null
                && numRenderTicks++ >= 10) {
            float timeSpentLoading = (System.nanoTime() - worldLoadStartTime) / 1_000_000_000f;
            LOGGER.warn("[OmniFix] Time from main menu to in-game was {} seconds", timeSpentLoading);
            if (gameStartTimeSeconds >= 0) {
                LOGGER.warn(
                        "[OmniFix] Total time to load game and open world was {} seconds",
                        timeSpentLoading + gameStartTimeSeconds);
            }
            numRenderTicks = 0;
            worldLoadStartTime = -1;
            recipesUpdated = false;
            tagsUpdated = false;
        }
    }
}
