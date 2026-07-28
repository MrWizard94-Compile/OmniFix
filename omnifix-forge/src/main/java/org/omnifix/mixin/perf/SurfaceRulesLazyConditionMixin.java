package org.omnifix.mixin.perf;

import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Several surface conditions extend LazyCondition but are not interned and invalidate every block
 * Y change — the cache is pure overhead. Call compute() directly via test().
 */
@Mixin(targets = {
        "net.minecraft.world.level.levelgen.SurfaceRules$BiomeConditionSource$1BiomeCondition",
        "net.minecraft.world.level.levelgen.SurfaceRules$StoneDepthCheck$1StoneDepthCondition",
        "net.minecraft.world.level.levelgen.SurfaceRules$VerticalGradientConditionSource$1VerticalGradientCondition",
        "net.minecraft.world.level.levelgen.SurfaceRules$WaterConditionSource$1WaterCondition",
        "net.minecraft.world.level.levelgen.SurfaceRules$YConditionSource$1YCondition"
})
public abstract class SurfaceRulesLazyConditionMixin extends SurfaceRules.LazyCondition {

    protected SurfaceRulesLazyConditionMixin(SurfaceRules.Context context) {
        super(context);
    }

    @Override
    public boolean test() {
        return compute();
    }
}
