package org.omnifix.mixin.net;

import io.netty.handler.timeout.ReadTimeoutHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Ensures any {@link ReadTimeoutHandler} constructed with a sub-120s budget is raised to 120s.
 * Large recipe/registry payloads on join otherwise trip vanilla 30s read timeouts.
 */
@Mixin(value = ReadTimeoutHandler.class, remap = false)
public abstract class ReadTimeoutHandlerMixin {

    private static final int OMNIFIX_MIN_SECONDS = 120;

    @ModifyVariable(method = "<init>(I)V", at = @At("HEAD"), argsOnly = true, require = 0)
    private static int omnifix$floorTimeoutSeconds(int timeoutSeconds) {
        return Math.max(timeoutSeconds, OMNIFIX_MIN_SECONDS);
    }

    @ModifyVariable(method = "<init>(JLjava/util/concurrent/TimeUnit;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private static long omnifix$floorTimeoutDuration(long timeout) {
        // Only boost small second-scale values; leave long-unit conversions alone if already large.
        if (timeout > 0 && timeout < OMNIFIX_MIN_SECONDS) {
            return OMNIFIX_MIN_SECONDS;
        }
        return timeout;
    }
}
