package org.omnifix.mixin.perf;

import net.minecraftforge.registries.GameData;
import org.omnifix.forge.registry.ObjectHolderClearer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After Forge finishes registry events, drop redundant ObjectHolders and clear stacktraces.
 */
@Mixin(value = GameData.class, remap = false)
public abstract class GameDataObjectHolderMixin {

    @Inject(method = "postRegisterEvents", at = @At("RETURN"))
    private static void omnifix$cleanupHolders(CallbackInfo ci) {
        ObjectHolderClearer.removeRedundantHolders();
    }
}
