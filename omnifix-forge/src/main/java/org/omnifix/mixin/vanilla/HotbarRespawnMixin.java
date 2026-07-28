package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-143474 — Respawning (or dimension-change via respawn packet) resets the hotbar selection to
 * slot 0.
 *
 * <p>Root cause: {@link ClientPacketListener#handleRespawn} constructs a new {@link LocalPlayer}
 * via {@code MultiPlayerGameMode#createPlayer} without copying {@code Inventory.selected}. The
 * server keeps the slot through {@code restoreFrom}/{@code replaceWith} but does not re-send
 * {@code ClientboundSetCarriedItemPacket} on death respawn, so the client stays on slot 0 until
 * the player scrolls.
 */
@Mixin(ClientPacketListener.class)
public abstract class HotbarRespawnMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique
    private int omnifix$savedHotbarSlot;

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void omnifix$saveHotbarOnRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        LocalPlayer old = this.minecraft.player;
        this.omnifix$savedHotbarSlot = old != null ? old.getInventory().selected : 0;
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void omnifix$restoreHotbarOnRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        LocalPlayer neu = this.minecraft.player;
        if (neu != null) {
            neu.getInventory().selected = this.omnifix$savedHotbarSlot;
        }
    }
}
