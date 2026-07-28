package org.omnifix.mixin.perf;

import net.minecraft.client.gui.screens.ConnectScreen;
import org.omnifix.client.OmniFixClientTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMeasureTimeMixin {

    @Inject(method = "connect", at = @At("HEAD"))
    private void omnifix$recordConnectStartTime(CallbackInfo ci) {
        OmniFixClientTiming.markWorldLoadStart();
    }
}
