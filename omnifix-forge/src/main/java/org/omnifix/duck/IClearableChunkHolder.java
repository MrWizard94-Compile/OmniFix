package org.omnifix.duck;

import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.FullChunkStatus;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Suspends protochunk futures on holders that are not FULL (or adjacent) so memory can be reclaimed.
 */
public interface IClearableChunkHolder {

    /** Do not drop FULL chunks or their immediate neighbors. */
    int LOWEST_DROPPABLE_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.FULL) + 2;

    void omnifix$resetProtoChunkFutures();

    AtomicInteger omnifix$getGenerationRefCount();
}
