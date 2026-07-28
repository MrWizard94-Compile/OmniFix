package org.omnifix.mixin.perf;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Do not maintain permanent tickets when the world spawn point changes. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelNoSpawnTicketMixin {

    @Redirect(
            method = "setDefaultSpawnPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerChunkCache;removeRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"
            )
    )
    private void omnifix$removeTicket(ServerChunkCache cache, TicketType<?> type, ChunkPos pos, int distance, Object o) {
    }

    @Redirect(
            method = "setDefaultSpawnPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerChunkCache;addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"
            )
    )
    private void omnifix$addTicket(ServerChunkCache cache, TicketType<?> type, ChunkPos pos, int distance, Object o) {
    }
}
