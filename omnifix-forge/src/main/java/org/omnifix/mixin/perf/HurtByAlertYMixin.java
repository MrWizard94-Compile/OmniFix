package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link HurtByTargetGoal#alertOthers} builds a pack-alert AABB via
 * {@code unitCubeFromLowerCorner(...).inflate(d0, 10.0D, d0)} then
 * {@code getEntitiesOfClass} over every same-class mob in that box. Horizontal extent
 * follows follow distance ({@code d0}); the fixed Y half-extent of 10 blocks yields a tall
 * cylinder that walks multi-level farms, towers, and stacked pens even when packmates are
 * not vertically relevant. Alert runs once per revenge {@code start()} when
 * {@code alertSameType} is set (wolves, zoglins, etc.), so each hurt event pays the full
 * tall-box scan.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code alertOthers} rewrites the sole
 * {@code double} literal {@code 10.0D} → {@code 7.0D} (Y inflate only). Horizontal
 * {@code d0} is a runtime follow-distance value, not a constant, so XZ reach is unchanged.
 * <strong>Revenge {@code canUse} is intentionally untouched</strong> — hurt-target selection
 * stays hot every goal evaluation; only the pack-alert vertical volume is shrunk.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code alertOthers}): method body contains exactly one
 * {@code double} constant — {@code 10.0} on the {@code inflate} Y arm. Other locals
 * ({@code d0}, entity refs, flags) are not matchable by this injector. Class field
 * {@code ALERT_RANGE_Y = 10} is not referenced by the mapped {@code alertOthers} body
 * (hardcoded double), so it is out of scope. {@code ModifyConstant} on
 * {@code doubleValue = 10.0D} is therefore safe without ordinal narrowing and cannot hit
 * {@code canUse} / {@code start}.
 *
 * <p>Trade-off: packmates more than 7 blocks above/below the hurt mob are no longer alerted
 * (vanilla 10). Horizontal alert radius still uses follow distance. Solo revenge targeting
 * is unchanged; multi-level pens may alert slightly fewer vertical neighbors, cutting
 * {@code getEntitiesOfClass} volume by {@code 7/10 = 30%} on the Y axis.
 *
 * <p>Unit: {@code perf.hurt_alert_y} (gated by mixin plugin / FeatureUnits).
 */
@Mixin(HurtByTargetGoal.class)
public abstract class HurtByAlertYMixin {

    /**
     * Rewrite {@code inflate(d0, 10.0D, d0)} Y half-extent to {@code 7.0D}.
     * Does not touch {@code canUse} revenge hot path.
     */
    @ModifyConstant(method = "alertOthers", constant = @Constant(doubleValue = 10.0D))
    private double omnifix$shrinkAlertY(double original) {
        return 7.0D;
    }
}
