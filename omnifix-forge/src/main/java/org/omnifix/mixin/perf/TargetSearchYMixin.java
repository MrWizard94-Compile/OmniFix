package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link NearestAttackableTargetGoal#getTargetSearchArea} builds the nearest-target
 * candidate AABB as {@code mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance)}.
 * Horizontal half-extent follows the goal's follow range; vertical half-extent is a hard-coded
 * {@code 4.0D} on every {@code findTarget} entity-class scan. Dense hostile packs therefore walk
 * an 8-block-tall slab of entities on each target acquisition pulse even when relevant targets
 * sit near the same Y as the mob.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code getTargetSearchArea} rewrites the sole vertical
 * inflate literal {@code 4.0D} → {@code 3.0D}. Horizontal arms remain {@code pTargetDistance}
 * (parameter, not a constant) and are intentionally untouched so follow-range targeting stays
 * vanilla on X/Z.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code getTargetSearchArea}): the method body contains
 * exactly one double constant — the Y half-extent {@code 4.0D}. The X and Z inflate args are
 * the {@code pTargetDistance} parameter. No other doubles appear in the method, so
 * {@code @Constant(doubleValue = 4.0D)} is unique without ordinal narrowing and cannot collide
 * with horizontal range.
 *
 * <p>Trade-off: target acquisition vertical box is slightly thinner (Y half-extent 3 instead of
 * 4 → full height 6 instead of 8). Targets more than ~3 blocks above/below the mob's bounding
 * box are ignored until the mob climbs/descends closer. Combat urgency is preserved: this only
 * shrinks the search volume; {@code canUse}/revenge hot paths and follow-range XZ are unchanged.
 *
 * <p>Unit: {@code perf.target_search_y} (gated by mixin plugin / FeatureUnits).
 *
 * @see TargetGoalIntervalMixin complementary canUse interval stretch (fewer findTarget pulses)
 */
@Mixin(NearestAttackableTargetGoal.class)
public abstract class TargetSearchYMixin {

    /**
     * Rewrite the Y inflate arm of {@code getTargetSearchArea} from {@code 4.0D} to {@code 3.0D}.
     * X/Z use {@code pTargetDistance} and are not matched.
     */
    @ModifyConstant(method = "getTargetSearchArea", constant = @Constant(doubleValue = 4.0D))
    private double omnifix$shrinkTargetSearchY(double original) {
        return 3.0D;
    }
}
