package org.omnifix.recipe;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Interns identical {@link Ingredient.ItemValue} templates (same item type/tag/count) so large
 * recipe graphs do not retain millions of duplicate value wrappers.
 */
public final class IngredientValueDeduplicator {

    private static final ObjectOpenCustomHashSet<Ingredient.ItemValue> VALUES =
            new ObjectOpenCustomHashSet<>(new Hash.Strategy<>() {
                @Override
                public int hashCode(Ingredient.ItemValue o) {
                    return o == null ? 0 : ItemStackLinkedSet.TYPE_AND_TAG.hashCode(o.item);
                }

                @Override
                public boolean equals(Ingredient.ItemValue a, Ingredient.ItemValue b) {
                    return a == b
                            || (a != null
                            && b != null
                            && ItemStackLinkedSet.TYPE_AND_TAG.equals(a.item, b.item)
                            && a.item.getCount() == b.item.getCount());
                }
            });

    private IngredientValueDeduplicator() {}

    public static Ingredient.Value deduplicate(Ingredient.Value value) {
        if (value.getClass() == Ingredient.ItemValue.class) {
            synchronized (VALUES) {
                return VALUES.addOrGet((Ingredient.ItemValue) value);
            }
        }
        return value;
    }
}
