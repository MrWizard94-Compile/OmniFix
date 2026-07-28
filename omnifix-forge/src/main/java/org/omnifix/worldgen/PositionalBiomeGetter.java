package org.omnifix.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reusable biome supplier for surface-rule Y updates. Vanilla allocates a new lambda/supplier
 * every {@code updateY}; this keeps one object and only re-resolves when coordinates change.
 */
public final class PositionalBiomeGetter implements Supplier<Holder<Biome>> {

    private final Function<BlockPos, Holder<Biome>> biomeGetter;
    private final BlockPos.MutableBlockPos pos;
    private int nextX;
    private int nextY;
    private int nextZ;
    private volatile Holder<Biome> curBiome;

    public PositionalBiomeGetter(Function<BlockPos, Holder<Biome>> biomeGetter, BlockPos.MutableBlockPos pos) {
        this.biomeGetter = biomeGetter;
        this.pos = pos;
    }

    public void update(int nextX, int nextY, int nextZ) {
        this.nextX = nextX;
        this.nextY = nextY;
        this.nextZ = nextZ;
        this.curBiome = null;
    }

    @Override
    public Holder<Biome> get() {
        Holder<Biome> biome = curBiome;
        if (biome == null) {
            curBiome = biome = biomeGetter.apply(pos.set(nextX, nextY, nextZ));
        }
        return biome;
    }
}
