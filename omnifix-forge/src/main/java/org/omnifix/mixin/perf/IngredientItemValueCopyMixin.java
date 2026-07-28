package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Defensive-copy stacks from interned ItemValue so mods that mutate getItems() results cannot
 * corrupt the shared template.
 */
@Mixin(Ingredient.ItemValue.class)
public abstract class IngredientItemValueCopyMixin {

    @ModifyExpressionValue(
            method = "getItems",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/item/crafting/Ingredient$ItemValue;item:Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack omnifix$defensiveCopy(ItemStack original) {
        return original.copy();
    }
}
