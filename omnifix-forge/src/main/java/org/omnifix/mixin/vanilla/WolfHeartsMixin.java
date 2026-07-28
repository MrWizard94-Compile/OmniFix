package org.omnifix.mixin.vanilla;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-93018 — Feeding meat to a wild wolf shows love hearts even though wild wolves cannot breed.
 *
 * <p>Root cause: {@link Wolf#mobInteract} falls through to {@link Animal#mobInteract} for non-bone
 * food. That path calls {@link Animal#setInLove} whenever {@link Animal#canFallInLove()} is true,
 * without checking {@link Wolf#isTame()}. Wild adults therefore broadcast entity event 18 (hearts)
 * and consume the food despite {@link Wolf#canMate} requiring both partners to be tamed.
 *
 * <p>Gating love on tame status prevents both the hearts and the wasted food for wild wolves while
 * leaving breeding of tamed pairs unchanged.
 */
@Mixin(Animal.class)
public abstract class WolfHeartsMixin {

    @Inject(method = "canFallInLove", at = @At("HEAD"), cancellable = true)
    private void omnifix$wildWolvesCannotFallInLove(CallbackInfoReturnable<Boolean> cir) {
        Animal self = (Animal) (Object) this;
        if (self instanceof Wolf wolf && !wolf.isTame()) {
            cir.setReturnValue(false);
        }
    }
}
