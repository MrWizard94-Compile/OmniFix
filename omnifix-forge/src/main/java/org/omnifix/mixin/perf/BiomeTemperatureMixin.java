package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 1.20.1 Biome temperature caching is ineffective (high miss rate) and only adds overhead.
 * Lithium/ModernFix approach: always compute height-adjusted temperature.
 */
@Mixin(Biome.class)
public abstract class BiomeTemperatureMixin {

    @Shadow
    protected abstract float getHeightAdjustedTemperature(BlockPos pos);

    /**
     * @author OmniFix (Lithium/ModernFix-class)
     * @reason remove ineffective temperature cache
     */
    @Overwrite
    private float getTemperature(BlockPos pos) {
        return this.getHeightAdjustedTemperature(pos);
    }
}
