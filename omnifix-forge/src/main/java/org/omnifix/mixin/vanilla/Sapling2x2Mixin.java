package org.omnifix.mixin.vanilla;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * MC-8187 — 2×2 spruce / jungle / dark oak saplings fail to grow when blocks sit north and/or west
 * of the formation.
 *
 * <p>Root cause (1.20.1 {@link TreeFeature#getMaxFreeTreeHeight}): mega trees use
 * {@code TwoLayersFeatureSize(1, 1, 2)}, so at height 0 the free-space radius is {@code 1}. The
 * nested loops then scan offsets {@code [-1..1]} on X/Z centered on the <em>north-west</em> sapling
 * of the 2×2. That extra ring north and west of the trunk is never occupied by the giant trunk
 * (which only places logs at {@code (0,0)}, {@code (1,0)}, {@code (0,1)}, {@code (1,1)}), so a wall
 * or farm block on those sides falsely aborts growth.
 *
 * <p>Fix (standard, ported from the widely used MC-8187 Fabric patch): when the free-space size at
 * height 0 is {@code 1} and a loop variable is about to start at {@code -1}, clamp it to {@code 0}
 * so only the 2×2 footprint is tested at the trunk base. Higher foliage radii ({@code size >= 2})
 * are left unchanged.
 *
 * <p>Bytecode local indices for {@code getMaxFreeTreeHeight} on 1.20.1 official mappings:
 * {@code k} (X) = 8, {@code l} (Z) = 9 — matching the Fabric intermediary patch targets.
 */
@Mixin(TreeFeature.class)
public abstract class Sapling2x2Mixin {

    @ModifyVariable(method = "getMaxFreeTreeHeight", at = @At(value = "STORE", ordinal = 0), index = 8, require = 0)
    private int omnifix$fixTrunkBaseX(int startValue, LevelSimulatedReader level, int height, BlockPos pos, TreeConfiguration config) {
        return clampNegativeTrunkBaseOffset(startValue, height, config);
    }

    @ModifyVariable(method = "getMaxFreeTreeHeight", at = @At(value = "STORE", ordinal = 0), index = 9, require = 0)
    private int omnifix$fixTrunkBaseZ(int startValue, LevelSimulatedReader level, int height, BlockPos pos, TreeConfiguration config) {
        return clampNegativeTrunkBaseOffset(startValue, height, config);
    }

    private static int clampNegativeTrunkBaseOffset(int startValue, int treeHeight, TreeConfiguration config) {
        // Roughly: height layer 0 uses size 1 for mega trunks → start value is -1.
        if (startValue == -1 && config.minimumSize.getSizeAtHeight(treeHeight, 0) == 1) {
            return 0;
        }
        return startValue;
    }
}
