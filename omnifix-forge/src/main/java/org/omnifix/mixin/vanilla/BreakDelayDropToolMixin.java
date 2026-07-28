package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-165381 — Dropping/throwing the held tool while mining delays the next block break.
 *
 * <p>Root cause: mid-mine stack change leaves {@code MultiPlayerGameMode} destroy state
 * ({@code destroyingItem}/{@code destroyDelay}) inconsistent, so the next destroy starts with the
 * multiplayer break cooldown. Explicitly stopping destroy after the selected stack is removed
 * clears that stale state.
 */
@Mixin(LocalPlayer.class)
public abstract class BreakDelayDropToolMixin {

    @Shadow @Final protected Minecraft minecraft;

    @Inject(
            method = "drop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;",
                    shift = At.Shift.AFTER
            )
    )
    private void omnifix$stopDestroyAfterDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.gameMode != null) {
            this.minecraft.gameMode.stopDestroyBlock();
        }
    }
}
