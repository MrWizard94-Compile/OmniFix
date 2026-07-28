package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ambient.Bat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.time.LocalDate;

/**
 * Bat Halloween check calls {@link LocalDate#now()} every evaluation — cache for 30s.
 */
@Mixin(value = Bat.class, priority = 1200)
public abstract class BatHalloweenDateMixin {

    @Unique
    private static long omnifix$lastQueryMs = -1L;

    @Unique
    private static LocalDate omnifix$lastDate;

    @Redirect(
            method = "isHalloween",
            at = @At(value = "INVOKE", target = "Ljava/time/LocalDate;now()Ljava/time/LocalDate;"),
            require = 0
    )
    private static LocalDate omnifix$cachedDate() {
        LocalDate date = omnifix$lastDate;
        long now = System.currentTimeMillis();
        if (date == null || Math.abs(now - omnifix$lastQueryMs) > 30_000L) {
            omnifix$lastDate = date = LocalDate.now();
            omnifix$lastQueryMs = now;
        }
        return date;
    }
}
