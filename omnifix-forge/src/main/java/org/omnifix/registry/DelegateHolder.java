package org.omnifix.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/** Fast-path storage for ForgeRegistry delegates on Block/Item instances. */
public interface DelegateHolder<T> {
    Holder.Reference<T> omnifix$getDelegate(ResourceKey<Registry<T>> registryKey);

    void omnifix$setDelegate(ResourceKey<Registry<T>> registryKey, Holder.Reference<T> holder);
}
