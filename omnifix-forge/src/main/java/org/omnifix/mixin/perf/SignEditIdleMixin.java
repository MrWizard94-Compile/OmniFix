package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Root cause: every sign (including hanging signs via inheritance) is registered with a ticker that
 * only clears a far-away editor UUID. When {@code playerWhoMayEdit} is null the body is a no-op, yet
 * the method still runs for every sign in loaded chunks. Skip until an edit session sets the UUID.
 */
@Mixin(SignBlockEntity.class)
public abstract class SignEditIdleMixin {

    @Shadow
    public abstract UUID getPlayerWhoMayEdit();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipIdleSign(
            Level level,
            BlockPos pos,
            BlockState state,
            SignBlockEntity sign,
            CallbackInfo ci
    ) {
        if (((SignEditIdleMixin) (Object) sign).getPlayerWhoMayEdit() == null) {
            ci.cancel();
        }
    }
}
