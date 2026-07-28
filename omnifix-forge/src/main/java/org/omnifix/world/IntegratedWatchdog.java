package org.omnifix.world;

import com.mojang.logging.LogUtils;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import org.omnifix.duck.ITimeTrackingServer;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.OptionalLong;

/**
 * Detects hung integrated-server ticks (>40s) and dumps all threads.
 */
public final class IntegratedWatchdog extends Thread {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long MAX_TICK_DELTA = 40_000L;

    private final WeakReference<MinecraftServer> server;

    public IntegratedWatchdog(MinecraftServer server) {
        this.server = new WeakReference<>(server);
        setDaemon(true);
        setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
        setName("OmniFix integrated server watchdog");
    }

    private OptionalLong getLastTickStart() {
        MinecraftServer s = this.server.get();
        if (s == null || !s.isRunning()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(((ITimeTrackingServer) s).omnifix$getLastTickStartTime());
    }

    @Override
    public void run() {
        while (true) {
            OptionalLong lastTickStart = getLastTickStart();
            if (lastTickStart.isEmpty()) {
                return;
            }
            if (lastTickStart.getAsLong() < 0) {
                try {
                    Thread.sleep(10_000L);
                } catch (InterruptedException ignored) {
                }
                continue;
            }
            long curTime = Util.getMillis();
            long delta = curTime - lastTickStart.getAsLong();
            if (delta > MAX_TICK_DELTA) {
                LOGGER.error(
                        "[OmniFix] A single server tick has taken {}, more than {} milliseconds",
                        delta,
                        MAX_TICK_DELTA);
                LOGGER.error(ThreadDumper.obtainThreadDump());
                delta = 0;
            }
            try {
                Thread.sleep(Math.max(1L, MAX_TICK_DELTA - delta));
            } catch (InterruptedException ignored) {
            }
        }
    }
}
