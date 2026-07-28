package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Root cause: {@link ExperienceOrb#tick} gates {@code scanForEntities} (player follow + merge)
 * on {@code tickCount % 20 == 1}. Dense XP dumps re-scan the neighbourhood every second per orb.
 *
 * <p>Stretching the period to 30 cuts scan frequency ~33% with only a slight delay in orb
 * attraction and merge under dumps.
 *
 * <p>Trade-off: orbs may take up to ~0.5s longer to lock onto a player or merge after spawn.
 */
@Mixin(ExperienceOrb.class)
public abstract class XpOrbScanPeriodMixin {

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
    private int omnifix$longerScanPeriod(int original) {
        return 30;
    }
}
