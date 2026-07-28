package org.omnifix.mixin.bugfix.missing_block_entities;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hypixel (and possibly other buggy servers) send chunks to the client that are missing some block entity data, which
 * causes these entities to be invisible. We "fix" this by recreating the block entity on the client with default data,
 * which is hopefully what the legacy server also expects.
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    private Level level;

    @Shadow
    @Nullable
    public abstract BlockEntity getBlockEntity(BlockPos pos, LevelChunk.EntityCreationType creationType);

    public LevelChunkMixin(
            ChunkPos chunkPos,
            UpgradeData upgradeData,
            LevelHeightAccessor levelHeightAccessor,
            Registry<Biome> biomeRegistry,
            long inhabitedTime,
            @Nullable LevelChunkSection[] sections,
            @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, biomeRegistry, inhabitedTime, sections, blendingData);
    }

    @Inject(method = "replaceWithPacketData", at = @At("RETURN"))
    private void omnifix$validateBlockEntitiesInChunk(CallbackInfo ci) {
        // No reason to check in singleplayer or on the integrated server
        if (this.level.isClientSide && !Minecraft.getInstance().isLocalServer()) {
            for (int i = 0; i < this.sections.length; i++) {
                LevelChunkSection section = this.sections[i];
                try {
                    if (!section.hasOnlyAir() && section.maybeHas(BlockBehaviour.BlockStateBase::hasBlockEntity)) {
                        omnifix$scanSectionForBlockEntities(section, i);
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception validating data in chunk", e);
                    return;
                }
            }
        }
    }

    @Unique
    private void omnifix$scanSectionForBlockEntities(LevelChunkSection section, int i) {
        int chunkXOff = this.chunkPos.x * 16;
        int chunkZOff = this.chunkPos.z * 16;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int sectionYOff = this.getSectionYFromSectionIndex(i) * 16;
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = section.getBlockState(x, y, z);
                    if (state.hasBlockEntity()) {
                        cursor.set(chunkXOff + x, sectionYOff + y, chunkZOff + z);
                        omnifix$makeBlockEntityIfNotExists(state, cursor);
                    }
                }
            }
        }
    }

    @Unique
    private void omnifix$makeBlockEntityIfNotExists(BlockState state, BlockPos.MutableBlockPos pos) {
        if (this.blockEntities.containsKey(pos) || this.pendingBlockEntities.containsKey(pos)) {
            return;
        }

        BlockEntity blockEntity = this.getBlockEntity(pos.immutable(), LevelChunk.EntityCreationType.IMMEDIATE);
        if (blockEntity != null && LOGGER.isDebugEnabled()) {
            String blockName = state.getBlock().toString();
            LOGGER.debug("Created missing block entity for {} at {}", blockName, pos.toShortString());
        }
    }
}
