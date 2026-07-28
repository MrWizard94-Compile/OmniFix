package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Root cause: {@link ExperienceOrb#scanForEntities} (from tick on ENTITY_SCAN_PERIOD) calls
 * {@link Level#getEntities} to find merge candidates. Under dense XP dumps many orbs re-scan the
 * same empty neighbourhood in one game tick (same class of hot path as item-entity merge).
 *
 * <p>Independent same-tick empty cache keyed by level identity + block position.
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbScanCacheMixin {

    @Unique
    private static final int OMNIFIX$CACHE_CAP = 4096;

    @Unique
    private static final ConcurrentHashMap<Long, Long> OMNIFIX$EMPTY_AT_TICK = new ConcurrentHashMap<>();

    @Unique
    private static long omnifix$lastPruneGameTime = Long.MIN_VALUE;

    @WrapOperation(
            method = "scanForEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private <T extends Entity> List<T> omnifix$cacheEmptyOrbScan(
            Level level,
            EntityTypeTest<Entity, T> typeTest,
            AABB box,
            Predicate<? super T> predicate,
            Operation<List<T>> original
    ) {
        if (level.isClientSide) {
            return original.call(level, typeTest, box, predicate);
        }
        ExperienceOrb self = (ExperienceOrb) (Object) this;
        long gameTime = level.getGameTime();
        long key = omnifix$cacheKey(level, self);
        Long emptyAt = OMNIFIX$EMPTY_AT_TICK.get(key);
        if (emptyAt != null && emptyAt == gameTime) {
            return Collections.emptyList();
        }
        List<T> result = original.call(level, typeTest, box, predicate);
        if (result == null || result.isEmpty()) {
            OMNIFIX$EMPTY_AT_TICK.put(key, gameTime);
            omnifix$prune(gameTime);
        } else {
            OMNIFIX$EMPTY_AT_TICK.remove(key);
        }
        return result;
    }

    @Unique
    private static long omnifix$cacheKey(Level level, ExperienceOrb orb) {
        return orb.blockPosition().asLong() ^ (((long) System.identityHashCode(level)) << 1);
    }

    @Unique
    private static void omnifix$prune(long gameTime) {
        if (gameTime == omnifix$lastPruneGameTime) {
            return;
        }
        omnifix$lastPruneGameTime = gameTime;
        if (OMNIFIX$EMPTY_AT_TICK.size() < OMNIFIX$CACHE_CAP) {
            Iterator<ConcurrentHashMap.Entry<Long, Long>> it = OMNIFIX$EMPTY_AT_TICK.entrySet().iterator();
            while (it.hasNext()) {
                ConcurrentHashMap.Entry<Long, Long> e = it.next();
                if (e.getValue() < gameTime - 1L) {
                    it.remove();
                }
            }
            return;
        }
        OMNIFIX$EMPTY_AT_TICK.entrySet().removeIf(e -> e.getValue() < gameTime - 1L);
        if (OMNIFIX$EMPTY_AT_TICK.size() > OMNIFIX$CACHE_CAP) {
            OMNIFIX$EMPTY_AT_TICK.clear();
        }
    }
}
