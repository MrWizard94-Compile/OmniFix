package org.omnifix.mixin.perf;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Root cause: {@link Enum#values()} allocates a cloned array on every call. Neighbour-update
 * and redstone paths still invoke {@link Direction#values()} in tight loops even though
 * shape-order walks already use static {@code UPDATE_SHAPE_ORDER} /
 * {@code NeighborUpdater.UPDATE_ORDER}.
 *
 * <p>Safe redirects only: every listed method iterates the array and never writes to it.
 * A global {@code Direction.values()} overwrite would be unsafe for callers that index or
 * mutate the returned array.
 *
 * <p>Mixin AP cannot multi-target these classes in one injector (SRG name conflicts on
 * shared method names such as {@code neighborChanged}). Nested single-target mixins share
 * one static cache. Nested classes are listed in {@code omnifix.perf.mixins.json} as
 * {@code DirectionValuesCacheMixin$…}; the plugin gates all of them via
 * {@code contains("DirectionValuesCacheMixin")} → {@code perf.direction_values_cache}.
 */
@Mixin(Level.class)
public abstract class DirectionValuesCacheMixin {

    @Unique
    private static final Direction[] OMNIFIX$DIRECTIONS = Direction.values();

    @Unique
    private static final Direction.Axis[] OMNIFIX$AXES = Direction.Axis.values();

    /**
     * {@link Level#updateNeighbourForOutputSignal} — comparator / container output fan-out.
     */
    @Redirect(
            method = "updateNeighbourForOutputSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
            )
    )
    private Direction[] omnifix$cachedDirectionValues() {
        return OMNIFIX$DIRECTIONS;
    }

    /**
     * {@link LeavesBlock} leaf-distance propagation (neighbour shape updates).
     */
    @Mixin(LeavesBlock.class)
    public abstract static class Leaves {
        @Redirect(
                method = "updateDistance(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
                )
        )
        private static Direction[] omnifix$cachedDirectionValues() {
            return OMNIFIX$DIRECTIONS;
        }
    }

    /**
     * {@link RedStoneWireBlock} power + corner neighbour fan-out.
     */
    @Mixin(RedStoneWireBlock.class)
    public abstract static class RedstoneWire {
        @Redirect(
                method = {
                        "updatePowerStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
                        "checkCornerChangeAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
                        "onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
                },
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
                )
        )
        private Direction[] omnifix$cachedDirectionValues() {
            return OMNIFIX$DIRECTIONS;
        }
    }

    /**
     * {@link RedstoneTorchBlock#onPlace} / {@link RedstoneTorchBlock#onRemove} neighbour notifies.
     */
    @Mixin(RedstoneTorchBlock.class)
    public abstract static class RedstoneTorch {
        @Redirect(
                method = {
                        "onPlace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
                        "onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
                },
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
                )
        )
        private Direction[] omnifix$cachedDirectionValues() {
            return OMNIFIX$DIRECTIONS;
        }
    }

    /**
     * {@link DiodeBlock#neighborChanged} — repeater/comparator side-input scan.
     */
    @Mixin(DiodeBlock.class)
    public abstract static class Diode {
        @Redirect(
                method = "neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
                )
        )
        private Direction[] omnifix$cachedDirectionValues() {
            return OMNIFIX$DIRECTIONS;
        }
    }

    /**
     * {@link PistonBaseBlock} {@code getNeighborSignal} — quasi-connectivity scan.
     */
    @Mixin(PistonBaseBlock.class)
    public abstract static class Piston {
        @Redirect(
                method = "getNeighborSignal(Lnet/minecraft/world/level/SignalGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
                )
        )
        private Direction[] omnifix$cachedDirectionValues() {
            return OMNIFIX$DIRECTIONS;
        }
    }

    /**
     * {@code BlockBehaviour.BlockStateBase.Cache} constructor streams
     * {@link Direction.Axis#values()} once per blockstate. Soft-dupes
     * {@link BlockStateEnumCacheMixin} when both units are on ({@code require=0} on second apply).
     */
    @Mixin(targets = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache")
    public abstract static class AxisCache {
        @Redirect(
                method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;)V",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/core/Direction$Axis;values()[Lnet/minecraft/core/Direction$Axis;"
                ),
                require = 0
        )
        private Direction.Axis[] omnifix$cachedAxisValues() {
            return OMNIFIX$AXES;
        }
    }
}
