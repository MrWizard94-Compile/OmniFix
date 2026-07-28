package org.omnifix.mixin.perf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lower integrated-server thread priority so client render/input stays responsive during SP load.
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerPriorityMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$adjustServerPriority(
            Thread thread,
            Minecraft minecraft,
            LevelStorageSource.LevelStorageAccess storage,
            PackRepository packs,
            WorldStem stem,
            Services services,
            ChunkProgressListenerFactory progress,
            CallbackInfo ci
    ) {
        thread.setPriority(4);
    }
}
