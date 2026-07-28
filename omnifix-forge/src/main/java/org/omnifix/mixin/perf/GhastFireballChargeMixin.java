package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Ghast$GhastShootFireballGoal#tick} fires a {@code LargeFireball} when
 * {@code chargeTime == 20} while the ghast has line-of-sight on its target. Each fireball is a
 * full entity spawn with explosion-power tracking; packs of ghasts in the Nether therefore
 * produce large-fireball entities on a ~1s charge cadence (plus the post-fire {@code -40}
 * cooldown climb). Stretching the fire threshold cuts fireball spawn rate without touching
 * LOS / look / wander goals.
 *
 * <p>Policy: {@code @ModifyConstant} on {@code tick} rewrites the sole {@code int} literal
 * {@code 20} → {@code 30} (+50% charge-to-fire). Audit (MC 1.20.1 {@code Ghast$GhastShootFireballGoal#tick}):
 * <ul>
 *   <li>{@code chargeTime == 10} warn sound — different int, untouched</li>
 *   <li>{@code chargeTime == 20} fire threshold — <strong>only</strong> int {@code 20} in method</li>
 *   <li>{@code chargeTime = -40} post-fire reset — different int, untouched</li>
 *   <li>{@code 4.0D} spawn offset / view-vector scale — doubles, out of scope</li>
 *   <li>{@code setCharging(chargeTime > 10)} mouth open — int {@code 10}, untouched</li>
 * </ul>
 * No ordinal needed: a single {@code intValue = 20} site exists in {@code tick}.
 *
 * <p>Trade-off: ghasts take longer to fire large fireballs (+50% charge, 20→30 ticks after the
 * warn sound window). Combat still works; warn sound at 10 and post-fire recovery at -40 are
 * unchanged. Panic / flee-from-hurt / fire-urgency goals are unrelated and untouched.
 *
 * <p>Unit: {@code perf.ghast_fireball_charge}
 *
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 * @see EndermanTakeIntervalMixin sibling nested-goal {@code targets = "...$Inner"} pattern
 * @see RangedCrossbowDelayMixin sibling fire-cadence {@code 20→30} pattern
 */
@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public abstract class GhastFireballChargeMixin {

    /**
     * Stretches the charge-to-fire threshold (vanilla {@code chargeTime == 20} → {@code 30}).
     *
     * @param original vanilla constant (always 20 at the matched site)
     * @return stretched fire threshold in ticks
     */
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
    private int omnifix$longerFireballCharge(int original) {
        return 30;
    }
}
