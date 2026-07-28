package org.omnifix.mixin.perf;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.omnifix.blockstate.BlockStateCacheHandler;
import org.omnifix.duck.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(value = Blocks.class, priority = 1100)
public abstract class BlocksRebuildCacheMixin {

    @ModifyArg(
            method = "rebuildCache",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/IdMapper;forEach(Ljava/util/function/Consumer;)V"
            ),
            index = 0
    )
    private static Consumer<?> omnifix$getEmptyConsumer(Consumer<?> original) {
        BlockStateCacheHandler.invalidateCache();
        return o -> {};
    }

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;initCache()V"
            ),
            require = 0
    )
    private static void omnifix$skipCacheInit(BlockState state) {
        Items.AIR.asItem();
        ((IBlockState) state).omnifix$clearCache();
    }
}
