package org.omnifix.mixin.vanilla;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-129909 (finish path) — cancel {@link LivingEntity#completeUsingItem()} for spectators so a
 * use that is already mid-finish cannot apply food/potion effects or shrink the stack.
 */
@Mixin(LivingEntity.class)
public abstract class SpectatorConsumeFinishMixin {

    @Inject(method = "completeUsingItem", at = @At("HEAD"), cancellable = true)
    private void omnifix$cancelCompleteUseInSpectator(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player && player.isSpectator()) {
            self.stopUsingItem();
            ci.cancel();
        }
    }
}
