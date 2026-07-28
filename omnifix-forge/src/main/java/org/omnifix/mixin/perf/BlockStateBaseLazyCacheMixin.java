package org.omnifix.mixin.perf;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.objectweb.asm.Opcodes;
import org.omnifix.duck.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Defer BlockState cache rebuilds: mark invalid on bake/registry rebuild, regenerate on first access.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseLazyCacheMixin extends StateHolder<Block, BlockState> implements IBlockState {

    @Unique
    private static final FluidState OMNIFIX$VANILLA_DEFAULT_FLUID = Fluids.EMPTY.defaultFluidState();

    @Shadow
    public abstract void initCache();

    @Shadow
    private BlockBehaviour.BlockStateBase.Cache cache;

    @Shadow
    private FluidState fluidState;

    @Shadow
    private boolean isRandomlyTicking;

    @Shadow
    @Deprecated
    private boolean legacySolid;

    @Shadow
    protected abstract BlockState asState();

    @Unique
    private volatile boolean omnifix$cacheInvalid;

    @Unique
    private static boolean omnifix$buildingCache;

    protected BlockStateBaseLazyCacheMixin(
            Block object,
            ImmutableMap<Property<?>, Comparable<?>> immutableMap,
            MapCodec<BlockState> mapCodec
    ) {
        super(object, immutableMap, mapCodec);
    }

    @Override
    public void omnifix$clearCache() {
        omnifix$cacheInvalid = true;
    }

    @Override
    public boolean omnifix$isCacheInvalid() {
        return omnifix$cacheInvalid;
    }

    @Unique
    private void omnifix$generateCache() {
        if (omnifix$cacheInvalid) {
            synchronized (BlockBehaviour.BlockStateBase.class) {
                if (omnifix$cacheInvalid && !omnifix$buildingCache) {
                    omnifix$buildingCache = true;
                    try {
                        this.initCache();
                        omnifix$cacheInvalid = false;
                    } finally {
                        omnifix$buildingCache = false;
                    }
                }
            }
        }
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;cache:Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase$Cache;",
                    ordinal = 0
            )
    )
    private BlockBehaviour.BlockStateBase.Cache omnifix$dynamicCacheGen(BlockBehaviour.BlockStateBase base) {
        omnifix$generateCache();
        return this.cache;
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;fluidState:Lnet/minecraft/world/level/material/FluidState;",
                    ordinal = 0
            ),
            require = 0
    )
    private FluidState omnifix$genCacheBeforeGettingFluid(BlockBehaviour.BlockStateBase base) {
        if (this.omnifix$cacheInvalid && this.fluidState == OMNIFIX$VANILLA_DEFAULT_FLUID) {
            synchronized (BlockBehaviour.BlockStateBase.class) {
                if (!omnifix$buildingCache) {
                    omnifix$buildingCache = true;
                    try {
                        this.fluidState = this.owner.getFluidState(this.asState());
                    } finally {
                        omnifix$buildingCache = false;
                    }
                }
            }
        }
        return this.fluidState;
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;isRandomlyTicking:Z",
                    ordinal = 0
            )
    )
    private boolean omnifix$genCacheBeforeGettingTicking(BlockBehaviour.BlockStateBase base) {
        if (this.omnifix$cacheInvalid) {
            return this.owner.isRandomlyTicking(this.asState());
        }
        return this.isRandomlyTicking;
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;legacySolid:Z",
                    ordinal = 0
            )
    )
    private boolean omnifix$genCacheBeforeCheckingSolid(BlockBehaviour.BlockStateBase base) {
        omnifix$generateCache();
        return this.legacySolid;
    }
}
