package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: idle furnaces/smokers/blast furnaces still enter {@code serverTick} every game tick
 * (read slots, recipe gate, block-state lit flip checks) even when cold and empty. Skip the entire
 * tick when there is nothing that can cook or burn.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceIdleMixin {

    @Shadow
    protected NonNullList<ItemStack> items;

    @Shadow
    int litTime;

    @Shadow
    int cookingProgress;

    @Shadow
    private boolean isLit() {
        throw new AssertionError();
    }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipIdleFurnace(
            Level level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo ci
    ) {
        FurnaceIdleMixin self = (FurnaceIdleMixin) (Object) furnace;
        if (self.isLit() || self.litTime > 0 || self.cookingProgress > 0) {
            return;
        }
        // Slot 0 = input, 1 = fuel, 2 = result (result may be non-empty while idle — still skip work).
        if (!self.items.get(0).isEmpty() || !self.items.get(1).isEmpty()) {
            return;
        }
        ci.cancel();
    }
}
