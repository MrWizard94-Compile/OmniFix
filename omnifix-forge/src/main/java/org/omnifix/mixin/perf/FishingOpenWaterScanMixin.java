package org.omnifix.mixin.perf;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while a bobber is in the {@code BOBBING} state with active nibble/hook timers,
 * every server tick re-evaluates open-water fishing via
 * {@link FishingHook#calculateOpenWater(net.minecraft.core.BlockPos)}. That method walks four
 * Y layers ({@code i = -1..2}) and, per layer, streams every block in the XZ box from
 * {@code pPos.offset(-2, i, -2)} to {@code pPos.offset(2, i, 2)} through
 * {@code getOpenWaterTypeForArea} → {@code BlockPos.betweenClosedStream} → per-block fluid /
 * collision probes. Vanilla volume is {@code 5×5×4 = 100} block-state lookups every tick the
 * open-water flag is still being verified (busy docks, AFK farms, multiplayer lakes with many
 * rods). Most of that cost is pure water confirmation once the hook has settled.
 *
 * <p>Policy: three {@code @ModifyConstant} injectors on {@code calculateOpenWater} only:
 * <ul>
 *   <li>{@code int 2} ordinal {@code 1} and {@code 2} → {@code 1} — the two positive arms of
 *       {@code pPos.offset(2, i, 2)}</li>
 *   <li>{@code int -2} (both sites) → {@code -1} — the two negative arms of
 *       {@code pPos.offset(-2, i, -2)}</li>
 * </ul>
 * Horizontal footprint becomes the centered 3×3 box {@code [-1..1]²} per layer instead of
 * 5×5. Y loop stays vanilla {@code for (int i = -1; i <= 2; ++i)}.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code calculateOpenWater}, confirmed via {@code javap}):
 * <ul>
 *   <li>{@code iconst_m1} then {@code istore} — Y loop init {@code i = -1}; not matched</li>
 *   <li>{@code iconst_2} ordinal {@code 0} — Y loop upper bound {@code i > 2} / {@code i <= 2};
 *       intentionally <strong>not</strong> rewritten (ordinal-narrowed injectors start at 1)</li>
 *   <li>{@code bipush -2} ×2 — sole negative horizontal offsets; both rewritten to {@code -1}</li>
 *   <li>{@code iconst_2} ordinals {@code 1}/{@code 2} — sole positive horizontal offsets;
 *       both rewritten to {@code 1}</li>
 *   <li>{@code iconst_0}/{@code iconst_1} — switch fall-through / {@code return true}; not matched
 *       by {@code intValue = 2} or {@code -2}</li>
 *   <li>{@code iinc 1} — loop step; not a matched constant load</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 2} <em>without</em> ordinal would also rewrite the
 * Y upper bound and collapse the layer span to {@code -1..1}. Ordinals {@code 1} and {@code 2}
 * isolate the {@code offset(2, i, 2)} arms. {@code intValue = -2} only appears on the horizontal
 * negative arms, so no ordinal narrowing is required there.
 *
 * <p>Trade-off: open-water fishing validation uses a smaller horizontal footprint (3×3 per
 * layer instead of 5×5; {@code 9×4 = 36} probes vs {@code 100}, ~64% fewer block probes per
 * recheck). Rods near docks, lily edges, or partial solid rings that only fail the outer 5×5
 * shell may now report open-water (and thus open-water loot tables / treasure eligibility)
 * when vanilla would have cleared the flag. Y column shape (water below + air/lily above) and
 * the every-tick recheck cadence while nibble/hook timers are active are unchanged. Does not
 * alter lure/luck timers, bite mechanics, or retrieve.
 *
 * <p>Unit: {@code perf.fishing_open_water_scan} (gated by mixin plugin / FeatureUnits).
 *
 * @see SecondaryPoiRadiusMixin sibling dual-axis box shrink {@code @ModifyConstant} pattern
 * @see SilverfishWakeScanMixin sibling multi-ordinal box-bound shrink pattern
 * @see FlyingHoverRadiusMixin sibling radius-shrink {@code @ModifyConstant} pattern
 */
@Mixin(FishingHook.class)
public abstract class FishingOpenWaterScanMixin {

    /**
     * Rewrite {@code iconst_2} ordinal 1 in {@code calculateOpenWater}
     * ({@code pPos.offset(2, i, 2)} positive X) to {@code 1}.
     * Ordinal 0 (Y loop upper bound) is intentionally skipped.
     *
     * @param original vanilla constant (always 2 at the matched site)
     * @return reduced positive X half-extent for the open-water area box
     */
    @ModifyConstant(method = "calculateOpenWater", constant = @Constant(intValue = 2, ordinal = 1))
    private int omnifix$shrinkOpenWaterPosX(int original) {
        return 1;
    }

    /**
     * Rewrite {@code iconst_2} ordinal 2 in {@code calculateOpenWater}
     * ({@code pPos.offset(2, i, 2)} positive Z) to {@code 1}.
     * Ordinal 0 (Y loop upper bound) is intentionally skipped.
     *
     * @param original vanilla constant (always 2 at the matched site)
     * @return reduced positive Z half-extent for the open-water area box
     */
    @ModifyConstant(method = "calculateOpenWater", constant = @Constant(intValue = 2, ordinal = 2))
    private int omnifix$shrinkOpenWaterPosZ(int original) {
        return 1;
    }

    /**
     * Rewrite every {@code -2} in {@code calculateOpenWater}
     * ({@code pPos.offset(-2, i, -2)} X and Z) to {@code -1}. Both sites are horizontal;
     * Y loop uses {@code -1}, not {@code -2}.
     *
     * @param original vanilla constant (always -2 at the matched sites)
     * @return reduced negative XZ half-extent for the open-water area box
     */
    @ModifyConstant(method = "calculateOpenWater", constant = @Constant(intValue = -2))
    private int omnifix$shrinkOpenWaterNegXZ(int original) {
        return -1;
    }
}
