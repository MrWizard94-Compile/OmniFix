package org.omnifix.compat.createportals.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.IPGlobal;

/**
 * When Create dismounts a player onto the far-side dimensional carriage, vanilla
 * {@code ServerPlayer.teleportTo} bypasses Immersive Portals' position/collision bookkeeping.
 * Route through {@code ServerTeleportationManager.teleportPlayer} so IP client/server state stays consistent.
 *
 * <p>Targets the synthetic inner class {@code Carriage$DimensionalCarriageEntity#dismountPlayer}.
 */
@Mixin(targets = "com.simibubi.create.content.trains.entity.Carriage$DimensionalCarriageEntity")
public abstract class MixinDimensionalCarriageDismount {

    @Redirect(
            method = "dismountPlayer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/Integer;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"
            ),
            require = 0,
            remap = false
    )
    private void omnifix$ipAwarePlayerTeleport(
            ServerPlayer player,
            ServerLevel level,
            double x,
            double y,
            double z,
            float yRot,
            float xRot
    ) {
        ResourceKey<Level> dim = level.dimension();
        // Eye-ish position; IP manager adjusts bounding box after set.
        IPGlobal.serverTeleportationManager.teleportPlayer(player, dim, new Vec3(x, y, z));
        player.setYRot(yRot);
        player.setXRot(xRot);
    }
}
