package org.omnifix.mixin.perf;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * Avoid iterator allocation in MaterialRuleList.calculate hot path during chunk generation.
 */
@Mixin(value = MaterialRuleList.class, priority = 100)
public abstract class MaterialRuleListAllocMixin {

    @Shadow
    @Final
    private List<NoiseChunk.BlockStateFiller> materialRuleList;

    /**
     * @author OmniFix
     * @reason index loop instead of for-each iterator
     */
    @Overwrite
    @Nullable
    public BlockState calculate(DensityFunction.FunctionContext arg) {
        BlockState state = null;
        int s = this.materialRuleList.size();
        for (int i = 0; state == null && i < s; i++) {
            state = this.materialRuleList.get(i).calculate(arg);
        }
        return state;
    }
}
