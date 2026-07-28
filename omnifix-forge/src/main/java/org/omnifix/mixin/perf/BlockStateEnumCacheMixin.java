package org.omnifix.mixin.perf;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SupportType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code Enum.values()} allocates a new array every call. Cache for BlockState Cache construction.
 */
@Mixin(targets = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache")
public abstract class BlockStateEnumCacheMixin {

    @Unique
    private static final SupportType[] OMNIFIX$SUPPORT = SupportType.values();

    @Unique
    private static final Direction.Axis[] OMNIFIX$AXIS = Direction.Axis.values();

    @Redirect(
            method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/SupportType;values()[Lnet/minecraft/world/level/block/SupportType;"
            )
    )
    private SupportType[] omnifix$supportTypes() {
        return OMNIFIX$SUPPORT;
    }

    @Redirect(
            method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Direction$Axis;values()[Lnet/minecraft/core/Direction$Axis;"
            )
    )
    private Direction.Axis[] omnifix$axes() {
        return OMNIFIX$AXIS;
    }
}
