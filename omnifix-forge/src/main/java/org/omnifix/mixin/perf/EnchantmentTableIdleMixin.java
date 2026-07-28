package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: every enchanting table runs full client {@code bookAnimationTick} (nearest-player
 * search + flip math) every tick even when the book is fully closed and no player is nearby.
 * When {@code open} and {@code oOpen} are both 0, only every 4th tick is needed to discover a
 * nearby player and begin opening; the other three ticks are pure idle animation cost.
 */
@Mixin(EnchantmentTableBlockEntity.class)
public abstract class EnchantmentTableIdleMixin {

    @Inject(method = "bookAnimationTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$throttleClosedBook(
            Level level,
            BlockPos pos,
            BlockState state,
            EnchantmentTableBlockEntity table,
            CallbackInfo ci
    ) {
        if (table.open == 0.0F && table.oOpen == 0.0F && (level.getGameTime() & 3L) != 0L) {
            ci.cancel();
        }
    }
}
