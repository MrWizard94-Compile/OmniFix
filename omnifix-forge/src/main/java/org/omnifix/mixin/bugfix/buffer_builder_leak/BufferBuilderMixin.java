package org.omnifix.mixin.bugfix.buffer_builder_leak;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.logging.LogUtils;
import org.omnifix.render.UnsafeBufferHelper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

/**
 * When a {@link BufferBuilder} is GC'd without {@code end}/{@code discard}, its direct
 * {@link ByteBuffer} leaks native memory. Finalize frees it; Flywheel-shared buffers skip free.
 */
@Mixin(value = BufferBuilder.class, priority = 1500)
public abstract class BufferBuilderMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    private ByteBuffer buffer;

    @Unique
    private static boolean omnifix$leakReported;

    @Unique
    private boolean omnifix$shouldFree = true;

    @Dynamic
    @Inject(method = "flywheel$injectForRender", at = @At("RETURN"), remap = false, require = 0)
    private void omnifix$preventFreeWhenFlywheel(CallbackInfo ci) {
        omnifix$shouldFree = false;
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void omnifix$initUnsafeHelper(CallbackInfo ci) {
        UnsafeBufferHelper.init();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            ByteBuffer buf = buffer;
            if (buf != null && omnifix$shouldFree) {
                if (!omnifix$leakReported) {
                    omnifix$leakReported = true;
                    LOGGER.warn("[OmniFix] BufferBuilder native buffer leaked; freeing via UnsafeBufferHelper.");
                }
                UnsafeBufferHelper.free(buf);
                buffer = null;
            }
        } finally {
            super.finalize();
        }
    }
}
