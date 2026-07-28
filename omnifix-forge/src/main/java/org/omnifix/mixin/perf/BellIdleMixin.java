package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: every placed bell runs {@code clientTick}/{@code serverTick} every game tick even when
 * idle. Vanilla tick only advances state while {@code shaking} or {@code resonating}; when both are
 * false the body is pure field reads. Skip the full tick until {@link BellBlockEntity#onHit} sets
 * shaking again.
 */
@Mixin(BellBlockEntity.class)
public abstract class BellIdleMixin {

    @Shadow
    public boolean shaking;

    @Shadow
    private boolean resonating;

    @Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipIdleBellClient(
            Level level,
            BlockPos pos,
            BlockState state,
            BellBlockEntity bell,
            CallbackInfo ci
    ) {
        BellIdleMixin self = (BellIdleMixin) (Object) bell;
        if (!self.shaking && !self.resonating) {
            ci.cancel();
        }
    }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipIdleBellServer(
            Level level,
            BlockPos pos,
            BlockState state,
            BellBlockEntity bell,
            CallbackInfo ci
    ) {
        BellIdleMixin self = (BellIdleMixin) (Object) bell;
        if (!self.shaking && !self.resonating) {
            ci.cancel();
        }
    }
}
