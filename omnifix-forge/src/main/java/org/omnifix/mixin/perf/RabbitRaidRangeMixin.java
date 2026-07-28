package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Rabbit$RaidGardenGoal} is a nested {@code MoveToBlockGoal} that hunts
 * mature carrot crops. Its constructor calls {@code super(pRabbit, 0.7D, 16)}, so every raid
 * attempt walks a horizontal search radius of {@code 16} blocks (vanilla
 * {@code MoveToBlockGoal} searchArea) looking for valid crop positions. Village gardens and
 * carrot farms re-run that block scan whenever the goal restarts after
 * {@code nextStartTick} / completion; dense rabbit populations therefore pay repeated
 * {@code searchForDestination}-class block walks over a large disc even when carrots only
 * exist near the rabbit.
 *
 * <p>Policy: one {@code @ModifyConstant} on {@code <init>} rewrites the sole {@code int}
 * literal {@code 16 → 12} (the search-range argument to {@code MoveToBlockGoal}'s super).
 * The speed double {@code 0.7D} is intentionally untouched (different constant type / value;
 * leaving it avoids accidental speed changes if a second double ever appears).
 *
 * <p>Constant audit (MC 1.20.1 mapped {@code Rabbit$RaidGardenGoal}):
 * <ul>
 *   <li>{@code <init>(Rabbit)}: sole {@code int 16} is the third super-arg
 *       {@code super(pRabbit, 0.7D, 16)} — {@code MoveToBlockGoal} search range</li>
 *   <li>{@code <init>}: also {@code 0.7D} (speed) — not matched by {@code intValue = 16}</li>
 *   <li>{@code canUse} / {@code canContinueToUse} / {@code isValidTarget} / {@code tick}:
 *       no additional search-range {@code int 16} literals (range is stored once on the
 *       parent goal from the constructor argument)</li>
 * </ul>
 * {@code ModifyConstant} on {@code intValue = 16} in {@code <init>} is therefore safe without
 * ordinal narrowing and does not rewrite the {@code 0.7D} speed argument.
 *
 * <p>Trade-off: rabbits search for carrot crops in a smaller radius (16→12 blocks;
 * planar area factor {@code (12/16)² = 0.5625} of vanilla disc). Crops 12–16 blocks away are
 * ignored until the rabbit moves closer; once a crop is found, move-to / eat behaviour is
 * unchanged. Complementary interval stretch lives on {@link MoveToBlockIntervalMixin}
 * (shared parent {@code nextStartTick}). Not panic/flee — {@code RabbitPanicGoal} and
 * hurt-flee goals are out of scope; no urgency exception required.
 *
 * <p>Unit: {@code perf.rabbit_raid_range} (gated by mixin plugin / FeatureUnits).
 *
 * @see MoveToBlockIntervalMixin complementary MoveToBlockGoal nextStartTick stretch
 * @see BeeHiveLocateMixin sibling nested animal goal {@code @ModifyConstant} pattern
 * @see GhastWanderRadiusMixin sibling nested-goal radius shrink pattern
 * @see NearestLivingSensorRadiusMixin sibling 16→12 range shrink pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Rabbit$RaidGardenGoal")
public abstract class RabbitRaidRangeMixin {

    /**
     * Rewrite the sole {@code 16} in {@code <init>}
     * ({@code super(pRabbit, 0.7D, 16)} MoveToBlockGoal search range) to {@code 12}.
     * Speed double {@code 0.7D} is not matched.
     *
     * @param original vanilla constant (always 16 at the matched site)
     * @return reduced carrot-crop block search range
     */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 16))
    private int omnifix$shrinkRaidGardenSearchRange(int original) {
        return 12;
    }
}
