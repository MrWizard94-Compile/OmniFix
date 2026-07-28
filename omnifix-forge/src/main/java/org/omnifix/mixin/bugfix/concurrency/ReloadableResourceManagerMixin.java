package org.omnifix.mixin.bugfix.concurrency;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Defers off-thread {@code registerReloadListener} calls on the client resource manager so they
 * cannot race the main-thread reload listener list.
 */
@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    private PackType type;

    @Shadow
    public abstract void registerReloadListener(PreparableReloadListener listener);

    /**
     * @author embeddedt / OmniFix port
     * @reason complain loudly when reload listeners are being registered too late in a way that would cause
     * concurrency issues, and prevent them from crashing the game
     */
    @WrapMethod(method = "registerReloadListener")
    private void omnifix$checkCallingThread(PreparableReloadListener listener, Operation<Void> original) {
        Minecraft minecraft = Minecraft.getInstance();
        // OmniFix does not track ModernFixForge.registryEventsFired; once the client Minecraft
        // instance exists, any off-thread registration against the client resource manager is unsafe.
        if (this.type == PackType.CLIENT_RESOURCES
                && minecraft != null
                && (Object) this == minecraft.getResourceManager()
                && !minecraft.isSameThread()) {
            LOGGER.error(
                    "A mod is calling registerReloadListener at the wrong time. This will cause random "
                            + "concurrency crashes. Please report this to the mod author.",
                    new Exception("registerReloadListener called on wrong thread"));
            // Defer onto the main client thread for predictable behaviour.
            minecraft.tell(() -> this.registerReloadListener(listener));
            return;
        }

        original.call(listener);
    }
}
