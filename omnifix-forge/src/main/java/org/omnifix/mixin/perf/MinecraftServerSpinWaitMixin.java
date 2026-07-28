package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

/**
 * Replaces busy-spin waiting between server ticks with {@link LockSupport#parkNanos}, cutting
 * idle CPU waste on integrated and dedicated servers (ModernFix fix_loop_spin_waiting).
 */
@Mixin(value = MinecraftServer.class, priority = 500)
public abstract class MinecraftServerSpinWaitMixin extends BlockableEventLoop<Runnable> {

    @Shadow
    private long nextTickTime;

    protected MinecraftServerSpinWaitMixin(String name) {
        super(name);
    }

    @Unique
    private boolean omnifix$waitingForNextTick;

    @WrapOperation(
            method = "waitUntilNextTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;managedBlock(Ljava/util/function/BooleanSupplier;)V"
            ))
    private void omnifix$markWaiting(MinecraftServer instance, BooleanSupplier isDone, Operation<Void> original) {
        try {
            this.omnifix$waitingForNextTick = true;
            original.call(instance, isDone);
        } finally {
            this.omnifix$waitingForNextTick = false;
        }
    }

    @Override
    protected void waitForTasks() {
        if (this.omnifix$waitingForNextTick) {
            LockSupport.parkNanos("waiting for tasks", (this.nextTickTime * 1_000_000L) - Util.getNanos());
        } else {
            super.waitForTasks();
        }
    }
}
