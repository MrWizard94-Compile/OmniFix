package org.omnifix.mixin.perf;

import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * Cap util ForkJoin worker priority so background Minecraft work is less likely to starve the
 * main / server threads on contested CPUs.
 */
@Mixin(Util.class)
public abstract class UtilThreadPriorityMixin {

    @ModifyArg(
            method = "makeExecutor",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/ForkJoinPool;<init>(ILjava/util/concurrent/ForkJoinPool$ForkJoinWorkerThreadFactory;Ljava/lang/Thread$UncaughtExceptionHandler;Z)V"
            ),
            index = 1
    )
    private static ForkJoinPool.ForkJoinWorkerThreadFactory omnifix$lowerWorkerPriority(
            ForkJoinPool.ForkJoinWorkerThreadFactory factory
    ) {
        return pool -> {
            ForkJoinWorkerThread thread = factory.newThread(pool);
            thread.setPriority(4);
            return thread;
        };
    }
}
