package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: when a drowned is on dry land during daytime,
 * {@code Drowned$DrownedGoToWaterGoal#canUse} calls private {@code getWaterPos}, which rolls up
 * to {@code 10} random {@link net.minecraft.core.BlockPos} samples around the mob looking for
 * {@code Blocks.WATER}:
 * <pre>
 *   for (int i = 0; i &lt; 10; i++) {
 *     pos.offset(nextInt(20) - 10, 2 - nextInt(8), nextInt(20) - 10)
 *   }
 * </pre>
 * Each sample is a block-state probe; failed searches pay the full 10-probe cost every
 * goal-selector pass for every dry daytime drowned. Riverbanks and coastal farms with many
 * stranded drowneds therefore burn AI ticks on redundant water-block lottery samples.
 *
 * <p>Policy: four {@code @ModifyConstant} injectors on private {@code getWaterPos} only:
 * <ul>
 *   <li>{@code int 10} ordinal {@code 0} → {@code 7} — loop sample count ({@code i &lt; 10})</li>
 *   <li>{@code int 20} (both sites) → {@code 16} — {@code nextInt} exclusive upper bound on X and Z</li>
 *   <li>{@code int 10} ordinal {@code 1} → {@code 8} — X subtract offset ({@code nextInt(...) - 10})</li>
 *   <li>{@code int 10} ordinal {@code 2} → {@code 8} — Z subtract offset</li>
 * </ul>
 * Resulting X/Z sample: vanilla {@code nextInt(20) - 10 ∈ [-10, 9]} becomes
 * {@code nextInt(16) - 8 ∈ [-8, 7]} (symmetric half-width 8, 16 values). Y sampling
 * ({@code 2 - nextInt(8)}) is intentionally left alone.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code Drowned$DrownedGoToWaterGoal#getWaterPos},
 * verified via {@code javap -c}):
 * <ul>
 *   <li>{@code bipush 10} ordinal 0 — loop compare ({@code i &lt; 10} / {@code if_icmpge})</li>
 *   <li>{@code bipush 20} — {@code nextInt(20)} X (first of two)</li>
 *   <li>{@code bipush 10} ordinal 1 — X {@code isub} offset</li>
 *   <li>{@code iconst_2} / {@code bipush 8} — Y base / {@code nextInt(8)}; not matched</li>
 *   <li>{@code bipush 20} — {@code nextInt(20)} Z (second of two)</li>
 *   <li>{@code bipush 10} ordinal 2 — Z {@code isub} offset</li>
 * </ul>
 * There is no {@code bipush -10}; offsets are positive {@code 10} subtracted via {@code isub}.
 * Changing every {@code 10} without ordinals would rewrite the loop bound and both offsets to
 * the same value, breaking X/Z symmetry when the span is also reduced (e.g. {@code nextInt(16)-7}
 * is asymmetric). Ordinals keep loop count (→7) and offsets (→8) independent. Both {@code 20}
 * sites are identical in role, so the {@code intValue = 20} injector is safe without ordinal.
 *
 * <p>Intentionally untouched: {@code canUse} day / {@code isInWater} gates (must stay hot so a
 * found water pos still starts the goal immediately), {@code canContinueToUse}/{@code start}
 * navigation, Y sample constants, combat / trident goals, and {@code DrownedSwimUpGoal} /
 * {@code DrownedGoToBeachGoal}.
 *
 * <p>Trade-off: daytime dry drowned water lottery uses fewer samples (10→7, −30%) inside a
 * slightly smaller XZ box (half-width 10→8). Water 9–10 blocks away horizontally is no longer
 * sampled until the drowned moves closer; failed searches are cheaper. Once a water pos is
 * found, pathing is vanilla. Not panic / flee-from-hurt / fire-urgency — this goal only runs
 * for dry-land daytime water seek; no urgency exception required.
 *
 * <p>Unit: {@code perf.drowned_water_search} (gated by mixin plugin / FeatureUnits).
 *
 * @see SilverfishWakeScanMixin sibling multi-constant box shrink with positive/negative literals
 * @see BeeHiveLocateMixin sibling nested-goal dual {@code @ModifyConstant} pattern
 * @see GhastWanderRadiusMixin sibling nested-goal radius shrink pattern
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 * @see TryFindWaterThrottleMixin related amphibious water-seek throttle (different goal)
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Drowned$DrownedGoToWaterGoal")
public abstract class DrownedWaterSearchMixin {

    /**
     * Rewrite the first {@code 10} in {@code getWaterPos} (loop sample bound
     * {@code for (i = 0; i &lt; 10; i++)}) to {@code 7}.
     *
     * <p>Bytecode ordinal 0 among {@code bipush 10} sites (loop compare before body offsets).
     *
     * @param original vanilla constant (always 10 at the matched site)
     * @return reduced water-sample attempt count
     */
    @ModifyConstant(method = "getWaterPos", constant = @Constant(intValue = 10, ordinal = 0))
    private int omnifix$fewerWaterSamples(int original) {
        return 7;
    }

    /**
     * Rewrite every {@code 20} in {@code getWaterPos} ({@code nextInt(20)} exclusive upper
     * bound on X and Z) to {@code 16}.
     *
     * @param original vanilla constant (always 20 at the matched sites)
     * @return reduced horizontal {@code nextInt} exclusive upper bound
     */
    @ModifyConstant(method = "getWaterPos", constant = @Constant(intValue = 20))
    private int omnifix$shrinkWaterNextIntSpan(int original) {
        return 16;
    }

    /**
     * Rewrite the second {@code 10} in {@code getWaterPos} (X {@code nextInt(...) - 10} offset)
     * to {@code 8} so the reduced span stays symmetric: {@code nextInt(16) - 8}.
     *
     * <p>Bytecode ordinal 1 among {@code bipush 10} sites.
     *
     * @param original vanilla constant (always 10 at the matched site)
     * @return reduced X subtract offset
     */
    @ModifyConstant(method = "getWaterPos", constant = @Constant(intValue = 10, ordinal = 1))
    private int omnifix$shrinkWaterOffsetX(int original) {
        return 8;
    }

    /**
     * Rewrite the third {@code 10} in {@code getWaterPos} (Z {@code nextInt(...) - 10} offset)
     * to {@code 8} so the reduced span stays symmetric: {@code nextInt(16) - 8}.
     *
     * <p>Bytecode ordinal 2 among {@code bipush 10} sites.
     *
     * @param original vanilla constant (always 10 at the matched site)
     * @return reduced Z subtract offset
     */
    @ModifyConstant(method = "getWaterPos", constant = @Constant(intValue = 10, ordinal = 2))
    private int omnifix$shrinkWaterOffsetZ(int original) {
        return 8;
    }
}
