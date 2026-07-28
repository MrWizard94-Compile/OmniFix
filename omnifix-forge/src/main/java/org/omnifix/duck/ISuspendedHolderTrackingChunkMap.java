package org.omnifix.duck;

import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.Executor;

public interface ISuspendedHolderTrackingChunkMap {
    void omnifix$markForSuspensionCheck(ChunkPos pos);

    Executor omnifix$getMainThreadExecutor();
}
