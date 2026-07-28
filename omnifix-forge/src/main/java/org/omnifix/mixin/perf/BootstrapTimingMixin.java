package org.omnifix.mixin.perf;

import com.google.common.base.Stopwatch;
import net.minecraft.server.Bootstrap;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.TimeUnit;

@Mixin(Bootstrap.class)
public abstract class BootstrapTimingMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Unique
    private static Stopwatch omnifix$startWatch;

    @Inject(
            method = "bootStrap",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTSTATIC,
                    target = "Lnet/minecraft/server/Bootstrap;isBootstrapped:Z",
                    ordinal = 0
            )
    )
    private static void omnifix$recordStartTime(CallbackInfo ci) {
        omnifix$startWatch = Stopwatch.createStarted();
    }

    @Inject(method = "bootStrap", at = @At("RETURN"))
    private static void omnifix$printStartTime(CallbackInfo ci) {
        if (omnifix$startWatch != null && omnifix$startWatch.isRunning()) {
            omnifix$startWatch.stop();
            LOGGER.info(
                    "[OmniFix] Vanilla bootstrap took {} milliseconds",
                    omnifix$startWatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }
}
