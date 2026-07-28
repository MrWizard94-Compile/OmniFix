package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Panda$PandaSitGoal} scans for nearby bamboo {@code ItemEntity} candidates
 * on every goal evaluation. {@code canUse} builds
 * {@code getEntitiesOfClass(ItemEntity, inflate(6.0D, 6.0D, 6.0D), ...)} to decide whether any
 * sit/eat item is present; when the goal starts, {@code start} re-scans with
 * {@code inflate(8.0D, 8.0D, 8.0D)} to pick the nearest item and begin pathing. Dense panda
 * groups near item dumps (bamboo farms, droppers, hopper thrash) therefore walk 12×12×12 and
 * 16×16×16 AABB item lists on the goal-selector cadence even when loot is far outside practical
 * sit range, dominating AI time without improving roll / panic / combat behavior.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on the nested goal only:
 * <ul>
 *   <li>{@code canUse}: every {@code double} literal {@code 6.0D} → {@code 4.0D}
 *       ({@code inflate(6,6,6)} → {@code inflate(4,4,4)})</li>
 *   <li>{@code start}: every {@code double} literal {@code 8.0D} → {@code 6.0D}
 *       ({@code inflate(8,8,8)} → {@code inflate(6,6,6)})</li>
 * </ul>
 * Both injectors intentionally rewrite all three inflate arms (X/Y/Z share the same constant
 * per method), shrinking the full search volume rather than only horizontal range.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code Panda$PandaSitGoal}):
 * <ul>
 *   <li>{@code canUse}: sole {@code double} literal group {@code 6.0D} — three inflate arms of
 *       the ItemEntity candidate AABB. No other {@code 6.0} doubles appear in {@code canUse}.</li>
 *   <li>{@code start}: sole {@code double} literal group {@code 8.0D} — three inflate arms of
 *       the nearest-item AABB used when the goal begins. No other {@code 8.0} doubles appear
 *       in {@code start}.</li>
 *   <li>{@code canContinueToUse}/{@code tick}/{@code stop}: out of scope — no injectors touch
 *       sit duration, eat timing, or goal teardown.</li>
 * </ul>
 * {@code ModifyConstant} on those two constant descriptors is therefore safe without ordinal
 * narrowing. Combat urgency is not touched: this goal only notices bamboo items for sit/eat;
 * hurt revenge, panic, and attack goals are unchanged.
 *
 * <p>Trade-off: pandas notice bamboo items for sit/eat in a smaller AABB. Presence gate half-
 * extent drops 6→4 (full box 12→8; volume
 * {@code (4/6)^3 ≈ 30%} of vanilla). Start nearest-item half-extent drops 8→6 (full box 16→12;
 * volume {@code (6/8)^3 ≈ 42%} of vanilla). Bamboo items between the old and new radii are
 * ignored until the panda walks closer; once an item is acquired, pathing / sit / eat duration
 * remain vanilla.
 *
 * <p>Unit: {@code perf.panda_sit_item_scan} (gated by mixin plugin / FeatureUnits).
 *
 * @see PhantomPlayerScanMixin sibling nested-goal Item/Player AABB shrink via double
 *      {@code @ModifyConstant}
 * @see NearestItemSensorRangeMixin sibling ItemEntity inflate shrink pattern
 * @see BeeWanderChanceMixin sibling nested animal goal {@code targets = "...$Inner"} pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Panda$PandaSitGoal")
public abstract class PandaSitItemScanMixin {

    /**
     * Shrink {@code canUse} ItemEntity scan {@code inflate(6.0D, 6.0D, 6.0D)} to
     * {@code inflate(4.0D, 4.0D, 4.0D)} (all three arms share the matched constant).
     *
     * @param original vanilla constant (always 6.0 at the matched sites)
     * @return reduced half-extent for the sit-item presence AABB
     */
    @ModifyConstant(method = "canUse", constant = @Constant(doubleValue = 6.0D))
    private double omnifix$shrinkPandaSitCanUseScan(double original) {
        return 4.0D;
    }

    /**
     * Shrink {@code start} nearest-item scan {@code inflate(8.0D, 8.0D, 8.0D)} to
     * {@code inflate(6.0D, 6.0D, 6.0D)} (all three arms share the matched constant).
     *
     * @param original vanilla constant (always 8.0 at the matched sites)
     * @return reduced half-extent for the start nearest-item AABB
     */
    @ModifyConstant(method = "start", constant = @Constant(doubleValue = 8.0D))
    private double omnifix$shrinkPandaSitStartScan(double original) {
        return 6.0D;
    }
}
