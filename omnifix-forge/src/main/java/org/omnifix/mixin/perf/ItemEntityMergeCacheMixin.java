package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Root cause: {@link ItemEntity} periodically scans for nearby merge candidates via
 * {@code mergeWithNeighbours()}, which calls {@link Level#getEntitiesOfClass} every time
 * (from {@link ItemEntity#tick} on a 2/40-tick cadence when {@code isMergable()}, and after
 * dimension change). Under item dumps and farms that is a hot path — many co-located item
 * entities re-query the same empty neighbourhood in the same game tick (Lithium-class issue;
 * independent reimplementation, not a GPL copy).
 *
 * <p>Fix: remember block positions that already observed an empty merge-candidate result for
 * the current {@link Level#getGameTime() game time}. Subsequent {@code mergeWithNeighbours}
 * calls for the same level + block position in that tick skip the entity section scan.
 * Non-empty results never enter the empty-cache (and clear any prior empty entry) so merges
 * still happen immediately when items are present.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMergeCacheMixin {

    /** Max retained empty markers before an aggressive prune/clear. */
    @Unique
    private static final int OMNIFIX$CACHE_CAP = 4096;

    /**
     * Key: packed item block pos mixed with level identity.
     * Value: gameTime when an empty merge-candidate result was last observed.
     */
    @Unique
    private static final ConcurrentHashMap<Long, Long> OMNIFIX$EMPTY_AT_TICK = new ConcurrentHashMap<>();

    @Unique
    private static long omnifix$lastPruneGameTime = Long.MIN_VALUE;

    @Inject(method = "mergeWithNeighbours", at = @At("HEAD"), cancellable = true)
    private void omnifix$skipEmptyMergeScan(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        Level level = self.level();
        if (level.isClientSide) {
            return;
        }
        long gameTime = level.getGameTime();
        long key = omnifix$cacheKey(level, self);
        Long emptyAt = OMNIFIX$EMPTY_AT_TICK.get(key);
        if (emptyAt != null && emptyAt == gameTime) {
            ci.cancel();
        }
    }

    /**
     * After a real neighbour scan, record empty markers / clear them on non-empty results.
     * Also short-circuits the {@code getEntitiesOfClass} call when the empty-cache already
     * matches (covers paths that reach the invoke without the HEAD cancel, if any).
     */
    @WrapOperation(
            method = "mergeWithNeighbours",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private List<ItemEntity> omnifix$rememberEmptyMergeScan(
            Level level,
            Class<ItemEntity> entityClass,
            AABB searchBox,
            Predicate<? super ItemEntity> predicate,
            Operation<List<ItemEntity>> original
    ) {
        if (level.isClientSide) {
            return original.call(level, entityClass, searchBox, predicate);
        }
        ItemEntity self = (ItemEntity) (Object) this;
        long gameTime = level.getGameTime();
        long key = omnifix$cacheKey(level, self);
        Long emptyAt = OMNIFIX$EMPTY_AT_TICK.get(key);
        if (emptyAt != null && emptyAt == gameTime) {
            return Collections.emptyList();
        }
        List<ItemEntity> items = original.call(level, entityClass, searchBox, predicate);
        if (items == null || items.isEmpty()) {
            OMNIFIX$EMPTY_AT_TICK.put(key, gameTime);
            omnifix$pruneCache(gameTime);
        } else {
            OMNIFIX$EMPTY_AT_TICK.remove(key);
        }
        return items;
    }

    @Unique
    private static long omnifix$cacheKey(Level level, ItemEntity item) {
        long pos = item.blockPosition().asLong();
        // Mix level identity so overworld/nether/etc. positions do not collide.
        int levelId = System.identityHashCode(level);
        return pos ^ ((long) levelId << 32) ^ (levelId & 0xffffffffL);
    }

    /**
     * Drop entries older than two game ticks; hard-clear if still over capacity.
     * Item entities tick on the server main thread, but the map is concurrent for modded
     * off-thread safety.
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
