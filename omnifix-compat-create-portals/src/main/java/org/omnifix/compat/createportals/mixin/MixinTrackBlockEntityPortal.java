package org.omnifix.compat.createportals.mixin;

import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.omnifix.compat.createportals.IpEntityPortalTrackCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create×IP leg (b): after Create's block-keyed portal-track path returns without promoting a
 * track, pair through Immersive Portals entity portals; and keep portal tracks that would be
 * deleted solely because no portal <em>block</em> remains.
 */
@Mixin(value = TrackBlock.class, remap = true)
public abstract class MixinTrackBlockEntityPortal {

    // Create method — not SRG-mapped; remap=false required for Mixin AP.
    @Inject(method = "connectToPortal", at = @At("RETURN"), remap = false)
    private void omnifix$connectEntityPortal(final ServerLevel level, final BlockPos pos,
                                             final BlockState state, final CallbackInfo ci) {
        // Stock path may have replaced or destroyed the block; read the live state.
        final BlockState live = level.getBlockState(pos);
        if (live.isAir()) {
            return;
        }
        if (live.hasProperty(TrackBlock.SHAPE) && live.getValue(TrackBlock.SHAPE).isPortal()) {
            return;
        }
        IpEntityPortalTrackCompat.tryConnectAfterStock(level, pos, live);
    }

    /**
     * Create's published jar keeps the SRG name for the vanilla {@code updateShape} override
     * ({@code m_7417_}); {@code remap=false} matches that bytecode at both compile and runtime.
     */
    @Inject(method = "m_7417_", at = @At("RETURN"), cancellable = true, remap = false)
    private void omnifix$keepEntityPortalTrack(final BlockState state, final Direction direction,
                                               final BlockState neighborState, final LevelAccessor level,
                                               final BlockPos pos, final BlockPos neighborPos,
                                               final CallbackInfoReturnable<BlockState> cir) {
        final BlockState result = cir.getReturnValue();
        // Create returns Blocks.AIR.defaultBlockState() when the supporting portal block vanishes.
        if (result == null || !result.isAir()) {
            return;
        }
        if (!state.hasProperty(TrackBlock.SHAPE) || !state.getValue(TrackBlock.SHAPE).isPortal()) {
            return;
        }
        if (IpEntityPortalTrackCompat.hasSupportingEntityPortal(level, pos, state)) {
            cir.setReturnValue(state);
        }
    }
}
