package org.omnifix.mixin.perf;

import net.minecraft.client.Minecraft;
import org.omnifix.resources.ReloadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ExecutorService;

/**
 * Client resource reloads use a dedicated pool instead of the shared background executor.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftReloadExecutorMixin {

    @Redirect(
            method = {
                    "<init>",
                    "reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/Util;backgroundExecutor()Ljava/util/concurrent/ExecutorService;",
                    ordinal = 0
            )
    )
    private ExecutorService omnifix$resourceReloadExecutor() {
        return ReloadExecutor.get();
    }
}
