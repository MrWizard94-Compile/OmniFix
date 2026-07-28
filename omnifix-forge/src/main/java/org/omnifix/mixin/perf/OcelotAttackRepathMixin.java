package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Root cause: {@link OcelotAttackGoal#tick} calls {@code getNavigation().moveTo(target, speed)}
 * every tick while the goal is active. {@code moveTo(Entity, double)} creates or refreshes a path
 * toward the living target each call — expensive under dense cat/ocelot combat or many wild
 * ocelots pathing at chickens.
 *
 * <p>Throttle: wrap only the {@link PathNavigation#moveTo(Entity, double)} invoke so repath runs
 * when {@code (mob.tickCount & 1) == 0} (every other entity tick). Look control, {@code attackTime}
 * countdown, and {@code doHurtTarget} still execute every tick — combat timing is unchanged.
 *
 * <p>Trade-off: chase path updates at ~10&nbsp;Hz instead of 20&nbsp;Hz; cats/ocelots may curve
 * slightly less tightly around moving targets between repaths.
 *
 * <p>Unit: {@code perf.ocelot_attack_repath}
 */
@Mixin(OcelotAttackGoal.class)
public abstract class OcelotAttackRepathMixin {

    @Shadow
    @Final
    private Mob mob;

    /**
     * Allows {@code moveTo} only on even mob ticks so pathfinding runs every other tick.
     * Skipped calls leave the previous path in place; MixinExtras supplies {@code false} as the
     * unused boolean result of a cancelled {@code moveTo}.
     *
     * @param navigation navigation instance that would receive {@code moveTo}
     * @param target     attack target entity (LivingEntity at call site)
     * @param speed      movement speed multiplier from the goal's distance bands
     * @return {@code true} to run {@code moveTo}; {@code false} to skip this tick's repath
     */
    @WrapWithCondition(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;D)Z"
            )
    )
    private boolean omnifix$repathEveryOtherTick(PathNavigation navigation, Entity target, double speed) {
        return (this.mob.tickCount & 1) == 0;
    }
}
