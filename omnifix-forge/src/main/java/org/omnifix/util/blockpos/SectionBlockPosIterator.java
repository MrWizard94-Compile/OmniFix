package org.omnifix.util.blockpos;

import net.minecraft.core.BlockPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Fast 16×16×16 section position iterator without Guava allocation per step. */
public final class SectionBlockPosIterator implements Iterator<BlockPos> {

    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private int index;
    private final int baseX;
    private final int baseY;
    private final int baseZ;

    public SectionBlockPosIterator(int baseX, int baseY, int baseZ) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.baseZ = baseZ;
    }

    public SectionBlockPosIterator(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean hasNext() {
        return index < 4096;
    }

    @Override
    public BlockPos next() {
        int i = index;
        if (i >= 4096) {
            throw new NoSuchElementException();
        }
        index = i + 1;
        this.pos.set(this.baseX + (i & 15), this.baseY + ((i >> 8) & 15), this.baseZ + ((i >> 4) & 15));
        return this.pos;
    }
}
