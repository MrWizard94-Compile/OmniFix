package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.animal.Pufferfish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-155509 — A puffed pufferfish that is already dead/dying can still sting and poison the player
 * on contact for the remainder of the touch tick.
 *
 * <p>Root cause: {@link Pufferfish#playerTouch} applies poison when {@code Player#hurt} returns
 * true, without re-checking that the pufferfish is still alive after (or during) the hurt call
 * path. Gate the sting side-effects on {@link net.minecraft.world.entity.Entity#isAlive()}.
 */
@Mixin(Pufferfish.class)
public abstract class PufferfishDyingMixin {

    @ModifyExpressionValue(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private boolean omnifix$onlyStingWhileAlive(boolean damaged) {
        return damaged && ((Pufferfish) (Object) this).isAlive();
    }
}
