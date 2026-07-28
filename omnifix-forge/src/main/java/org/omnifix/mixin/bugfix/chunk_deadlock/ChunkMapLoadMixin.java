package org.omnifix.mixin.bugfix.chunk_deadlock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Completes the ModernFix chunk-deadlock suite: full-chunk promotion runs on the main-thread
 * executor with a surrogate future so EntityJoinLevelEvent / recursive loads cannot deadlock the
 * priority sorter.
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapLoadMixin {

    @Shadow
    @Nullable
    protected abstract ChunkHolder getVisibleChunkIfPresent(long l);

    @Shadow
    @Final
    private BlockableEventLoop<Runnable> mainThreadExecutor;

    @Unique
    private static final ThreadLocal<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>
            OMNIFIX$SURROGATE = new ThreadLocal<>();

    @Unique
    private final ConcurrentLinkedQueue<Throwable> omnifix$promotionExceptions = new ConcurrentLinkedQueue<>();

    @Redirect(
            method = "protoChunkToFullChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
                    ordinal = 0
            ),
            require = 0
    )
    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> omnifix$surrogateFuture(
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> previousFuture,
            Function<? super Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>,
                    ? extends Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> fn,
            Executor executor
    ) {
        var surrogate = new CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>();
        previousFuture.thenComposeAsync(CompletableFuture::completedFuture, executor).thenApplyAsync(either -> {
            OMNIFIX$SURROGATE.set(surrogate);
            try {
                return fn.apply(either);
            } finally {
                OMNIFIX$SURROGATE.remove();
            }
        }, this.mainThreadExecutor).whenComplete((either, throwable) -> {
            if (throwable != null) {
                if (!surrogate.isDone()) {
                    surrogate.completeExceptionally(throwable);
                } else {
                    this.omnifix$promotionExceptions.add(throwable);
                }
            } else {
                surrogate.complete(either);
            }
        });
        return surrogate;
    }

    @Inject(
            method = "lambda$protoChunkToFullChunk$34",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;runPostLoad()V"),
            require = 0
    )
    private void omnifix$completeSurrogate(
            ChunkHolder holder,
            ChunkAccess access,
            CallbackInfoReturnable<ChunkAccess> cir,
            @Local(ordinal = 0) LevelChunk levelChunk
    ) {
        var future = OMNIFIX$SURROGATE.get();
        if (future != null) {
            future.complete(Either.left(levelChunk));
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"), require = 0)
    private void omnifix$reportDeferredPromotion(CallbackInfo ci) {
        Throwable throwable = this.omnifix$promotionExceptions.poll();
        if (throwable == null) {
            return;
        }
        if (throwable instanceof ReportedException e) {
            throw e;
        }
        throw new ReportedException(CrashReport.forThrowable(throwable, "Exception during promotion of chunk to FULL status"));
    }

    private static final Field CURRENTLY_LOADING =
            ObfuscationReflectionHelper.findField(ChunkHolder.class, "currentlyLoading");

    private static void setCurrentlyLoading(ChunkHolder holder, LevelChunk value) {
        try {
            CURRENTLY_LOADING.set(holder, value);
        } catch (ReflectiveOperationException e) {
            // soft
        }
    }

    @WrapOperation(
            method = "lambda$protoChunkToFullChunk$34",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;runPostLoad()V"),
            require = 0
    )
    private void omnifix$setCurrentlyLoading(LevelChunk chunk, Operation<Void> operation) {
        ChunkHolder holder = this.getVisibleChunkIfPresent(chunk.getPos().toLong());
        if (holder != null) {
            LevelChunk prev = null;
            try {
                prev = (LevelChunk) CURRENTLY_LOADING.get(holder);
            } catch (ReflectiveOperationException ignored) {
            }
            try {
                setCurrentlyLoading(holder, chunk);
                operation.call(chunk);
            } finally {
                setCurrentlyLoading(holder, prev);
            }
        } else {
            operation.call(chunk);
        }
    }
}
