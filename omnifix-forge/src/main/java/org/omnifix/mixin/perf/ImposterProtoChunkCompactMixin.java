package org.omnifix.mixin.perf;

import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ImposterProtoChunk allocates duplicate section arrays and sky-light sources for a read-through
 * view of a LevelChunk. Share the wrapped chunk's arrays/objects instead.
 */
@Mixin(ImposterProtoChunk.class)
public abstract class ImposterProtoChunkCompactMixin extends ChunkAccessImposterShareMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$shareWrappedArrays(LevelChunk wrapped, boolean allowWrites, CallbackInfo ci) {
        this.sections = wrapped.getSections();
        this.skyLightSources = wrapped.getSkyLightSources();
    }
}
