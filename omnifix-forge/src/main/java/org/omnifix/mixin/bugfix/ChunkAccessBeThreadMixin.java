package org.omnifix.mixin.bugfix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.omnifix.util.ConcurrencySanitizingMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Wrap block entity maps so cross-thread access fails loudly instead of silent corruption/CME.
 */
@Mixin(ChunkAccess.class)
public abstract class ChunkAccessBeThreadMixin {

    @Shadow
    @Final
    @Mutable
    protected Map<BlockPos, BlockEntity> blockEntities;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$wrapInConcurrencyDetector(
            ChunkPos chunkPos,
            UpgradeData upgradeData,
            LevelHeightAccessor levelHeightAccessor,
            Registry<?> biomeRegistry,
            long inhabitedTime,
            LevelChunkSection[] sections,
            BlendingData blendingData,
            CallbackInfo ci
    ) {
        if (levelHeightAccessor instanceof Level level) {
            this.blockEntities = new ConcurrencySanitizingMap<>(
                    this.blockEntities,
                    ((LevelThreadAccessor) level).getThread()
            );
        }
    }
}
