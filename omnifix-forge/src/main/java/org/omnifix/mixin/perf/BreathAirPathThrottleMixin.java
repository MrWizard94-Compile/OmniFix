package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.BreathAirGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Unit: {@code perf.breath_air_path_throttle}
 *
 * <p>Root cause: while {@link BreathAirGoal} is active (air &lt; 140), {@code tick()} invokes private
 * {@code findAirPosition()} every tick. That method scans a vertical column via
 * {@code BlockPos.betweenClosed}, tests pathfindable air / bubble-column at each pos, then issues
 * {@code moveTo}. Dolphins and other breathers pay that block-scan cost every tick even when air is
 * still comfortable.
 *
 * <p>Approach: MixinExtras {@link WrapOperation} on the {@code findAirPosition} invoke inside
 * {@code tick} only. When air supply is non-critical ({@code >= 60}), skip the original on odd mob
 * ticks. When air is critical ({@code < 60}), always call original. {@code moveRelative} /
 * {@code move} after the wrap remain on the vanilla path every tick — cancelling whole {@code tick}
 * would stall swimming toward surface.
 *
 * <p>Hard law: never throttle when air is critical ({@code getAirSupply() < 60}). Goal {@code start()}
 * still calls {@code findAirPosition} unthrottled so activation always gets a fresh target.
 *
 * <p>Trade-off: non-critical air re-seek every 2nd tick (~50 ms extra latency at 20 TPS). Critical
 * drowning always seeks every tick. Halves steady-state betweenClosed / pathfindable cost for active
 * breathers that are not yet near drowning.
 */
@Mixin(BreathAirGoal.class)
public abstract class BreathAirPathThrottleMixin {

    /** Air supply below this is treated as critical — never skip the air-position search. */
    private static final int CRITICAL_AIR = 60;

    @Shadow
    @Final
    private PathfinderMob mob;

    /**
     * Gate {@code findAirPosition} inside {@code tick}. Skip original when air is non-critical and
     * the mob tick is odd; always run when air &lt; {@link #CRITICAL_AIR}.
     *
     * @param self     receiver of the private {@code findAirPosition} invoke
     * @param original vanilla {@code findAirPosition} body
     */
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/BreathAirGoal;findAirPosition()V"
            )
    )
    private void omnifix$throttleFindAir(BreathAirGoal self, Operation<Void> original) {
        // Hard law: air critical → always re-seek. Even ticks when non-critical → re-seek.
        if (this.mob.getAirSupply() < CRITICAL_AIR || (this.mob.tickCount & 1) == 0) {
            original.call(self);
        }
    }
}
