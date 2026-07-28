package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * MC-577 — Mouse buttons bound to inventory controls (close inventory, drop) do not work inside
 * container screens.
 *
 * <p>Root cause: {@link AbstractContainerScreen#mouseClicked} only fully handles buttons 0/1 (plus
 * pick-block). {@code checkHotbarMouseClicked} covers remapped hotbar/offhand, but inventory-close
 * and drop never consult {@code KeyMapping#matchesMouse}. Soft {@code require = 0} on the drop
 * inject because local capture is sensitive to inventory-screen rewrites from other mods.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MouseInventoryMixin extends Screen {

    @Shadow
    protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType);

    protected MouseInventoryMixin(Component title) {
        super(title);
    }

    /**
     * If the parent {@link Screen#mouseClicked} did not consume the click, still close the screen
     * when the inventory key is a mouse binding.
     */
    @ModifyExpressionValue(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"
            ),
            require = 0
    )
    private boolean omnifix$mouseInventoryClose(boolean parentHandled, double mouseX, double mouseY, int button) {
        return parentHandled || this.omnifix$tryCloseWithMouse(button);
    }

    @Inject(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/Util;getMillis()J"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true,
            require = 0
    )
    private void omnifix$dropWithMouse(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir,
            boolean isPickItem,
            Slot hoveredSlot
    ) {
        if (this.minecraft == null || this.minecraft.options == null) {
            return;
        }
        if (!this.minecraft.options.keyDrop.matchesMouse(button)) {
            return;
        }
        if (hoveredSlot == null) {
            return;
        }
        this.slotClicked(hoveredSlot, hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
        cir.setReturnValue(true);
    }

    @Unique
    private boolean omnifix$tryCloseWithMouse(int button) {
        if (this.minecraft != null
                && this.minecraft.options != null
                && this.minecraft.options.keyInventory.matchesMouse(button)) {
            this.onClose();
            return true;
        }
        return false;
    }
}
