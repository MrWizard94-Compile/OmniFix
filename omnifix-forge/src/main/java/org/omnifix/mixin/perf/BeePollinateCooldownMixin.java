package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: when {@code Bee$BeePollinateGoal#canBeeUse} fails to find a flower, vanilla sets
 * {@code remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(random, 20, 60)}. Dense bee swarms
 * then re-enter flower block scans on a short cadence; failed searches dominate AI time without
 * changing pollination quality when a flower is actually present.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code canBeeUse} only rewrites the two {@code nextInt}
 * bounds 20→30 and 60→90 (+50%). Audit (1.20.1 / 1.21.x Bee): those ints appear solely in
 * {@code Mth.nextInt(..., 20, 60)} inside {@code canBeeUse}. The {@code tickCount % 20} flower
 * re-validate lives in {@code canBeeContinueToUse} (different method — untouched). Successful
 * pollinate stop cooldown ({@code 200}) and field initializer on {@code Bee} are out of scope.
 *
 * <p>Trade-off: failed flower searches wait longer before retry (fewer block scans for bee swarms).
 * Successful finds, nectar carry, hive return, and anger / sting combat goals are unchanged.
 *
 * <p>Unit: {@code perf.bee_pollinate_cooldown}
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeePollinateGoal")
public abstract class BeePollinateCooldownMixin {

    /**
     * Stretches the {@code nextInt} lower bound (vanilla 20 → 30 ticks).
     *
     * @param original vanilla constant (always 20 at the matched site)
     * @return stretched min cooldown before locating a new flower
     */
    @ModifyConstant(method = "canBeeUse", constant = @Constant(intValue = 20))
    private int omnifix$longerFlowerSearchCooldownMin(int original) {
        return 30;
    }

    /**
     * Stretches the {@code nextInt} upper bound (vanilla 60 → 90 ticks).
     *
     * @param original vanilla constant (always 60 at the matched site)
     * @return stretched max cooldown before locating a new flower
     */
    @ModifyConstant(method = "canBeeUse", constant = @Constant(intValue = 60))
    private int omnifix$longerFlowerSearchCooldownMax(int original) {
        return 90;
    }
}
