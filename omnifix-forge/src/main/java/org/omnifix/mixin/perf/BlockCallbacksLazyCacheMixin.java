package org.omnifix.mixin.perf;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import org.omnifix.blockstate.BlockStateCacheHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraftforge.registries.GameData$BlockCallbacks")
public abstract class BlockCallbacksLazyCacheMixin {

    @Redirect(
            method = "onBake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;initCache()V"
            )
    )
    private void omnifix$skipCache(BlockState instance) {
    }

    @Inject(method = "onBake", at = @At("TAIL"), remap = false)
    private void omnifix$computeCaches(
            IForgeRegistryInternal<Block> owner,
            RegistryManager stage,
            CallbackInfo ci
    ) {
        BlockStateCacheHandler.invalidateCache();
    }
}
