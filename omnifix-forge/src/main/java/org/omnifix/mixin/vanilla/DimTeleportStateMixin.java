package org.omnifix.mixin.vanilla;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-124177 — Teleporting to another dimension loses some client states (effects, food HUD, abilities).
 *
 * <p>Root cause: {@link ServerPlayer#teleportTo(ServerLevel, double, double, double, float, float)}
 * sends a respawn packet and {@code sendAllPlayerInfo}, but unlike portal
 * {@code changeDimension} it does not re-broadcast active effects, health/food/saturation, or
 * abilities. The client clears those on respawn and never gets them back until relog. Resend
 * after {@code sendAllPlayerInfo}.
 */
@Mixin(ServerPlayer.class)
public abstract class DimTeleportStateMixin {

    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void omnifix$resyncClientStateAfterDimTeleport(
            ServerLevel level,
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            CallbackInfo ci
    ) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        for (MobEffectInstance effect : self.getActiveEffects()) {
            self.connection.send(new ClientboundUpdateMobEffectPacket(self.getId(), effect));
        }
        FoodData food = self.getFoodData();
        self.connection.send(new ClientboundSetHealthPacket(
                self.getHealth(),
                food.getFoodLevel(),
                food.getSaturationLevel()
        ));
        self.connection.send(new ClientboundPlayerAbilitiesPacket(self.getAbilities()));
    }
}
