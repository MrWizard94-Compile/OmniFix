package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Unit: {@code perf.use_item_throttle}.
 *
 * <p>Root cause: {@link UseItemGoal#canUse} re-evaluates item-use preconditions (held item match,
 * finish predicate, remaining use ticks) on every goal-selector pass for mobs that drink/eat via
 * this goal. Throttling to every third mob tick cuts redundant canUse work.
 *
 * <p>Shadow note: vanilla field is {@code private final T mob} with {@code T extends Mob}. Mixin
 * shadows it as {@link Mob} (erasure-compatible).
 *
 * <p>Trade-off: item-use AI goals (e.g. some mobs drinking) may begin up to ~2 ticks later. Not
 * urgency-critical (no panic-on-fire / flee-from-hurt path).
 */
@Mixin(UseItemGoal.class)
public abstract class UseItemThrottleMixin {

    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleUseItem(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
