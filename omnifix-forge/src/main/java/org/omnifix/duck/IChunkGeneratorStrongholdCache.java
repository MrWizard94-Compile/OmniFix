package org.omnifix.duck;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

/**
 * Duck for {@link net.minecraft.world.level.chunk.ChunkGeneratorStructureState} so stronghold ring
 * positions can be disk-cached per dimension path.
 */
public interface IChunkGeneratorStrongholdCache {

    void omnifix$setStrongholdCachePath(Path cachePath, MinecraftServer server);
}
