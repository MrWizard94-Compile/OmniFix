package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Without permanent spawn chunks, end portals into unloaded overworld spawn can drop entities
 * into the void. Create a temporary PORTAL ticket when needed.
 */
@Mixin(Entity.class)
public abstract class EntityPortalSpawnLoadMixin {

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"
            ),
            require = 0
    )
    private BlockPos omnifix$triggerChunkloadAtSpawnPos(BlockPos spawnPos, ServerLevel destination) {
        if (destination.dimension() == ServerLevel.OVERWORLD) {
            if (!destination.hasChunk(spawnPos.getX() >> 4, spawnPos.getZ() >> 4)) {
                BlockPos key = spawnPos.immutable();
                destination.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(key), 3, key);
                destination.getChunk(key);
            }
        }
        return spawnPos;
    }
}
