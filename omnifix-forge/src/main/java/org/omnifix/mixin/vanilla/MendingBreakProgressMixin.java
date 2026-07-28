package org.omnifix.mixin.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-176559 — Mending (or any repair that rewrites Damage NBT / replaces the ItemStack instance)
 * used to reset client block-break progress mid-mine.
 *
 * <p>Forge already implements the primary fix in {@code IForgeItem#shouldCauseBlockBreakReset}
 * (ignores {@code Damage} tag). This mixin keeps {@code destroyingItem} synchronized with the live
 * main-hand stack when only non-resetting changes occur, so progress is never restarted by a stale
 * snapshot after inventory sync packets replace the stack instance.
 *
 * <p>Server destroy timing is tick-based and unaffected by hand-item NBT.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MendingBreakProgressMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private BlockPos destroyBlockPos;
    @Shadow private ItemStack destroyingItem;
    @Shadow private boolean isDestroying;

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void omnifix$syncDestroyingItemWhenOnlyDamageChanged(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isDestroying || this.minecraft.player == null) {
            return;
        }
        if (!pos.equals(this.destroyBlockPos)) {
            return;
        }

        ItemStack held = this.minecraft.player.getMainHandItem();
        ItemStack previous = this.destroyingItem;
        if (previous.isEmpty() || held.isEmpty()) {
            return;
        }
        // Forge's shouldCauseBlockBreakReset returns false when only Damage (or equal tags) differ.
        if (!previous.shouldCauseBlockBreakReset(held)) {
            // Refresh the snapshot so later equality checks stay stable after inventory sync.
            this.destroyingItem = held;
        }
    }
}
