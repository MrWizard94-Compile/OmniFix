package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-88371 — Ender Dragon flies into the void when the exit portal / podium is destroyed.
 *
 * <p>Root cause: {@link DragonLandingPhase#doServerTick} sets the landing target from
 * {@code level.getHeightmapPos(MOTION_BLOCKING_NO_LEAVES, podium)}. With no solid blocks under
 * the podium, the heightmap returns Y=0 and the dragon dives into the void. When the sample is
 * void-level, pin the landing Y to the standard End podium height (65).
 */
@Mixin(DragonLandingPhase.class)
public abstract class DragonVoidPortalMixin {

    private static final int END_PODIUM_FALLBACK_Y = 65;

    @ModifyExpressionValue(
            method = "doServerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos omnifix$fallbackLandingWhenPodiumMissing(BlockPos heightmapPos) {
        if (heightmapPos.getY() == 0) {
            return new BlockPos(heightmapPos.getX(), END_PODIUM_FALLBACK_Y, heightmapPos.getZ());
        }
        return heightmapPos;
    }
}
