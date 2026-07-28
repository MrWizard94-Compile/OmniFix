package org.omnifix.mixin.perf;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.omnifix.resources.ReloadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

/**
 * World-create pack apply / openFresh loads use the dedicated reload executor.
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenReloadExecutorMixin {

    @ModifyArg(
            method = "applyNewPackConfig",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/WorldLoader;load(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 3
    )
    private Executor omnifix$getReloadExecutorService(Executor e) {
        return ReloadExecutor.get();
    }

    @ModifyArg(
            method = "openFresh",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/WorldLoader;load(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 3
    )
    private static Executor omnifix$getCreationExecutorService(Executor e) {
        return ReloadExecutor.get();
    }
}
