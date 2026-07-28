package org.omnifix.resources;

import com.mojang.logging.LogUtils;
import net.minecraft.ReportedException;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dedicated ForkJoin pool for resource reloads so reload work does not starve the shared
 * {@link net.minecraft.Util#backgroundExecutor()} used by other game systems.
 */
public final class ReloadExecutor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile ExecutorService INSTANCE;

    private ReloadExecutor() {}

    public static ExecutorService get() {
        ExecutorService existing = INSTANCE;
        if (existing != null) {
            return existing;
        }
        synchronized (ReloadExecutor.class) {
            if (INSTANCE == null) {
                INSTANCE = create();
            }
            return INSTANCE;
        }
    }

    private static ExecutorService create() {
        ClassLoader loader = ReloadExecutor.class.getClassLoader();
        AtomicInteger workerCount = new AtomicInteger(0);
        return new ForkJoinPool(ForkJoinPool.getCommonPoolParallelism(), pool -> {
            ForkJoinWorkerThread thread = new ForkJoinWorkerThread(pool) {
                @Override
                protected void onTermination(Throwable throwOnTermination) {
                    if (throwOnTermination != null) {
                        LOGGER.warn("[OmniFix] {} died", this.getName(), throwOnTermination);
                    } else {
                        LOGGER.debug("[OmniFix] {} shutdown", this.getName());
                    }
                    super.onTermination(throwOnTermination);
                }
            };
            thread.setContextClassLoader(loader);
            thread.setName("Worker-ResourceReload-" + workerCount.getAndIncrement());
            return thread;
        }, ReloadExecutor::handleException, true);
    }

    private static void handleException(Thread thread, Throwable throwable) {
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }
        if (throwable instanceof ReportedException reported) {
            Bootstrap.realStdoutPrintln(reported.getReport().getFriendlyReport());
            System.exit(-1);
        }
        LOGGER.error(String.format("[OmniFix] Caught exception in thread %s", thread), throwable);
    }
}
