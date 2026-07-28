package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link SecondaryPoiSensor#doTick} scans profession secondary job-site blocks over
 * XZ {@code [-4, 4]} (9×9) and Y {@code [-2, 2]} (5) — i.e. {@code 9² × 5 = 405} block probes
 * per sensor pulse for every villager with a secondary POI set. Dense villages multiply this
 * into steady block-state lookup cost.
 *
 * <p>Policy: rewrite both XZ bound literals ({@code 4} and {@code -4}) so the box stays
 * symmetric at {@code [-3, 3]} (7×7). Y span stays {@code [-2, 2]}. Volume becomes
 * {@code 7² × 5 = 245} probes (~39.5% fewer). The unused local {@code int i = 4} is also
 * rewritten harmlessly to {@code 3}.
 *
 * <p>Trade-off: secondary job-site discovery only inside radius 3 instead of 4. Y reach and
 * sensor cadence are unchanged.
 *
 * <p>Unit: {@code perf.secondary_poi_radius}
 */
@Mixin(SecondaryPoiSensor.class)
public abstract class SecondaryPoiRadiusMixin {

    /**
     * Positive XZ bounds and dead local {@code i = 4} → {@code 3}.
     */
    @ModifyConstant(method = "doTick", constant = @Constant(intValue = 4))
    private int omnifix$smallerSecondaryPoiRadiusPos(int original) {
        return 3;
    }

    /**
     * Negative XZ loop starts {@code -4} → {@code -3} so the scan box stays centered.
     */
    @ModifyConstant(method = "doTick", constant = @Constant(intValue = -4))
    private int omnifix$smallerSecondaryPoiRadiusNeg(int original) {
        return -3;
    }
}
