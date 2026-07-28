package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code EnderMan$EndermanTakeBlockGoal#canUse} rolls
 * {@code nextInt(reducedTickDelay(20)) == 0} before any block-probe / griefing work. Idle
 * endermen that are not carrying a block re-evaluate this on every goal-selector pass; dense
 * enderman packs therefore burn AI time on random take attempts that usually fail the roll.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code canUse} only rewrites the sole {@code int}
 * literal {@code 20} → {@code 30} (+50%). Audit (MC 1.20.1 {@code EnderMan$EndermanTakeBlockGoal}):
 * {@code canUse} contains that single {@code 20} as the {@code reducedTickDelay} argument —
 * early exits ({@code getCarriedBlock() != null}, mob-griefing gamerule) have no other int
 * constants. Place-block goal, teleport / stare combat, and hurt revenge are out of scope.
 *
 * <p>Trade-off: endermen attempt block pickup less often (fewer random block probes). When a take
 * does start, place/search behavior is unchanged. Combat urgency is not touched.
 *
 * <p>Unit: {@code perf.enderman_take_interval}
 */
@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public abstract class EndermanTakeIntervalMixin {

    /**
     * Stretches the take-block attempt interval base (vanilla 20 → 30 ticks before
     * {@code reducedTickDelay}).
     *
     * @param original vanilla constant (always 20 at the matched site)
     * @return stretched random-roll interval base for take attempts
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 20))
    private int omnifix$longerTakeAttemptInterval(int original) {
        return 30;
    }
}
