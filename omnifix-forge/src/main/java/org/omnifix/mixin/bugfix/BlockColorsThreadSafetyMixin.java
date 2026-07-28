package org.omnifix.mixin.bugfix;

import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Serialize BlockColors.register to avoid CME when mods register off-thread or concurrently. */
@Mixin(value = BlockColors.class, priority = 700)
public abstract class BlockColorsThreadSafetyMixin {

    @Unique
    private final Lock omnifix$mapLock = new ReentrantLock();

    @Inject(method = "register", at = @At("HEAD"))
    private void omnifix$lockMapBeforeAccess(CallbackInfo ci) {
        omnifix$mapLock.lock();
    }

    @Inject(method = "register", at = @At("TAIL"))
    private void omnifix$unlockMap(CallbackInfo ci) {
        omnifix$mapLock.unlock();
    }
}
