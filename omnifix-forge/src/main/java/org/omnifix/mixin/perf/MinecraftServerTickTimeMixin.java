package org.omnifix.mixin.perf;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import org.omnifix.duck.ITimeTrackingServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerTickTimeMixin implements ITimeTrackingServer {

    @Unique
    private long omnifix$lastTickStartTime = -1L;

    @Override
    public long omnifix$getLastTickStartTime() {
        return omnifix$lastTickStartTime;
    }

    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V"
            )
    )
    private void omnifix$trackTickTime(CallbackInfo ci) {
        omnifix$lastTickStartTime = Util.getMillis();
    }
}
