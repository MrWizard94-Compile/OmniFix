package org.omnifix.mixin.perf;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeHooks;
import org.omnifix.recipe.ExtendedIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, priority = 900)
public abstract class ForgeHooksHasNoElementsMixin {

    @Inject(method = "hasNoElements", at = @At("HEAD"), cancellable = true, remap = false)
    private static void omnifix$fastHasNoElements(Ingredient ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (ingredient.isVanilla()) {
            cir.setReturnValue(((ExtendedIngredient) ingredient).omnifix$hasNoElements());
        }
    }
}
