package org.omnifix.mixin.perf;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link ClimbOnTopOfPowderSnowGoal#canUse} re-checks powder-snow immersion tags and
 * the block state / collision shape above the mob on every goal-selector evaluation for rabbits,
 * foxes, and other powder-snow-walkable mobs. Throttling to every third mob tick cuts repeated
 * block lookups in cold biomes without removing climb-out-of-powder-snow behavior.
 *
 * <p>Trade-off: climb-out may start up to ~2 ticks later. Not a panic/flee-from-hurt goal — freeze
 * damage still accumulates on the entity tick path independently of this goal.
 */
@Mixin(ClimbOnTopOfPowderSnowGoal.class)
public abstract class ClimbPowderSnowThrottleMixin {

    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleClimbPowderSnow(CallbackInfoReturnable<Boolean> cir) {
        if ((this.mob.tickCount % 3) != 0) {
            cir.setReturnValue(false);
        }
    }
}
