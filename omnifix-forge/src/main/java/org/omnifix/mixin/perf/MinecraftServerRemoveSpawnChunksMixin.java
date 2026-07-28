package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WorldData;
import org.apache.commons.lang3.tuple.Pair;
import org.omnifix.duck.ISpawnTrackingMinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

/**
 * Replace permanent world-spawn region tickets with a temporary START ticket at the player
 * position (integrated) or a one-shot chunk load (dedicated).
 */
@Mixin(value = MinecraftServer.class, priority = 1100)
public abstract class MinecraftServerRemoveSpawnChunksMixin implements ISpawnTrackingMinecraftServer {

    @Unique
    private static final Logger OMNIFIX$LOGGER = LogUtils.getLogger();

    @Shadow
    public abstract boolean isDedicatedServer();

    @Shadow
    public abstract WorldData getWorldData();

    @Shadow
    @Nullable
    public abstract ServerLevel getLevel(ResourceKey<Level> dimension);

    @Unique
    private Pair<ResourceKey<Level>, ChunkPos> omnifix$initialSpawnLocation;

    @Unique
    @Nullable
    private Pair<ResourceKey<Level>, ChunkPos> omnifix$loadPlayerSpawnLocation() {
        CompoundTag player = this.getWorldData().getLoadedPlayerTag();
        if (player == null) {
            return null;
        }
        ListTag pos = player.getList("Pos", CompoundTag.TAG_DOUBLE);
        double x = pos.getDouble(0);
        double z = pos.getDouble(2);
        ResourceKey<Level> dimension = DimensionType.parseLegacy(
                new Dynamic<>(NbtOps.INSTANCE, player.get("Dimension"))
        ).resultOrPartial(OMNIFIX$LOGGER::error).orElse(Level.OVERWORLD);
        return Pair.of(dimension, new ChunkPos(
                SectionPos.blockToSectionCoord(Mth.floor(x)),
                SectionPos.blockToSectionCoord(Mth.floor(z))));
    }

    @Redirect(
            method = "prepareLevels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerChunkCache;addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"
            )
    )
    private void omnifix$addSpawnChunkTicket(
            ServerChunkCache cache,
            TicketType<?> type,
            ChunkPos pos,
            int distance,
            Object o,
            @Local(ordinal = 0, argsOnly = true) ChunkProgressListener listener
    ) {
        if (!this.isDedicatedServer()) {
            var pair = this.omnifix$initialSpawnLocation = omnifix$loadPlayerSpawnLocation();
            if (pair != null) {
                var level = this.getLevel(pair.getLeft());
                if (level != null) {
                    cache = level.getChunkSource();
                    pos = pair.getRight();
                }
            }
            listener.updateSpawnPos(pos);
            cache.addRegionTicket(TicketType.START, pos, 0, Unit.INSTANCE);
        } else {
            cache.getChunk(pos.x, pos.z, true);
        }
    }

    @Redirect(
            method = "prepareLevels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerChunkCache;getTickingGenerated()I"
            ),
            require = 0
    )
    private int omnifix$getGenerated(ServerChunkCache cache) {
        return 441;
    }

    @Override
    public Pair<ResourceKey<Level>, ChunkPos> omnifix$getInitialStartTicketLocation() {
        var pair = this.omnifix$initialSpawnLocation;
        this.omnifix$initialSpawnLocation = null;
        return pair;
    }
}
