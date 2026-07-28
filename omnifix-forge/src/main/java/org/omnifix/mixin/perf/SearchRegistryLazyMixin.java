package org.omnifix.mixin.perf;

import net.minecraft.client.searchtree.SearchRegistry;
import org.omnifix.searchtree.LazySearchTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SearchRegistry.class)
public abstract class SearchRegistryLazyMixin {

    @ModifyVariable(method = "register", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private <T> SearchRegistry.TreeBuilderSupplier<T> omnifix$useLazyBuilder(
            SearchRegistry.TreeBuilderSupplier<T> supplier
    ) {
        return LazySearchTree.decorate(supplier);
    }
}
