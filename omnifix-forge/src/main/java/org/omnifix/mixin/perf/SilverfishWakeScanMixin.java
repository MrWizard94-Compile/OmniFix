package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: when a silverfish is hurt (and does not die in one hit),
 * {@code Silverfish$SilverfishWakeUpFriendsGoal#tick} eventually expands a spiral
 * block scan over XZ {@code [-10, 10]} and Y {@code [-5, 5]} looking for
 * {@code InfestedBlock} instances to break open. That box is
 * {@code 21 × 11 × 21 = 4851} block-state probes per wake pulse (worst case, no
 * early random breakout). Stronghold fights and poison-farm setups re-run this
 * full volume after each surviving hurt.
 *
 * <p>Policy: four {@code @ModifyConstant} injectors on {@code tick} only:
 * <ul>
 *   <li>{@code int 5 → 4} and {@code int -5 → -4} — Y spiral bounds</li>
 *   <li>{@code int 10 → 8} and {@code int -10 → -8} — both XZ spiral axes
 *       (j and k loop upper/lower bounds)</li>
 * </ul>
 * Intentionally untouched: {@code lookForFriends} / {@code notifyHurt} path that
 * seeds {@code lookForFriends = adjustedTickDelay(20)} (hurt cadence must stay
 * vanilla so combat still arms the goal immediately), countdown decrement,
 * spiral step arithmetic ({@code 0}/{@code 1}), and {@code setBlock} flag
 * {@code 3}.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code Silverfish$SilverfishWakeUpFriendsGoal#tick}):
 * <ul>
 *   <li>{@code int 5} — sole site: Y loop upper bound {@code i <= 5}</li>
 *   <li>{@code int -5} — sole site: Y loop lower bound {@code i >= -5}</li>
 *   <li>{@code int 10} — exactly two sites: XZ j and k upper bounds</li>
 *   <li>{@code int -10} — exactly two sites: XZ j and k lower bounds</li>
 *   <li>{@code int 20} — lives only on {@code lookForFriends} (not {@code tick})</li>
 *   <li>{@code int 0}/{@code 1}/{@code 3} — spiral step / setBlock flags; not matched</li>
 * </ul>
 * {@code ModifyConstant} on those four descriptors is therefore safe without
 * ordinal narrowing and cannot touch the hurt-seed delay.
 *
 * <p>Trade-off: wake-friends infest scan box shrinks from {@code 21×11×21} to
 * {@code 17×9×17} ({@code 2601} probes, ~46% volume cut). Infested blocks 9–10
 * blocks away on X/Z or 5 blocks away on Y are ignored until the silverfish
 * moves closer. Swarm density near the hurt silverfish is unchanged; only the
 * outer shell of the rescue radius is trimmed. Hurt-to-wake arming delay remains
 * vanilla ({@code adjustedTickDelay(20)}).
 *
 * <p>Unit: {@code perf.silverfish_wake_scan} (gated by mixin plugin / FeatureUnits).
 *
 * @see SecondaryPoiRadiusMixin sibling dual-axis box shrink {@code @ModifyConstant} pattern
 * @see NearestBedScanMixin sibling scan-radius shrink pattern
 * @see BeeHiveLocateMixin sibling nested-goal {@code @ModifyConstant} pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishWakeUpFriendsGoal")
public abstract class SilverfishWakeScanMixin {

    /**
     * Rewrite the sole {@code 5} in {@code tick} (Y spiral upper bound) to {@code 4}.
     *
     * @param original vanilla constant (always 5 at the matched site)
     * @return reduced positive Y scan half-extent
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 5))
    private int omnifix$shrinkWakeScanYPos(int original) {
        return 4;
    }

    /**
     * Rewrite the sole {@code -5} in {@code tick} (Y spiral lower bound) to {@code -4}.
     *
     * @param original vanilla constant (always -5 at the matched site)
     * @return reduced negative Y scan half-extent
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = -5))
    private int omnifix$shrinkWakeScanYNeg(int original) {
        return -4;
    }

    /**
     * Rewrite every {@code 10} in {@code tick} (XZ j and k spiral upper bounds)
     * to {@code 8}.
     *
     * @param original vanilla constant (always 10 at the matched sites)
     * @return reduced positive XZ scan half-extent
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
    private int omnifix$shrinkWakeScanXZPos(int original) {
        return 8;
    }

    /**
     * Rewrite every {@code -10} in {@code tick} (XZ j and k spiral lower bounds)
     * to {@code -8}.
     *
     * @param original vanilla constant (always -10 at the matched sites)
     * @return reduced negative XZ scan half-extent
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = -10))
    private int omnifix$shrinkWakeScanXZNeg(int original) {
        return -8;
    }
}
