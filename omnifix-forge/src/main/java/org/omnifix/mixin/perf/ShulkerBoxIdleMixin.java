package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: closed (and fully opened) shulker boxes still enter {@code tick} → {@code updateAnimation}
 * every game tick, which only re-assigns {@code progress} to 0 or 1. Skip until an open/close event
 * moves the animation into OPENING/CLOSING.
 */
@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxIdleMixin {

    @Shadow
    public abstract ShulkerBoxBlockEntity.AnimationStatus getAnimationStatus();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipIdleShulker(
            Level level,
            BlockPos pos,
            BlockState state,
            ShulkerBoxBlockEntity box,
            CallbackInfo ci
    ) {
        ShulkerBoxBlockEntity.AnimationStatus status =
                ((ShulkerBoxIdleMixin) (Object) box).getAnimationStatus();
        if (status == ShulkerBoxBlockEntity.AnimationStatus.CLOSED
                || status == ShulkerBoxBlockEntity.AnimationStatus.OPENED) {
            ci.cancel();
        }
    }
}
