package org.omnifix.mixin.perf;

import net.minecraft.server.MinecraftServer;
import org.omnifix.resources.ReloadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

/**
 * Server datapack reloads use the dedicated resource-reload executor.
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerReloadExecutorMixin {

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ReloadableServerResources;loadResources(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess$Frozen;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 5
    )
    private Executor omnifix$getReloadExecutor(Executor asyncExecutor) {
        return ReloadExecutor.get();
    }
}
