package org.omnifix.mixin.perf;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.omnifix.load.MinecraftServerReloadTracker;
import org.omnifix.recipe.ExtendedIngredient;
import org.omnifix.recipe.IngredientItemStacksSoftReference;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

/**
 * Faster tag ingredient test/stacking, soft-cached getItems expansion.
 */
@Mixin(value = Ingredient.class, priority = 700)
public abstract class IngredientFasterMixin implements ExtendedIngredient {

    @Shadow
    public abstract boolean isVanilla();

    @Shadow
    @Final
    private Ingredient.Value[] values;

    @Shadow
    private @Nullable IntList stackingIds;

    @Shadow
    @Nullable
    private ItemStack[] itemStacks;

    @Unique
    private volatile IngredientItemStacksSoftReference omnifix$cachedItemStacks;

    @Unique
    private boolean omnifix$areTagsAvailable() {
        return !MinecraftServerReloadTracker.isReloadActive();
    }

    @Inject(
            method = "test(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/Ingredient;getItems()[Lnet/minecraft/world/item/ItemStack;"
            ),
            cancellable = true
    )
    private void omnifix$fasterTagIngredientTest(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.isVanilla()
                && this.values.length == 1
                && this.values[0] instanceof Ingredient.TagValue tagValue
                && omnifix$areTagsAvailable()) {
            cir.setReturnValue(stack.getItemHolder().is(tagValue.tag));
        }
    }

    @Override
    public boolean omnifix$hasNoElements() {
        return !omnifix$containsItems();
    }

    @Unique
    private boolean omnifix$isEmptyTagStack(ItemStack item) {
        return item.getItem() == Items.BARRIER
                && item.getHoverName() instanceof MutableComponent hoverName
                && hoverName.getString().startsWith("Empty Tag: ");
    }

    @Unique
    private boolean omnifix$containsItems() {
        for (Ingredient.Value value : this.values) {
            if (value instanceof Ingredient.ItemValue) {
                return true;
            } else if (value instanceof Ingredient.TagValue tagValue && omnifix$areTagsAvailable()) {
                var holderSetOpt = BuiltInRegistries.ITEM.getTag(tagValue.tag);
                if (holderSetOpt.isPresent() && holderSetOpt.get().size() > 0) {
                    return true;
                }
            } else {
                var items = value.getItems();
                if (items.isEmpty() || omnifix$isEmptyTagStack(items.iterator().next())) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    @Inject(
            method = "getStackingIds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/Ingredient;getItems()[Lnet/minecraft/world/item/ItemStack;"
            ),
            cancellable = true
    )
    private void omnifix$fasterTagIngredientStacking(CallbackInfoReturnable<IntList> cir) {
        if (this.isVanilla()
                && this.values.length == 1
                && this.values[0] instanceof Ingredient.TagValue tagValue
                && omnifix$areTagsAvailable()) {
            var tag = BuiltInRegistries.ITEM.getTag(tagValue.tag);
            if (tag.isEmpty() || tag.get().size() == 0) {
                return;
            }
            var list = new IntArrayList(tag.get().stream().mapToInt(h -> BuiltInRegistries.ITEM.getId(h.value())).toArray());
            list.sort(IntComparators.NATURAL_COMPARATOR);
            this.stackingIds = list;
            cir.setReturnValue(list);
        }
    }

    /**
     * @author OmniFix
     * @reason soft-reference item expansion so GC can reclaim under pressure
     */
    @Overwrite
    public ItemStack[] getItems() {
        if (this.itemStacks != null) {
            return this.itemStacks;
        }
        var cache = this.omnifix$cachedItemStacks;
        if (cache != null) {
            var stacks = cache.get();
            if (stacks != null) {
                return stacks;
            }
        }
        IngredientItemStacksSoftReference.clearReferences();
        ItemStack[] result = omnifix$computeItemsArray();
        this.omnifix$cachedItemStacks = new IngredientItemStacksSoftReference((Ingredient) (Object) this, result);
        return result;
    }

    @Unique
    private ItemStack[] omnifix$computeItemsArray() {
        if (this.values.length == 1) {
            if (this.values[0] instanceof Ingredient.TagValue tagValue && omnifix$areTagsAvailable()) {
                var tag = BuiltInRegistries.ITEM.getTag(tagValue.tag);
                if (tag.isPresent() && tag.get().size() > 0) {
                    var holderSet = tag.get();
                    ItemStack[] result = new ItemStack[holderSet.size()];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = new ItemStack(holderSet.get(i));
                    }
                    return result;
                }
            }
        }
        ArrayList<ItemStack> itemList = new ArrayList<>(2);
        for (var value : this.values) {
            var collection = value.getItems();
            itemList.ensureCapacity(collection.size() + itemList.size());
            for (var item : collection) {
                itemList.add(item);
            }
        }
        return itemList.toArray(ItemStack[]::new);
    }

    @Override
    public void omnifix$clearReference() {
        this.omnifix$cachedItemStacks = null;
    }

    @Inject(method = "invalidate", at = @At("RETURN"), remap = false)
    private void omnifix$invalidateSoftReference(CallbackInfo ci) {
        omnifix$clearReference();
    }
}
