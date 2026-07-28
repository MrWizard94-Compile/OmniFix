package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link MoveTowardsRestrictionGoal#canUse} calls
 * {@link DefaultRandomPos#getPosTowards} (expensive random-pos + path-space probe) on every
 * goal-selector evaluation when a leashed/restricted mob is outside its home. Throttling to every
 * third mob tick preserves return-to-restriction behavior with lower steady-state pathfinder cost
 * in dense farms/villages.
 *
 * <p>Trade-off: restricted mobs may begin walking home up to ~2 ticks later. Not an urgent
 * panic/flee path — no fire/hurt bypass required.
 */
@Mixin(MoveTowardsRestrictionGoal.class)
public abstract class MoveRestrictionThrottleMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleRestrictionPos(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
