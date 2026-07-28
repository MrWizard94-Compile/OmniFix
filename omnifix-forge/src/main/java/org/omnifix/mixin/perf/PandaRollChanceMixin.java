package org.omnifix.mixin.perf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@code Panda$PandaRollGoal#canUse} rolls
 * {@code nextInt(reducedTickDelay(isPlayful() ? 60 : 500))} before any roll animation /
 * movement work. Grounded pandas re-evaluate this on every goal-selector pass; bamboo
 * biomes with dense panda packs therefore probe roll starts on a ~1/60 (playful) or
 * ~1/500 (normal) cadence whenever other goals leave a free slot, burning AI time without
 * improving panic, flee-from-hurt, breed, or attack behavior.
 *
 * <p>Policy: two {@code @ModifyConstant} injectors on {@code canUse} only:
 * <ul>
 *   <li>playful gate base {@code 60} → {@code 90} (+50%)</li>
 *   <li>normal gate base {@code 500} → {@code 750} (+50%)</li>
 * </ul>
 * Audit (MC 1.20.1 mapped {@code Panda$PandaRollGoal}):
 * <ul>
 *   <li>{@code canUse}: sole {@code int 60} — playful {@code reducedTickDelay(60)} nextInt bound</li>
 *   <li>{@code canUse}: sole {@code int 500} — normal {@code reducedTickDelay(500)} nextInt bound</li>
 *   <li>{@code canContinueToUse}/{@code start}/{@code tick}/{@code stop}: other ints live in
 *       different methods — out of scope</li>
 *   <li>Sneeze, sit, attack, breed, and panic goals live on separate nested classes — not touched</li>
 * </ul>
 * The two int literals are unique within {@code canUse}, so {@code ModifyConstant} on
 * {@code intValue = 60} and {@code intValue = 500} is safe without ordinal narrowing.
 *
 * <p>Trade-off: pandas roll less often (fewer playful and normal roll starts). Once a roll
 * does start, animation duration, continuation, and movement are unchanged. Panic /
 * flee-from-hurt and combat goals are out of scope — no fire/hurt urgency exception is
 * required for this idle random gate.
 *
 * <p>Unit: {@code perf.panda_roll_chance} (gated by mixin plugin / FeatureUnits).
 *
 * @see TurtleGoHomeChanceMixin sibling nested animal nextInt({@code reducedTickDelay}) chance
 * @see BeeWanderChanceMixin sibling nested idle canUse nextInt chance pattern
 * @see VexRandomMoveChanceMixin sibling nested nextInt({@code reducedTickDelay}) chance
 * @see SlimeJumpDelayMixin sibling dual {@code @ModifyConstant} on one method
 * @see FollowOwnerRepathMixin sibling {@code @ModifyConstant} interval pattern
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Panda$PandaRollGoal")
public abstract class PandaRollChanceMixin {

    /**
     * Stretches the playful roll random-gate base (vanilla {@code reducedTickDelay(60)} →
     * {@code reducedTickDelay(90)}).
     *
     * @param original vanilla constant (always 60 at the matched site)
     * @return stretched nextInt bound base for playful PandaRollGoal canUse chance
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 60))
    private int omnifix$rarerPlayfulPandaRoll(int original) {
        return 90;
    }

    /**
     * Stretches the normal roll random-gate base (vanilla {@code reducedTickDelay(500)} →
     * {@code reducedTickDelay(750)}).
     *
     * @param original vanilla constant (always 500 at the matched site)
     * @return stretched nextInt bound base for normal PandaRollGoal canUse chance
     */
    @ModifyConstant(method = "canUse", constant = @Constant(intValue = 500))
    private int omnifix$rarerNormalPandaRoll(int original) {
        return 750;
    }
}
