package org.omnifix.blockstate;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.omnifix.duck.IBlockState;

/** Marks every registered BlockState cache invalid so rebuilds can be deferred until first use. */
public final class BlockStateCacheHandler {

    private BlockStateCacheHandler() {}

    public static void invalidateCache() {
        synchronized (BlockBehaviour.BlockStateBase.class) {
            for (BlockState blockState : Block.BLOCK_STATE_REGISTRY) {
                ((IBlockState) blockState).omnifix$clearCache();
            }
        }
    }
}
