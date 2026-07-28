package org.omnifix.mixin.perf;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * doWorldLoad spin-waits with Thread.yield(); sleep briefly instead to free CPU for the server
 * thread while it prepares.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftWorldLoadSleepMixin {

    @Redirect(
            method = "doWorldLoad",
            at = @At(value = "INVOKE", target = "Ljava/lang/Thread;yield()V")
    )
    private void omnifix$sleepInsteadOfYield() {
        try {
            Thread.sleep(16L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
