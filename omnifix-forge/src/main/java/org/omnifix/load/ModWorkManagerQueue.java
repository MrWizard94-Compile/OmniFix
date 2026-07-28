package org.omnifix.load;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModWorkManager;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Replaces FML's sync-executor task deque so idle polling parks briefly and alternates a dummy
 * task — keeps the early loading screen ticking without busy-spinning the sync worker.
 */
public final class ModWorkManagerQueue extends ConcurrentLinkedDeque<Runnable> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long PARK_TIME = TimeUnit.MILLISECONDS.toNanos(25);
    private static final Runnable DUMMY_TASK = () -> {};

    private boolean shouldReturnDummyTask = false;

    @Override
    public Runnable pollFirst() {
        Runnable r = super.pollFirst();
        if (r == null) {
            LockSupport.parkNanos(PARK_TIME);
            boolean isReturning = shouldReturnDummyTask;
            shouldReturnDummyTask = !shouldReturnDummyTask;
            // Alternate dummy task vs null so FML redraws splash but can still exit the loop.
            return isReturning ? DUMMY_TASK : null;
        }
        return r;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void replace() {
        try {
            Class<?> syncExecutorClass = Class.forName("net.minecraftforge.fml.ModWorkManager$SyncExecutor");
            ConcurrentLinkedDeque<Runnable> taskQueue = (ConcurrentLinkedDeque<Runnable>)
                    ObfuscationReflectionHelper.getPrivateValue(
                            (Class) syncExecutorClass,
                            (Object) ModWorkManager.syncExecutor(),
                            "tasks");
            if (taskQueue == null) {
                LOGGER.warn("[OmniFix] ModWorkManager SyncExecutor.tasks missing; queue replace skipped");
                return;
            }
            ModWorkManagerQueue q = new ModWorkManagerQueue();
            Runnable task;
            do {
                task = taskQueue.pollFirst();
                if (task != null) {
                    q.push(task);
                }
            } while (task != null);
            ObfuscationReflectionHelper.setPrivateValue(
                    (Class) syncExecutorClass,
                    (Object) ModWorkManager.syncExecutor(),
                    q,
                    "tasks");
            LOGGER.info("[OmniFix] Replaced ModWorkManager sync task queue (park + splash kick)");
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[OmniFix] Failed to replace ModWorkManager queue", e);
        }
    }
}
