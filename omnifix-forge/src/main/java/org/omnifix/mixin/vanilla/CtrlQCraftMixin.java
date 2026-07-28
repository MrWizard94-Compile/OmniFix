package org.omnifix.mixin.vanilla;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-135971 — Ctrl+Q (throw entire stack) on a crafting result only drops the currently displayed
 * result once, instead of crafting-and-dropping until ingredients run out.
 *
 * <p>Root cause: {@link AbstractContainerMenu#doClick} THROW with button 1 takes the full count of
 * the result stack in one {@code safeTake}, but crafting tables immediately refill a single craft
 * into the result slot afterward only on the next interaction. Loop THROW button 0 on
 * {@link ResultSlot}s while the same item reappears so Ctrl+Q drains the recipe, matching inventory
 * Ctrl+Q behaviour. Soft-capped to avoid pathological infinite loops from broken menus.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class CtrlQCraftMixin {

    /** Safety cap: max single-item throws per Ctrl+Q on a result slot. */
    @Unique
    private static final int OMNIFIX$MAX_CTRL_Q_CRAFTS = 4096;

    @Shadow
    protected abstract void doClick(int slotId, int button, ClickType clickType, Player player);

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void omnifix$ctrlQCraftingResult(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (clickType != ClickType.THROW || button != 1 || slotIndex < 0) {
            return;
        }
        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
        if (!self.getCarried().isEmpty()) {
            return;
        }
        if (slotIndex >= self.slots.size()) {
            return;
        }
        Slot slot = self.slots.get(slotIndex);
        // Only result slots regenerate after a throw; normal stacks already dump via vanilla j1=count.
        if (!(slot instanceof ResultSlot) || !slot.hasItem() || !slot.mayPickup(player)) {
            return;
        }
        Item item = slot.getItem().getItem();
        int guard = 0;
        while (slot.hasItem() && slot.getItem().is(item) && guard++ < OMNIFIX$MAX_CTRL_Q_CRAFTS) {
            // button 0 = throw one crafted unit; refill happens inside ResultSlot.onTake.
            this.doClick(slotIndex, 0, ClickType.THROW, player);
        }
        ci.cancel();
    }
}
