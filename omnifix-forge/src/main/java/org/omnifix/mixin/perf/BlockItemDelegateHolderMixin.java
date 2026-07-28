package org.omnifix.mixin.perf;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.omnifix.registry.DelegateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Block.class, Item.class})
public abstract class BlockItemDelegateHolderMixin<T> implements DelegateHolder<T> {

    @Unique
    private Holder.Reference<T> omnifix$delegate;

    @Unique
    private ResourceKey<Registry<T>> omnifix$key;

    @Override
    public Holder.Reference<T> omnifix$getDelegate(ResourceKey<Registry<T>> registryKey) {
        return omnifix$key == registryKey ? omnifix$delegate : null;
    }

    @Override
    public void omnifix$setDelegate(ResourceKey<Registry<T>> registryKey, Holder.Reference<T> holder) {
        this.omnifix$delegate = holder;
        this.omnifix$key = registryKey;
    }
}
