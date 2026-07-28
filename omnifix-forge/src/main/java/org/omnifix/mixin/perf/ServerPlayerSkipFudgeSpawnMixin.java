package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSkipFudgeSpawnMixin {

    @WrapWithCondition(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;fudgeSpawnLocation(Lnet/minecraft/server/level/ServerLevel;)V"
            )
    )
    private boolean omnifix$skipFudgingForSPOwner(ServerPlayer player, ServerLevel targetLevel) {
        return targetLevel.getServer().getWorldData().getLoadedPlayerTag() == null
                || !targetLevel.getServer().isSingleplayerOwner(player.getGameProfile());
    }
}
