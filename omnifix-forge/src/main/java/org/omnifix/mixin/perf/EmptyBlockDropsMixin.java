package org.omnifix.mixin.perf;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

/**
 * Root cause: {@code BlockState.getDrops} always builds loot params and queries the loot data
 * manager even when the block declares {@link BuiltInLootTables#EMPTY} (no drops). Short-circuit
 * avoids that work on bulk break / worldgen cleanup of empty-loot blocks.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class EmptyBlockDropsMixin {

    @Inject(
            method = "getDrops(Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void omnifix$skipEmptyLootTable(LootParams.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        BlockBehaviour.BlockStateBase self = (BlockBehaviour.BlockStateBase) (Object) this;
        Block block = self.getBlock();
        ResourceLocation table = block.getLootTable();
        if (table != null && table.equals(BuiltInLootTables.EMPTY)) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
