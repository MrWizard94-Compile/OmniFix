package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while a bee carries nectar and {@code Bee$BeeGrowCropGoal} is active, every
 * goal tick rolls {@code random.nextInt(adjustedTickDelay(30)) == 0} before scanning the
 * two blocks under the bee for {@code BEE_GROWABLES} and applying a single growth stage
 * (or cave-vine bonemeal). Dense apiaries over crop fields therefore re-enter block-state
 * lookups and optional {@code setBlockAndUpdate} / level-event paths on a 1/30 cadence
 * (difficulty-adjusted) without improving combat, hive return, or pollinate success.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code tick} only rewrites the sole {@code int}
 * literal {@code 30} → {@code 45} (nextInt gate via {@code adjustedTickDelay}: 1/30 → 1/45).
 * Audit (MC 1.20.1 mapped {@code Bee$BeeGrowCropGoal}):
 * <ul>
 *   <li>{@code tick}: sole {@code int 30} — {@code nextInt(adjustedTickDelay(30)) == 0}
 *       (or static {@code GROW_CHANCE = 30} inlined at that call site only)</li>
 *   <li>{@code canBeeUse}/{@code canBeeContinueToUse}: age / nectar / mob-griefing checks;
 *       float chance only — no {@code int 30}</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 30} in {@code tick} is therefore safe without
 * ordinal narrowing. If a {@code GROW_CHANCE} field exists and is only read from {@code tick},
 * rewriting the call-site constant is equivalent.
 *
 * <p>Intentionally untouched: pollinate / hive-locate / wander goals
 * ({@link BeePollinateCooldownMixin}, {@link BeeHiveLocateMixin}, {@link BeeWanderChanceMixin}),
 * anger / sting combat goals, and successful growth-stage application once the gate passes.
 *
 * <p>Trade-off: bees apply crop growth less often (~1/30 → ~1/45 of active grow-crop ticks,
 * +50% mean interval). Crop growth still occurs; apiaries over large farms advance crops more
 * slowly. Combat urgency and pollination are unchanged.
 *
 * <p>Unit: {@code perf.bee_grow_crop_interval} (gated by mixin plugin / FeatureUnits).
 *
 * @see BeePollinateCooldownMixin sibling nested Bee goal {@code @ModifyConstant} pattern
 * @see BeeHiveLocateMixin sibling nested Bee goal {@code @ModifyConstant} pattern
 * @see BeeWanderChanceMixin sibling nested Bee nextInt-gate {@code @ModifyConstant} pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeeGrowCropGoal")
public abstract class BeeGrowCropIntervalMixin {

    /**
     * Stretches the crop-growth random gate (vanilla {@code adjustedTickDelay(30)} →
     * {@code adjustedTickDelay(45)}).
     *
     * @param original vanilla constant (always 30 at the matched site)
     * @return stretched nextInt bound for grow-crop tick chance
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 30))
    private int omnifix$rarerCropGrowthTick(int original) {
        return 45;
    }
}
