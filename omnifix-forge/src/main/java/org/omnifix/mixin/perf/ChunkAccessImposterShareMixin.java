package org.omnifix.mixin.perf;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Shared shadow fields for {@link ImposterProtoChunkCompactMixin}.
 */
@Mixin(ChunkAccess.class)
public abstract class ChunkAccessImposterShareMixin {

    @Shadow
    @Final
    @Mutable
    protected LevelChunkSection[] sections;

    @Shadow
    protected ChunkSkyLightSources skyLightSources;
}
