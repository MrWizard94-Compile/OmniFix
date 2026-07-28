package org.omnifix.mixin.perf;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Root cause: {@link Level#tickBlockEntities} always pushes a profiler section and walks
 * pending/fresh queues even when every BE list is empty. Fast-path cancel when idle.
 */
@Mixin(Level.class)
public abstract class EmptyBlockEntityTickMixin {

    @Shadow
    @Final
    public List<TickingBlockEntity> blockEntityTickers;

    @Shadow
    @Final
    private List<TickingBlockEntity> pendingBlockEntityTickers;

    @Shadow
    @Final
    private ArrayList<BlockEntity> freshBlockEntities;

    @Shadow
    @Final
    private ArrayList<BlockEntity> pendingFreshBlockEntities;

    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void omnifix$skipEmptyBeTick(CallbackInfo ci) {
        if (this.blockEntityTickers.isEmpty()
                && this.pendingBlockEntityTickers.isEmpty()
                && this.freshBlockEntities.isEmpty()
                && this.pendingFreshBlockEntities.isEmpty()) {
            ci.cancel();
        }
    }
}
