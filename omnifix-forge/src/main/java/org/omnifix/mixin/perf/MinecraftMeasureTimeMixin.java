package org.omnifix.mixin.perf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import org.jetbrains.annotations.Nullable;
import org.omnifix.client.OmniFixClientTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMeasureTimeMixin {

    @Shadow
    @Nullable
    public Overlay overlay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void omnifix$onClientTick(CallbackInfo ci) {
        if (this.overlay == null) {
            OmniFixClientTiming.onGameLaunchFinish();
        }
    }

    @Inject(method = "doWorldLoad", at = @At("HEAD"))
    private void omnifix$recordWorldLoadStart(CallbackInfo ci) {
        OmniFixClientTiming.markWorldLoadStart();
    }
}
