package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: inactive conduits still enter {@code serverTick} every game tick. Vanilla only needs
 * shape refresh every 40 game ticks when inactive; between those boundaries the body is effectively
 * {@code tickCount++}. Skip the rest while inactive, still advancing {@code tickCount}.
 */
@Mixin(ConduitBlockEntity.class)
public abstract class ConduitInactiveThrottleMixin {

    @Shadow
    public int tickCount;

    @Shadow
    public abstract boolean isActive();

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$throttleInactiveConduit(
            Level level,
            BlockPos pos,
            BlockState state,
            ConduitBlockEntity conduit,
            CallbackInfo ci
    ) {
        ConduitInactiveThrottleMixin self = (ConduitInactiveThrottleMixin) (Object) conduit;
        if (self.isActive()) {
            return;
        }
        // Shape refresh runs when gameTime % 40 == 0 (vanilla BLOCK_REFRESH_RATE).
        if (level.getGameTime() % 40L != 0L) {
            self.tickCount++;
            ci.cancel();
        }
    }
}
