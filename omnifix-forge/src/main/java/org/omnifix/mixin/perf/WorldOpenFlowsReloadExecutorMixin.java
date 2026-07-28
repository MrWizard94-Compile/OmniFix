package org.omnifix.mixin.perf;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.omnifix.resources.ReloadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

/**
 * World open data loads use the dedicated resource-reload executor.
 */
@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsReloadExecutorMixin {

    @ModifyArg(
            method = "loadWorldDataBlocking",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/WorldLoader;load(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 3
    )
    private Executor omnifix$getResourceReloadExecutor(Executor service) {
        return ReloadExecutor.get();
    }
}
