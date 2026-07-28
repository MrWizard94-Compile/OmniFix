package org.omnifix.mixin.perf;

import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: while a container is open, {@code scheduleRecheck} queues a block tick every 5 ticks
 * that rescans nearby players for opener validity. Stretching to 8 reduces player AABB scans during
 * long open sessions (AE2-style automation nearby is unaffected — open count still updates on
 * open/close).
 *
 * <p>Trade-off: abandoned open containers may close ~3 ticks later.
 */
@Mixin(ContainerOpenersCounter.class)
public abstract class ContainerOpenersRecheckMixin {

    @ModifyConstant(method = "scheduleRecheck", constant = @Constant(intValue = 5))
    private static int omnifix$longerRecheck(int original) {
        return 8;
    }
}
