package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: every chest runs {@code lidAnimateTick} → {@code ChestLidController.tickLid} every
 * tick, even when the lid is fully closed ({@code openness == 0} and {@code shouldBeOpen == false}).
 * That path only copies openness → oOpenness. Skip until openers flip shouldBeOpen.
 */
@Mixin(ChestBlockEntity.class)
public abstract class ChestLidIdleMixin {

    @Shadow
    @Final
    private ChestLidController chestLidController;

    @Inject(method = "lidAnimateTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipIdleChestLid(
            Level level,
            BlockPos pos,
            BlockState state,
            ChestBlockEntity chest,
            CallbackInfo ci
    ) {
        ChestLidController lid = ((ChestLidIdleMixin) (Object) chest).chestLidController;
        ChestLidControllerAccessor acc = (ChestLidControllerAccessor) lid;
        if (!acc.omnifix$shouldBeOpen() && acc.omnifix$openness() == 0.0F) {
            // Keep oOpenness in sync without running the full tick body when already closed.
            acc.omnifix$setOOpenness(0.0F);
            ci.cancel();
        }
    }
}
