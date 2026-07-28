package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Root cause: {@link HopperBlockEntity#getItemsAtAndAbove} is invoked from the entity-suction
 * path in {@link HopperBlockEntity#suckInItems} whenever there is no inventory above the hopper.
 * That method streams the suck-shape AABBs and calls {@link Level#getEntitiesOfClass} every time.
 * When nothing is transferred, hopper cooldown is not applied, so empty hoppers re-query item
 * entities on every server tick — a known hot path under large hopper farms (Lithium/Canary class
 * of fix; this is an independent reimplementation).
 *
 * <p>Fix: remember hopper positions that already observed an empty entity result for the current
 * {@link Level#getGameTime() game time}. Subsequent calls for the same level + block position in
 * that tick return an empty list without touching the entity section maps. Non-empty results never
 * enter the empty-cache (and clear any prior empty entry) so pickup latency is unchanged when items
 * are present.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperEntityCacheMixin {

    /** Max retained empty markers before an aggressive prune/clear. */
    @Unique
    private static final int OMNIFIX$CACHE_CAP = 4096;

    /**
     * Key: packed hopper block pos mixed with level identity.
     * Value: gameTime when an empty suction result was last observed.
     */
    @Unique
    private static final ConcurrentHashMap<Long, Long> OMNIFIX$EMPTY_AT_TICK = new ConcurrentHashMap<>();

    @Unique
    private static long omnifix$lastPruneGameTime = Long.MIN_VALUE;

    @Inject(method = "getItemsAtAndAbove", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipEmptyEntityQuery(
            Level level,
            Hopper hopper,
            CallbackInfoReturnable<List<ItemEntity>> cir
    ) {
        if (level.isClientSide) {
            return;
        }
        long gameTime = level.getGameTime();
        long key = omnifix$cacheKey(level, hopper);
        Long emptyAt = OMNIFIX$EMPTY_AT_TICK.get(key);
        if (emptyAt != null && emptyAt == gameTime) {
            cir.setReturnValue(Collections.emptyList());
        }
    }

    @Inject(method = "getItemsAtAndAbove", at = @At("RETURN"))
    private static void omnifix$rememberEmptyEntityQuery(
            Level level,
            Hopper hopper,
            CallbackInfoReturnable<List<ItemEntity>> cir
    ) {
        if (level.isClientSide) {
            return;
        }
        long gameTime = level.getGameTime();
        long key = omnifix$cacheKey(level, hopper);
        List<ItemEntity> items = cir.getReturnValue();
        if (items == null || items.isEmpty()) {
            OMNIFIX$EMPTY_AT_TICK.put(key, gameTime);
            omnifix$pruneCache(gameTime);
        } else {
            OMNIFIX$EMPTY_AT_TICK.remove(key);
        }
    }

    @Unique
    private static long omnifix$cacheKey(Level level, Hopper hopper) {
        long pos = omnifix$hopperPosLong(hopper);
        // Mix level identity so overworld/nether/etc. positions do not collide.
        int levelId = System.identityHashCode(level);
        return pos ^ ((long) levelId << 32) ^ (levelId & 0xffffffffL);
    }

    @Unique
    private static long omnifix$hopperPosLong(Hopper hopper) {
        if (hopper instanceof BlockEntity blockEntity) {
            return blockEntity.getBlockPos().asLong();
        }
        return BlockPos.asLong(
                Mth.floor(hopper.getLevelX()),
                Mth.floor(hopper.getLevelY()),
                Mth.floor(hopper.getLevelZ())
        );
    }

    /**
     * Drop entries older than two game ticks; hard-clear if still over capacity.
     * Hoppers tick on the server main thread, but the map is concurrent for modded off-thread safety.
     */
    @Unique
    private static void omnifix$pruneCache(long gameTime) {
        if (gameTime != omnifix$lastPruneGameTime) {
            omnifix$lastPruneGameTime = gameTime;
            if (OMNIFIX$EMPTY_AT_TICK.size() > 256) {
                omnifix$removeStale(gameTime);
            }
        }
        if (OMNIFIX$EMPTY_AT_TICK.size() > OMNIFIX$CACHE_CAP) {
            omnifix$removeStale(gameTime);
            if (OMNIFIX$EMPTY_AT_TICK.size() > OMNIFIX$CACHE_CAP) {
                OMNIFIX$EMPTY_AT_TICK.clear();
            }
        }
    }

    @Unique
    private static void omnifix$removeStale(long gameTime) {
        Iterator<Map.Entry<Long, Long>> it = OMNIFIX$EMPTY_AT_TICK.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Long> entry = it.next();
            long emptyAt = entry.getValue();
            // Retain current and previous tick only; older markers cannot match same-tick hits.
            if (gameTime - emptyAt > 1L) {
                it.remove();
            }
        }
    }
}
