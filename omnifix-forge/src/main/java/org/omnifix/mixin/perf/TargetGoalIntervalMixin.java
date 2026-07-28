package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: {@link NearestAttackableTargetGoal#canUse} rolls {@code randomInterval} every tick
 * before {@code findTarget} (entity scan). Default interval is 10 (after reducedTickDelay). Raising
 * it cuts expensive nearest-target searches on dense mob packs.
 *
 * <p>Trade-off: mobs may acquire targets slightly slower.
 */
@Mixin(NearestAttackableTargetGoal.class)
public abstract class TargetGoalIntervalMixin {

    private static final int OMNIFIX$MAX_INTERVAL = 40;

    /**
     * Full constructor receives the raw interval before {@code reducedTickDelay}. Bump by +50%.
     */
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/Mob;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static int omnifix$bumpTargetInterval(int randomInterval) {
        if (randomInterval <= 0) {
            return randomInterval;
        }
        long bumped = randomInterval + (randomInterval / 2L);
        return (int) Math.min(OMNIFIX$MAX_INTERVAL, bumped);
    }
}
