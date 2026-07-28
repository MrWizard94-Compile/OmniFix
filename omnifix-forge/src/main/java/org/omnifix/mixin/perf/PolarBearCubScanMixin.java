package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: adult {@code PolarBear$PolarBearAttackPlayersGoal#canUse} first delegates to
 * super (nearest-player target selection), then — only when a player target is already
 * eligible — re-walks every nearby polar bear via
 * {@code level.getEntitiesOfClass(PolarBear.class, getBoundingBox().inflate(8.0D, 4.0D, 8.0D))}
 * to decide whether a baby cub is close enough to justify attacking the player. Dense ice
 * biomes and cub-farm pens therefore pay a full 16×8×16 cub AABB scan on every successful
 * super-{@code canUse} pulse even when cubs sit well inside a tighter shell around the adult.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on {@code canUse} only:
 * <ul>
 *   <li>{@code double 8.0D → 6.0D} — both X and Z arms of
 *       {@code inflate(8.0D, 4.0D, 8.0D)}</li>
 *   <li>{@code double 4.0D → 3.0D} — Y arm of the same inflate</li>
 * </ul>
 * Half-extent product shrinks {@code 8·4·8 = 256 → 6·3·6 = 108} (~42% of vanilla scan
 * volume). Cub-proximity still gates player aggression; only the scan box shrinks mildly.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code PolarBear$PolarBearAttackPlayersGoal#canUse}):
 * <ul>
 *   <li>{@code double 8.0D} — exactly two sites: X and Z inflate half-extents (same constant
 *       pool entry loaded twice)</li>
 *   <li>{@code double 4.0D} — sole site: Y inflate half-extent</li>
 *   <li>{@code double 0.5D} — lives only on {@code getFollowDistance} ({@code super * 0.5}),
 *       not in {@code canUse}, so follow-range halving is intentionally untouched</li>
 *   <li>{@code int 20} — constructor target-interval only; out of scope</li>
 * </ul>
 * {@code ModifyConstant} on those two double descriptors is therefore safe without ordinal
 * narrowing and cannot hit revenge / super target acquisition volumes (parent methods) or
 * follow-distance scaling.
 *
 * <p>Trade-off: adults protect cubs with a smaller scan box when deciding to attack players
 * (XZ half-extent 8→6, Y 4→3). Cubs 6–8 blocks away horizontally or 3–4 blocks vertically
 * no longer arm player-aggression until the adult moves closer; once armed, melee / anger /
 * super targeting behave as vanilla. Combat urgency of an already-selected player target is
 * preserved — this only trims the cub-presence probe volume after super-{@code canUse}
 * already found a player candidate.
 *
 * <p>Unit: {@code perf.polar_bear_cub_scan} (gated by mixin plugin / FeatureUnits).
 *
 * @see PhantomPlayerScanMixin sibling nested-goal player-scan inflate {@code @ModifyConstant}
 * @see TargetSearchYMixin sibling target AABB Y-inflate shrink pattern
 * @see HurtByAlertYMixin sibling pack-alert volume shrink (revenge {@code canUse} stays hot)
 * @see HoglinRepellentRangeMixin sibling dual-axis scan half-extent shrink 8/4 → 6/3
 */
@Mixin(targets = "net.minecraft.world.entity.animal.PolarBear$PolarBearAttackPlayersGoal")
public abstract class PolarBearCubScanMixin {

    /**
     * Rewrite every {@code 8.0D} in {@code canUse} (X and Z arms of
     * {@code inflate(8.0D, 4.0D, 8.0D)}) to {@code 6.0D}.
     *
     * @param original vanilla constant (always 8.0 at the matched sites)
     * @return reduced horizontal half-extent for the baby-cub presence AABB
     */
    @ModifyConstant(method = "canUse", constant = @Constant(doubleValue = 8.0D))
    private double omnifix$shrinkCubScanXZ(double original) {
        return 6.0D;
    }

    /**
     * Rewrite the sole {@code 4.0D} in {@code canUse} (Y arm of
     * {@code inflate(8.0D, 4.0D, 8.0D)}) to {@code 3.0D}.
     *
     * @param original vanilla constant (always 4.0 at the matched site)
     * @return reduced vertical half-extent for the baby-cub presence AABB
     */
    @ModifyConstant(method = "canUse", constant = @Constant(doubleValue = 4.0D))
    private double omnifix$shrinkCubScanY(double original) {
        return 3.0D;
    }
}
