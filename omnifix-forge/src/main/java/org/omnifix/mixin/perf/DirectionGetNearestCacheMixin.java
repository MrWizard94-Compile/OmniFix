package org.omnifix.mixin.perf;

import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Root cause: {@link Direction#getNearest(double, double, double)} recomputes the best axis on
 * every call. Look/place/piston paths often re-query the same exact vector within a tick.
 *
 * <p>Caches only on exact double-bit equality (correctness-first; no quantization).
 */
@Mixin(Direction.class)
public abstract class DirectionGetNearestCacheMixin {

    @Unique
    private static final int OMNIFIX$CACHE_CAP = 2048;

    @Unique
    private static final java.util.concurrent.ConcurrentHashMap<Long, Direction> OMNIFIX$CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Inject(method = "getNearest(DDD)Lnet/minecraft/core/Direction;", at = @At("HEAD"), cancellable = true)
    private static void omnifix$cachedGetNearest(
            double x, double y, double z, CallbackInfoReturnable<Direction> cir) {
        Direction cached = OMNIFIX$CACHE.get(omnifix$key(x, y, z));
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getNearest(DDD)Lnet/minecraft/core/Direction;", at = @At("RETURN"))
    private static void omnifix$rememberGetNearest(
            double x, double y, double z, CallbackInfoReturnable<Direction> cir) {
        Direction result = cir.getReturnValue();
        if (result == null) {
            return;
        }
        if (OMNIFIX$CACHE.size() < OMNIFIX$CACHE_CAP) {
            OMNIFIX$CACHE.putIfAbsent(omnifix$key(x, y, z), result);
        }
    }

    @Unique
    private static long omnifix$key(double x, double y, double z) {
        long h = Double.doubleToLongBits(x);
        h = 31L * h + Double.doubleToLongBits(y);
        h = 31L * h + Double.doubleToLongBits(z);
        // spread bits
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        return h;
    }
}
