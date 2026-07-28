package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: idle brewing stands still run {@code serverTick} every game tick (fuel check +
 * isBrewable scan) even when empty. Skip when there is no active brew, no blaze fuel to load,
 * and no brewable configuration.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandIdleMixin {

    @Shadow
    private NonNullList<ItemStack> items;

    @Shadow
    int fuel;

    @Shadow
    int brewTime;

    @Shadow
    private static boolean isBrewable(NonNullList<ItemStack> items) {
        throw new AssertionError();
    }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipIdleBrewingStand(
            Level level,
            BlockPos pos,
            BlockState state,
            BrewingStandBlockEntity stand,
            CallbackInfo ci
    ) {
        BrewingStandIdleMixin self = (BrewingStandIdleMixin) (Object) stand;
        if (self.brewTime > 0 || self.fuel > 0) {
            return;
        }
        ItemStack fuelSlot = self.items.get(4);
        if (fuelSlot.is(Items.BLAZE_POWDER)) {
            return;
        }
        if (isBrewable(self.items)) {
            return;
        }
        ci.cancel();
    }
}
