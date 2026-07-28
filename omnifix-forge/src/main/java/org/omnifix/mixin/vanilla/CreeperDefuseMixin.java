package org.omnifix.mixin.vanilla;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-179072 — Creepers keep swelling (and can explode) after the target switches to creative or
 * spectator.
 *
 * <p>Root cause: {@link SwellGoal#canUse} treats {@code swellDir > 0} as sufficient to keep the goal
 * active, and {@link SwellGoal#tick} only checks distance / line-of-sight — never invulnerability
 * modes. Defuse when the tracked target is a creative/spectator player.
 */
@Mixin(SwellGoal.class)
public abstract class CreeperDefuseMixin {

    @Shadow @Final private Creeper creeper;
    @Shadow private LivingEntity target;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true, require = 0)
    private void omnifix$noSwellOnInvulnerableTarget(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity current = this.creeper.getTarget();
        if (isCreativeOrSpectator(current)) {
            // Ensure any partial swell decays when the goal is rejected.
            if (this.creeper.getSwellDir() > 0) {
                this.creeper.setSwellDir(-1);
            }
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private void omnifix$defuseIfTargetInvulnerable(CallbackInfo ci) {
        if (!isCreativeOrSpectator(this.target)) {
            return;
        }
        this.creeper.setSwellDir(-1);
        this.target = null;
        ci.cancel();
    }

    private static boolean isCreativeOrSpectator(LivingEntity entity) {
        return entity instanceof Player player && (player.isCreative() || player.isSpectator());
    }
}
