package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code EnderMan$EndermanLeaveBlockGoal#canUse} rolls
 * {@code nextInt(reducedTickDelay(2000)) == 0} before any place-block / griefing work. Carrying
 * endermen re-evaluate this on every goal-selector pass; dense packs therefore burn AI time on
 * random place attempts that usually fail the roll.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code canUse} only rewrites the sole {@code int}
 * literal {@code 2000} → {@code 3000} (+50%). Audit (MC 1.20.1
 * {@code EnderMan$EndermanLeaveBlockGoal}): {@code canUse} contains that single {@code 2000} as
 * the {@code reducedTickDelay} argument — early exits ({@code getCarriedBlock() == null},
 * mob-griefing gamerule) have no other int constants. Take-block goal, teleport / stare combat,
 * and hurt revenge are out of scope.
 *
 * <p>Trade-off: endermen place carried blocks less often (fewer random place probes). When a leave
 * does start, place/search behavior is unchanged. Combat urgency is not touched.
 *
 * <p>Unit: {@code perf.enderman_leave_interval}
 */
@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanLeaveBlockGoal")
public abstract class EndermanLeaveIntervalMixin {

    /**
     * Stretches the leave-block attempt interval base (vanilla 2000 → 3000 ticks before
     * {@code reducedTickDelay}).
     *
     * @param original vanilla constant (always 2000 at the matched site)
     * @return stretched random-roll interval base for place attempts
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 2000))
    private int omnifix$longerLeaveAttemptInterval(int original) {
        return 3000;
    }
}
