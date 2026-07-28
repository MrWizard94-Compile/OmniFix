package org.omnifix.mixin.perf;

import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;

/**
 * Duck for attaching generator structure state to {@link net.minecraft.world.level.levelgen.structure.StructureCheck}.
 */
public interface IStructureCheck {
    void omnifix$setStructureState(ChunkGeneratorStructureState state);
}
