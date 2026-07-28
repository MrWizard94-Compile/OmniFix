package org.omnifix.mixin.perf;

import net.minecraft.world.item.crafting.Ingredient;
import org.omnifix.recipe.IngredientValueDeduplicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.stream.Stream;

/**
 * Deduplicate Ingredient.ItemValue instances as ingredients are constructed.
 */
@Mixin(Ingredient.class)
public abstract class IngredientDedupMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Stream<? extends Ingredient.Value> omnifix$dedupeValues(
            Stream<? extends Ingredient.Value> stream
    ) {
        return stream.map(IngredientValueDeduplicator::deduplicate);
    }
}
