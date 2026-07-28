package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: campfires run {@code cookTick}/{@code cooldownTick} every tick even when empty.
 * Skip when there is no food and no residual cooking progress to decay.
 */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireIdleMixin {

    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    @Shadow
    @Final
    private int[] cookingProgress;

    @Inject(method = "cookTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipEmptyCook(
            Level level,
            BlockPos pos,
            BlockState state,
            CampfireBlockEntity campfire,
            CallbackInfo ci
    ) {
        CampfireIdleMixin self = (CampfireIdleMixin) (Object) campfire;
        for (ItemStack stack : self.items) {
            if (!stack.isEmpty()) {
                return;
            }
        }
        ci.cancel();
    }

    @Inject(method = "cooldownTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipEmptyCooldown(
            Level level,
            BlockPos pos,
            BlockState state,
            CampfireBlockEntity campfire,
            CallbackInfo ci
    ) {
        CampfireIdleMixin self = (CampfireIdleMixin) (Object) campfire;
        for (int progress : self.cookingProgress) {
            if (progress > 0) {
                return;
            }
        }
        ci.cancel();
    }
}
