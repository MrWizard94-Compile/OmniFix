package org.omnifix.mixin.vanilla;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

/**
 * MC-80859 — Starting a stack drag over a compatible stack makes that stack invisible until its
 * count changes.
 *
 * <p>Root cause: {@code AbstractContainerScreen#renderSlot} early-returns when the slot is in
 * {@code quickCraftSlots} and {@code quickCraftSlots.size() == 1}, skipping the normal item draw.
 * With only one drag target the multi-slot quick-craft preview is meaningless, so treat that case
 * as non-drag and fall through to ordinary slot rendering.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class DragStackInvisibleMixin {

    /**
     * When the drag set has a single slot, pretend it is not a drag member so the under-stack
     * keeps rendering. Multi-slot drags keep vanilla {@code contains} behaviour.
     */
    @Redirect(
            method = "renderSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
            )
    )
    private boolean omnifix$keepSingleDragSlotVisible(Set<Slot> quickCraftSlots, Object slot) {
        if (quickCraftSlots.size() == 1) {
            return false;
        }
        //noinspection SuspiciousMethodCalls — vanilla passes the Slot being rendered
        return quickCraftSlots.contains(slot);
    }
}
