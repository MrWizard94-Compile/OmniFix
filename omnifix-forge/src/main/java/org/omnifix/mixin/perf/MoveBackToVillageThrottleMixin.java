package org.omnifix.mixin.perf;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveBackToVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Unit: {@code perf.move_back_village_throttle}.
 *
 * <p>Root cause: {@link MoveBackToVillageGoal#canUse} runs a server-level village section check
 * ({@code ServerLevel#isVillage}) and, when outside a village, falls through to
 * {@link RandomStrollGoal#canUse} which samples {@code getPosition()} (closest-village section +
 * {@code DefaultRandomPos#getPosTowards}). That pathfinder probe runs on every goal-selector
 * evaluation for mobs using this goal. Throttling to every third mob tick preserves return-to-village
 * behavior with lower steady-state POI / pathfinder cost in dense packs.
 *
 * <p>{@code mob} is shadowed from the parent {@link RandomStrollGoal}
 * ({@code protected final PathfinderMob mob}).
 *
 * <p>Trade-off: return-to-village stroll starts up to ~2 ticks later. Not an urgent panic/flee or
 * fire-urgency path — no {@code isOnFire()} bypass required. Active navigation
 * ({@code tick}/{@code canContinueToUse}) is unchanged once the goal is running.
 */
@Mixin(MoveBackToVillageGoal.class)
public abstract class MoveBackToVillageThrottleMixin {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleMoveBackToVillage(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
