package org.omnifix.mixin.perf;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * Avoid iterator allocation in SurfaceRules.SequenceRule.tryApply.
 */
@Mixin(value = SurfaceRules.SequenceRule.class, priority = 100)
public abstract class SurfaceSequenceRuleAllocMixin {

    @Shadow
    @Final
    private List<SurfaceRules.SurfaceRule> rules;

    /**
     * @author OmniFix
     * @reason index loop instead of for-each iterator
     */
    @Overwrite
    public BlockState tryApply(int x, int y, int z) {
        int s = this.rules.size();
        for (int i = 0; i < s; i++) {
            BlockState state = this.rules.get(i).tryApply(x, y, z);
            if (state != null) {
                return state;
            }
        }
        return null;
    }
}
