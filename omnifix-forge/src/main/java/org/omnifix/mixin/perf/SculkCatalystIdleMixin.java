package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: every sculk catalyst runs {@code serverTick} → {@code SculkSpreader.updateCursors}
 * every game tick even when the cursor list is empty (no recent kills to spread). That path still
 * walks the empty list and bookkeeping. Skip until a game event adds charge cursors.
 */
@Mixin(SculkCatalystBlockEntity.class)
public abstract class SculkCatalystIdleMixin {

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipEmptyCatalyst(
            Level level,
            BlockPos pos,
            BlockState state,
            SculkCatalystBlockEntity catalyst,
            CallbackInfo ci
    ) {
        if (catalyst.getListener().getSculkSpreader().getCursors().isEmpty()) {
            ci.cancel();
        }
    }
}
