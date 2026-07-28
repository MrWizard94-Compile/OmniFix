package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hide live (level-attached) BlockEntities from worldgen via ImposterProtoChunk when writes are
 * disallowed — prevents deadlock from setChanged during generation.
 */
@Mixin(ImposterProtoChunk.class)
public abstract class ImposterProtoChunkBeGuardMixin {

    @Unique
    private static final Logger OMNIFIX$LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    private boolean allowWrites;

    @ModifyExpressionValue(
            method = "getBlockEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"
            )
    )
    private BlockEntity omnifix$avoidLeakingLiveBE(
            BlockEntity original,
            @Local(ordinal = 0, argsOnly = true) BlockPos pos
    ) {
        if (!this.allowWrites && original != null && original.getLevel() != null) {
            OMNIFIX$LOGGER.debug(
                    "[OmniFix] Blocked live BE access at {} via ImposterProtoChunk during worldgen",
                    pos
            );
            return null;
        }
        return original;
    }
}
