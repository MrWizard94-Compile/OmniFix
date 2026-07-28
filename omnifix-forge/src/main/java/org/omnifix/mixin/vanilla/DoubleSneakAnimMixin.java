package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * MC-159163 — Quickly tapping sneak plays the crouch eye-height animation twice on laggy clients.
 *
 * <p>Root cause: the client already drives crouch from local input ({@code LocalPlayer#isShiftKeyDown}
 * reads {@code Input.shiftKeyDown}). The server still broadcasts {@code DATA_POSE} updates that
 * re-apply the same pose transition via {@link SynchedEntityData#assignValues}, causing a second
 * crouch bob. Drop pose entries from entity-data packets for the local player only.
 */
@Mixin(ClientPacketListener.class)
public abstract class DoubleSneakAnimMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "handleSetEntityData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void omnifix$stripLocalPlayerPose(
            ClientboundSetEntityDataPacket packet,
            CallbackInfo ci,
            Entity entity
    ) {
        if (entity == null || this.minecraft == null || entity != this.minecraft.player) {
            return;
        }
        List<SynchedEntityData.DataValue<?>> items = packet.packedItems();
        if (items == null || items.isEmpty()) {
            return;
        }
        try {
            items.removeIf(value -> value != null && value.serializer() == EntityDataSerializers.POSE);
        } catch (UnsupportedOperationException ignored) {
            // Immutable packed list (unexpected on 1.20.1 network path) — soft fail.
        }
    }
}
