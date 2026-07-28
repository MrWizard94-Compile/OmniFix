package org.omnifix.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public final class IngredientItemStacksSoftReference extends SoftReference<ItemStack[]> {

    private final Ingredient ingredient;
    private static final ReferenceQueue<ItemStack[]> QUEUE = new ReferenceQueue<>();

    public IngredientItemStacksSoftReference(Ingredient ingredient, ItemStack[] stacks) {
        super(stacks, QUEUE);
        this.ingredient = ingredient;
    }

    public static void clearReferences() {
        Reference<? extends ItemStack[]> ref;
        while ((ref = QUEUE.poll()) != null) {
            if (ref instanceof IngredientItemStacksSoftReference ingRef
                    && ingRef.ingredient instanceof ExtendedIngredient extIng) {
                extIng.omnifix$clearReference();
            }
        }
    }
}
