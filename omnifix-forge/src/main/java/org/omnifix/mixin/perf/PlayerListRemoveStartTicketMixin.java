package org.omnifix.mixin.perf;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Unit;
import org.omnifix.duck.ISpawnTrackingMinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListRemoveStartTicketMixin {

    @Inject(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addNewPlayer(Lnet/minecraft/server/level/ServerPlayer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void omnifix$removeStartTicket(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        var initial = ((ISpawnTrackingMinecraftServer) player.server).omnifix$getInitialStartTicketLocation();
        if (initial != null) {
            var level = player.server.getLevel(initial.getLeft());
            if (level != null) {
                level.getChunkSource().removeRegionTicket(TicketType.START, initial.getRight(), 0, Unit.INSTANCE);
            }
        }
    }
}
