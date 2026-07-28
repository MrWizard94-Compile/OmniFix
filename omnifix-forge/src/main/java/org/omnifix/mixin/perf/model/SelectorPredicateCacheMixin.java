package org.omnifix.mixin.perf.model;

import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Multipart model selectors rebuild predicates per StateDefinition lookup — cache them.
 */
@Mixin(Selector.class)
public abstract class SelectorPredicateCacheMixin {

    @Unique
    private final ConcurrentHashMap<StateDefinition<Block, BlockState>, Predicate<BlockState>>
            omnifix$predicateCache = new ConcurrentHashMap<>();

    @Inject(method = "getPredicate", at = @At("HEAD"), cancellable = true)
    private void omnifix$useCached(
            StateDefinition<Block, BlockState> definition,
            CallbackInfoReturnable<Predicate<BlockState>> cir
    ) {
        Predicate<BlockState> cached = omnifix$predicateCache.get(definition);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(method = "getPredicate", at = @At("RETURN"))
    private void omnifix$storeCached(
            StateDefinition<Block, BlockState> definition,
            CallbackInfoReturnable<Predicate<BlockState>> cir
    ) {
        omnifix$predicateCache.put(definition, cir.getReturnValue());
    }
}
