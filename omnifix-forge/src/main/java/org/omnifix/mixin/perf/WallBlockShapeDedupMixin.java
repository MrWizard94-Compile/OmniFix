package org.omnifix.mixin.perf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Vanilla wall blocks share default shape dimensions — cache property→shape maps and rebuild
 * per-block BlockState maps without reconstructing VoxelShapes.
 */
@Mixin(WallBlock.class)
public abstract class WallBlockShapeDedupMixin extends Block {

    private static final Map<ImmutableList<Float>,
            Pair<Map<ImmutableMap<Property<?>, Comparable<?>>, VoxelShape>, StateDefinition<Block, BlockState>>>
            OMNIFIX$CACHE = new HashMap<>();

    public WallBlockShapeDedupMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "makeShapes", at = @At("HEAD"), cancellable = true)
    private synchronized void omnifix$useCached(
            float f1, float f2, float f3, float f4, float f5, float f6,
            CallbackInfoReturnable<Map<BlockState, VoxelShape>> cir
    ) {
        ImmutableList<Float> key = ImmutableList.of(f1, f2, f3, f4, f5, f6);
        var cache = OMNIFIX$CACHE.get(key);
        if (cache == null || !cache.getSecond().getProperties().equals(this.stateDefinition.getProperties())) {
            return;
        }
        ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();
        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            VoxelShape shape = cache.getFirst().get(state.getValues());
            if (shape == null) {
                return;
            }
            builder.put(state, shape);
        }
        cir.setReturnValue(builder.build());
    }

    @Inject(method = "makeShapes", at = @At("RETURN"))
    private synchronized void omnifix$storeCache(
            float f1, float f2, float f3, float f4, float f5, float f6,
            CallbackInfoReturnable<Map<BlockState, VoxelShape>> cir
    ) {
        if ((Class<?>) this.getClass() != WallBlock.class) {
            return;
        }
        ImmutableList<Float> key = ImmutableList.of(f1, f2, f3, f4, f5, f6);
        if (OMNIFIX$CACHE.containsKey(key)) {
            return;
        }
        Map<ImmutableMap<Property<?>, Comparable<?>>, VoxelShape> byProps = new HashMap<>();
        for (Map.Entry<BlockState, VoxelShape> e : cir.getReturnValue().entrySet()) {
            byProps.put(e.getKey().getValues(), e.getValue());
        }
        OMNIFIX$CACHE.put(key, Pair.of(byProps, this.stateDefinition));
    }
}
