package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.sensing.Sensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: brain {@link Sensor}s run {@code doTick} every {@code scanRate} ticks (often 20).
 * Raising scan rates modestly cuts perception CPU on large farms of villagers/piglin/etc.
 *
 * <p>Trade-off: sensors react up to ~50% slower (capped at 80 ticks). Running goals and pathing
 * are unaffected.
 */
@Mixin(Sensor.class)
public abstract class SensorScanRateMixin {

    private static final int OMNIFIX$MAX_SCAN_RATE = 80;

    @ModifyVariable(method = "<init>(I)V", at = @At("HEAD"), argsOnly = true)
    private static int omnifix$increaseScanRate(int scanRate) {
        if (scanRate <= 0) {
            return scanRate;
        }
        // +50%, cap so very slow sensors do not become pathological.
        long bumped = scanRate + (scanRate / 2L);
        return (int) Math.min(OMNIFIX$MAX_SCAN_RATE, bumped);
    }
}
