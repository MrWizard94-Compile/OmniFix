package org.omnifix.mixin.perf;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Throttles vanilla duplicate-entity UUID log spam.
 * <p>
 * Root causes (1.20.1 official):
 * <ul>
 *   <li>{@link EntityLookup#add} — {@code "Duplicate entity UUID {}: {}"}</li>
 *   <li>{@link PersistentEntitySectionManager#addEntityUuid} — {@code "UUID of added entity already exists: {}"}</li>
 * </ul>
 * Logs the first hit per UUID each minute; suppressed hits are summarized every 10s.
 * Soft {@code require = 0}; non-UUID logger calls pass through unchanged.
 * <p>
 * Uses {@code method = "*"} so multi-target AP does not require a shared named method on both classes.
 */
@Mixin(value = {
        EntityLookup.class,
        PersistentEntitySectionManager.class
}, priority = 900)
public abstract class UuidDuplicateLogMixin {

    @Unique
    private static final long omnifix$PER_UUID_WINDOW_MS = 60_000L;

    @Unique
    private static final long omnifix$SUMMARY_INTERVAL_MS = 10_000L;

    @Unique
    private static final int omnifix$MAP_PRUNE_THRESHOLD = 4096;

    @Unique
    private static final UUID omnifix$GLOBAL_KEY = new UUID(0L, 0L);

    @Unique
    private static final Map<UUID, Long> omnifix$lastFullLogMs = new ConcurrentHashMap<>();

    @Unique
    private static final AtomicInteger omnifix$suppressedSinceSummary = new AtomicInteger();

    @Unique
    private static final AtomicLong omnifix$lastSummaryMs = new AtomicLong(0L);

    /**
     * EntityLookup.add → Logger.warn(String, Object, Object) for "Duplicate entity UUID {}: {}".
     * Format filter keeps this narrow on multi-target method="*".
     */
    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            ),
            require = 0
    )
    private void omnifix$throttleTwoArgUuidWarn(
            Logger logger, String format, Object arg1, Object arg2) {
        if (!omnifix$isUuidDuplicateFormat(format)) {
            logger.warn(format, arg1, arg2);
            return;
        }
        UUID uuid = omnifix$resolveUuid(arg1, arg2);
        if (omnifix$allowFullLog(uuid)) {
            logger.warn(format, arg1, arg2);
        } else {
            omnifix$noteSuppressed(logger);
        }
    }

    /**
     * PersistentEntitySectionManager.addEntityUuid → Logger.warn(String, Object)
     * for "UUID of added entity already exists: {}".
     */
    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
                    remap = false
            ),
            require = 0
    )
    private void omnifix$throttleOneArgUuidWarn(Logger logger, String format, Object arg1) {
        if (!omnifix$isUuidDuplicateFormat(format)) {
            logger.warn(format, arg1);
            return;
        }
        UUID uuid = omnifix$resolveUuid(arg1, null);
        if (omnifix$allowFullLog(uuid)) {
            logger.warn(format, arg1);
        } else {
            omnifix$noteSuppressed(logger);
        }
    }

    @Unique
    private static boolean omnifix$isUuidDuplicateFormat(String format) {
        if (format == null) {
            return false;
        }
        return format.indexOf("UUID") >= 0
                && (format.indexOf("already exists") >= 0 || format.indexOf("Duplicate entity") >= 0);
    }

    @Unique
    private static UUID omnifix$resolveUuid(Object primary, Object secondary) {
        UUID fromPrimary = omnifix$asUuid(primary);
        if (fromPrimary != null) {
            return fromPrimary;
        }
        UUID fromSecondary = omnifix$asUuid(secondary);
        if (fromSecondary != null) {
            return fromSecondary;
        }
        return omnifix$GLOBAL_KEY;
    }

    @Unique
    private static UUID omnifix$asUuid(Object arg) {
        if (arg instanceof UUID uuid) {
            return uuid;
        }
        if (arg instanceof EntityAccess access) {
            return access.getUUID();
        }
        return null;
    }

    @Unique
    private static boolean omnifix$allowFullLog(UUID uuid) {
        long now = System.currentTimeMillis();
        Long previous = omnifix$lastFullLogMs.get(uuid);
        if (previous == null || now - previous >= omnifix$PER_UUID_WINDOW_MS) {
            omnifix$lastFullLogMs.put(uuid, now);
            if (omnifix$lastFullLogMs.size() > omnifix$MAP_PRUNE_THRESHOLD) {
                omnifix$pruneStale(now);
            }
            return true;
        }
        return false;
    }

    @Unique
    private static void omnifix$noteSuppressed(Logger logger) {
        omnifix$suppressedSinceSummary.incrementAndGet();
        long now = System.currentTimeMillis();
        long last = omnifix$lastSummaryMs.get();
        if (now - last < omnifix$SUMMARY_INTERVAL_MS) {
            return;
        }
        if (!omnifix$lastSummaryMs.compareAndSet(last, now)) {
            return;
        }
        int count = omnifix$suppressedSinceSummary.getAndSet(0);
        if (count > 0) {
            logger.warn(
                    "[OmniFix] Suppressed {} duplicate-entity UUID warning(s) over ~{}s (rate-limited; first per UUID still logged each minute)",
                    count,
                    omnifix$SUMMARY_INTERVAL_MS / 1000L
            );
        }
    }

    @Unique
    private static void omnifix$pruneStale(long now) {
        Iterator<Map.Entry<UUID, Long>> it = omnifix$lastFullLogMs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> e = it.next();
            Long ts = e.getValue();
            if (ts == null || now - ts >= omnifix$PER_UUID_WINDOW_MS) {
                it.remove();
            }
        }
    }
}
