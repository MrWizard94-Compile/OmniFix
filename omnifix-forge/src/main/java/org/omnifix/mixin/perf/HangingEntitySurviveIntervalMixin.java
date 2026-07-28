package org.omnifix.mixin.perf;

import net.minecraft.world.entity.decoration.HangingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: item frames and paintings call {@code survives()} (full {@code noCollision} scan)
 * every 100 ticks on the server. Raising the interval to 150 cuts collision queries ~33% with
 * negligible delay detecting broken support blocks.
 */
@Mixin(HangingEntity.class)
public abstract class HangingEntitySurviveIntervalMixin {

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 100))
    private int omnifix$longerSurviveInterval(int original) {
        return 150;
    }
}
