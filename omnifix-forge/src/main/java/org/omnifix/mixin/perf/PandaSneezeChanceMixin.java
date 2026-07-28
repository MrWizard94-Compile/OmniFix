package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Panda$PandaSneezeGoal#canUse} rolls
 * {@code nextInt(reducedTickDelay(isWeak() ? 500 : 6000))} (weak babies first gate on
 * {@code 500}, all baby paths also / otherwise on {@code 6000}) before sneeze animation
 * work. Grounded baby pandas re-evaluate this on every goal-selector pass; bamboo biomes
 * with dense cub packs therefore probe sneeze starts on a ~1/500 (weak) or ~1/6000
 * (normal) cadence whenever other goals leave a free slot, burning AI time without
 * improving panic, flee-from-hurt, breed, or attack behavior.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on {@code canUse} only:
 * <ul>
 *   <li>weak gate base {@code 500} → {@code 750} (+50%)</li>
 *   <li>normal gate base {@code 6000} → {@code 8000} (~+33%)</li>
 * </ul>
 * Audit (MC 1.20.1 mapped {@code Panda$PandaSneezeGoal}):
 * <ul>
 *   <li>{@code canUse}: sole {@code int 500} — weak {@code reducedTickDelay(500)} nextInt bound</li>
 *   <li>{@code canUse}: sole {@code int 6000} — normal {@code reducedTickDelay(6000)} nextInt bound</li>
 *   <li>{@code canContinueToUse}/{@code start}/{@code tick}/{@code stop}: other ints live in
 *       different methods — out of scope</li>
 *   <li>Roll, sit, attack, breed, and panic goals live on separate nested classes — not touched</li>
 * </ul>
 * The two int literals are unique within {@code canUse}, so {@code ModifyConstant} on
 * {@code intValue = 500} and {@code intValue = 6000} is safe without ordinal narrowing.
 *
 * <p>Trade-off: baby pandas sneeze less often (fewer weak and normal sneeze starts). Once a
 * sneeze does start, animation duration, sneeze particle/sound, and continuation are
 * unchanged. Panic / flee-from-hurt and combat goals are out of scope — no fire/hurt
 * urgency exception is required for this idle random gate. Only babies can sneeze in
 * vanilla; adults are unaffected by this goal.
 *
 * <p>Unit: {@code perf.panda_sneeze_chance} (gated by mixin plugin / FeatureUnits).
 *
 * @see PandaRollChanceMixin sibling nested Panda dual reducedTickDelay chance gates
 * @see TurtleGoHomeChanceMixin sibling nested animal nextInt({@code reducedTickDelay}) chance
 * @see BeeWanderChanceMixin sibling nested idle canUse nextInt chance pattern
 * @see VexRandomMoveChanceMixin sibling nested nextInt({@code reducedTickDelay}) chance
 * @see SlimeJumpDelayMixin sibling dual {@code @ModifyConstant} on one method
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Panda$PandaSneezeGoal")
public abstract class PandaSneezeChanceMixin {

    /**
     * Stretches the weak-gene sneeze random-gate base (vanilla {@code reducedTickDelay(500)} →
     * {@code reducedTickDelay(750)}).
     *
     * @param original vanilla constant (always 500 at the matched site)
     * @return stretched nextInt bound base for weak PandaSneezeGoal canUse chance
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 500))
    private int omnifix$rarerWeakPandaSneeze(int original) {
        return 750;
    }

    /**
     * Stretches the normal sneeze random-gate base (vanilla {@code reducedTickDelay(6000)} →
     * {@code reducedTickDelay(8000)}).
     *
     * @param original vanilla constant (always 6000 at the matched site)
     * @return stretched nextInt bound base for normal PandaSneezeGoal canUse chance
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 6000))
    private int omnifix$rarerNormalPandaSneeze(int original) {
        return 8000;
    }
}
