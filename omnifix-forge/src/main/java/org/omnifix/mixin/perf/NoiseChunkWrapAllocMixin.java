package org.omnifix.mixin.perf;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * NoiseChunk.wrap uses Map.computeIfAbsent (lambda alloc) and a heavier map. Use open-hash map
 * and get/put without allocation.
 */
@Mixin(value = NoiseChunk.class, priority = 100)
public abstract class NoiseChunkWrapAllocMixin {

    @Shadow
    @Final
    @Mutable
    private Map<DensityFunction, DensityFunction> wrapped = new Object2ObjectOpenHashMap<>();

    @Shadow
    protected abstract DensityFunction wrapNew(DensityFunction densityFunction);

    /**
     * @author OmniFix
     * @reason avoid lambda allocation on wrap cache miss
     */
    @Overwrite
    protected DensityFunction wrap(DensityFunction unwrapped) {
        DensityFunction func = this.wrapped.get(unwrapped);
        if (func == null) {
            func = this.wrapNew(unwrapped);
            this.wrapped.put(unwrapped, func);
        }
        return func;
    }
}
