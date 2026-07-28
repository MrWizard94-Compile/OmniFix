package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Phantom$PhantomAttackPlayerTargetGoal#canUse} periodically rebuilds a
 * player target via {@code getEntitiesOfClass(Player, inflate(16, 64, 16), ...)} then picks
 * the nearest valid candidate. After each scan vanilla schedules the next with
 * {@code nextScanTick = reducedTickDelay(60)}. Idle phantoms (night sky, End-adjacent void
 * islands, long-range patrols) therefore re-walk a 32×128×32 AABB of players every ~3s even
 * when no survivor is nearby, dominating AI time for large phantom flocks without improving
 * combat once a target is already held.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on {@code canUse} only:
 * <ul>
 *   <li>{@code intValue = 60} → {@code 90} — post-scan {@code reducedTickDelay(60)} reschedule
 *       (+50%)</li>
 *   <li>{@code doubleValue = 16.0D} → {@code 12.0D} — both X and Z arms of
 *       {@code inflate(16.0D, 64.0D, 16.0D)}; Y {@code 64.0D} is a different constant and is
 *       intentionally left alone</li>
 * </ul>
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code canUse}):
 * <ul>
 *   <li>The only {@code int} literal {@code 60} is the argument to {@code reducedTickDelay(60)}
 *       after a completed scan. Field initializer {@code nextScanTick = 20} lives on the nested
 *       class field / constructor path, not as a {@code 60} in {@code canUse}, so initial
 *       warm-up delay stays vanilla.</li>
 *   <li>The only {@code double} literals {@code 16.0} are the X and Z inflate half-extents.
 *       Y half-extent {@code 64.0} is unmatched by {@code doubleValue = 16.0D}. No other
 *       {@code 16.0} doubles appear in {@code canUse}.</li>
 *   <li>Follow / targeting range values that use {@code 64} (int or other methods) are out of
 *       scope; this injector never matches them.</li>
 * </ul>
 * {@code ModifyConstant} on those two constant descriptors is therefore safe without ordinal
 * narrowing and does not touch revenge urgency, dive/attack goals, or the initial 20-tick
 * scan delay.
 *
 * <p>Trade-off: phantoms rescan for player targets less often (~4.5s base vs ~3s) with a
 * slightly smaller XZ search box (half-extent 12 vs 16; Y still 64). Players between 12–16
 * blocks horizontally may be acquired one rescan later or only after the phantom moves
 * closer; dive / attack once targeted is unchanged. Cuts player-list AABB volume by
 * {@code (12/16)^2 = 56%} in XZ area and stretches scan cadence +50%.
 *
 * <p>Unit: {@code perf.phantom_player_scan} (gated by mixin plugin / FeatureUnits).
 *
 * @see BeePollinateCooldownMixin sibling nested-goal {@code targets = "...$Inner"} pattern
 * @see NearestItemSensorRangeMixin sibling XZ-only inflate shrink via double ModifyConstant
 * @see PlayerSensorRangeMixin sibling player-scan range shrink pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomAttackPlayerTargetGoal")
public abstract class PhantomPlayerScanMixin {

    /**
     * Stretch post-scan reschedule {@code reducedTickDelay(60)} → {@code 90}. Does not match
     * field initializer {@code nextScanTick = 20}.
     *
     * @param original vanilla constant (always 60 at the matched site)
     * @return stretched base delay before the next player AABB scan
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 60))
    private int omnifix$stretchPhantomPlayerRescan(int original) {
        return 90;
    }

    /**
     * Shrink {@code inflate(16.0D, 64.0D, 16.0D)} X and Z half-extents to {@code 12.0D}.
     * Y half-extent {@code 64.0D} is not matched by this constant descriptor.
     *
     * @param original vanilla constant (always 16.0 at the matched sites)
     * @return reduced horizontal half-extent for the player search AABB
     */
    @ModifyConstant(method = "canUse", constant = @Constant(doubleValue = 16.0D))
    private double omnifix$shrinkPhantomPlayerScanXZ(double original) {
        return 12.0D;
    }
}
