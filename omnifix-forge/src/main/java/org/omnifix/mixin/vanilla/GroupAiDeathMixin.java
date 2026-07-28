package org.omnifix.mixin.vanilla;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * MC-183990 — Group / pack AI keeps a dead entity as its attack target.
 *
 * <p>Root cause: {@link Mob#target} is only cleared when target selectors run and decide to drop
 * it. Some goals keep working against a dead reference, so pack members stall. Clear the target
 * at the end of {@link Mob#baseTick} when it is already dead.
 */
@Mixin(Mob.class)
public abstract class GroupAiDeathMixin {

    @Shadow
    @Nullable
    private LivingEntity target;

    @Shadow
    public abstract void setTarget(@Nullable LivingEntity target);

    @Inject(
            method = "baseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void omnifix$clearDeadTarget(CallbackInfo ci) {
        if (this.target != null && !this.target.isAlive()) {
            this.setTarget(null);
        }
    }
}
