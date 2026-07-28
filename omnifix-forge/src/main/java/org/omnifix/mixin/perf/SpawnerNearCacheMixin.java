package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link BaseSpawner#serverTick} and {@code clientTick} each call {@code isNearPlayer},
 * which walks the player list with distance checks. Within one game tick the answer cannot change
 * for a given spawner instance; cache it on the spawner keyed by {@link Level#getGameTime()}.
 */
@Mixin(BaseSpawner.class)
public abstract class SpawnerNearCacheMixin {

    @Unique
    private long omnifix$nearCheckGameTime = Long.MIN_VALUE;

    @Unique
    private boolean omnifix$nearCached;

    @Inject(method = "isNearPlayer", at = @At("HEAD"), cancellable = true)
    private void omnifix$useNearCache(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        long t = level.getGameTime();
        if (t == this.omnifix$nearCheckGameTime) {
            cir.setReturnValue(this.omnifix$nearCached);
        }
    }

    @Inject(method = "isNearPlayer", at = @At("RETURN"))
    private void omnifix$storeNearCache(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.omnifix$nearCheckGameTime = level.getGameTime();
        this.omnifix$nearCached = cir.getReturnValueZ();
    }
}
