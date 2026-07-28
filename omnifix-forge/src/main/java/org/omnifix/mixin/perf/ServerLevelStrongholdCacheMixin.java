package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.omnifix.duck.IChunkGeneratorStrongholdCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Injects the dimension storage path + server into ChunkGeneratorStructureState before ring
 * positions are generated, enabling disk caching.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelStrongholdCacheMixin {

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;ensureStructuresGenerated()V"
            )
    )
    private void omnifix$setCachePath(
            ChunkGeneratorStructureState instance,
            Operation<Void> original,
            @Local(ordinal = 0, argsOnly = true) LevelStorageSource.LevelStorageAccess levelStorageAccess,
            @Local(ordinal = 0, argsOnly = true) ResourceKey<Level> dimension,
            @Local(ordinal = 0, argsOnly = true) MinecraftServer server
    ) {
        ((IChunkGeneratorStrongholdCache) (Object) instance)
                .omnifix$setStrongholdCachePath(levelStorageAccess.getDimensionPath(dimension), server);
        original.call(instance);
    }
}
