package org.omnifix.mixin.perf;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link ItemEntity#tick} gates {@code mergeWithNeighbours()} (AABB
 * {@code getEntitiesOfClass} scan) on {@code tickCount % i == 0} where
 * {@code int i = flag ? 2 : 40} — block-crossing items scan every 2 ticks, stationary
 * piles every 40 (~2 s). Dense item dumps and farms re-scan every 2 s per resting entity
 * even when nothing can merge.
 *
 * <p>Stretching the stationary period from 40 → 60 cuts resting merge-scan frequency by
 * 33% (~2 s → ~3 s at 20 TPS). The moving-case literal {@code 2} is left untouched so
 * items still try to coalesce promptly while sliding/falling into piles.
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code tick}): the only {@code int 40} in the method
 * body is the stationary merge-period arm of the ternary. Age lifespan uses the
 * {@code lifespan} field (default 6000 via constructor / item tag), not a {@code 40}
 * literal. Other ints in {@code tick} are sentinels ({@code 32767}, {@code -32768}), the
 * move-interval {@code 4}, and the moving merge period {@code 2}. {@code ModifyConstant}
 * on {@code intValue = 40} is therefore safe without ordinal narrowing and does not
 * alter the moving-case {@code 2}.
 *
 * <p>Trade-off: stationary items merge-scan every 60 ticks (~3 s) instead of 40 (~2 s).
 * Moving items keep the vanilla 2-tick cadence. Stack consolidation on settled farms is
 * up to ~1 s slower; entity count and lifetime are otherwise unchanged.
 *
 * <p>Unit: {@code perf.item_merge_period} (gated by mixin plugin / FeatureUnits).
 *
 * @see XpOrbScanPeriodMixin sibling period-stretch pattern
 * @see ItemEntityMergeCacheMixin same-tick empty merge-scan cache (complementary)
 */
@Mixin(ItemEntity.class)
public abstract class ItemMergePeriodMixin {

    /**
     * Rewrite only the stationary merge period ({@code 40} → {@code 60}).
     * Moving-case {@code 2} is a different constant and is not matched.
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 40))
    private int omnifix$longerStationaryMergePeriod(int original) {
        return 60;
    }
}
