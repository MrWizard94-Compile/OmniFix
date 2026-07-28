package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: {@link LookAtPlayerGoal#canUse} rolls {@code probability} (default 0.02) then may run
 * nearest-entity queries. Reducing probability by ~1/3 cuts idle look scans across almost all mobs.
 * The 3-arg constructor chains through the full constructor with {@code DEFAULT_PROBABILITY}, so a
 * single {@code ModifyVariable} on the full ctor covers all construction paths.
 *
 * <p>Trade-off: mobs look at players slightly less often.
 */
@Mixin(LookAtPlayerGoal.class)
public abstract class LookAtPlayerProbabilityMixin {

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;FFZ)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1
    )
    private static float omnifix$reduceProbability(float probability) {
        if (probability <= 0.0F || probability > 1.0F) {
            return probability;
        }
        return probability * (2.0F / 3.0F);
    }
}
