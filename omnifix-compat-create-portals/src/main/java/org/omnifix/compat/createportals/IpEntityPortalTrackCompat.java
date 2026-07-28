package org.omnifix.compat.createportals;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackShape;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.IPMcHelper;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.DQuaternion;

/**
 * Leg (b) of Create×IP track pairing: Immersive Portals' <em>block-less</em> general portals.
 *
 * <p>Create's stock {@code TrackBlock.connectToPortal} only keys on portal <em>blocks</em> via
 * {@code PortalTrackProvider.isSupportedPortal}. IP wand/command/datapack portals are pure
 * {@link Portal} entities with no pane block, so the stock path never fires. This helper is
 * invoked after the stock path returns without converting the track: it finds an overlapping IP
 * portal entity, maps the far-side exit with {@link Portal#transformPoint} + rotation (same
 * geometry as {@link IpPortalTrackCompat} / Create's {@code fromProbe}), places the twin portal
 * track, and binds both {@link TrackBlockEntity}s.
 *
 * <p>Also keeps portal tracks alive when Create's neighbor update would delete them for lack of a
 * portal block while a supporting IP portal entity is still present.
 */
public final class IpEntityPortalTrackCompat {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Max distance from the track-side portal cell to the nearest point on a portal plane for the
     * portal to be considered "adjacent" for pairing.
     */
    private static final double PORTAL_ADJACENCY = 1.35;

    /**
     * Portal normal must face roughly opposite the track→portal travel direction (dot of normal
     * with travel ≤ this). -0.35 allows mild angles while rejecting portals the track is edge-on to.
     */
    private static final double NORMAL_DOT_MAX = -0.35;

    private IpEntityPortalTrackCompat() {}

    /**
     * After Create's block-keyed {@code connectToPortal} returns without promoting the track to a
     * portal shape, try pairing through a nearby IP portal entity.
     */
    public static void tryConnectAfterStock(final ServerLevel level, final BlockPos pos, final BlockState state) {
        if (!(state.getBlock() instanceof TrackBlock)) {
            return;
        }
        final TrackShape shape = state.getValue(TrackBlock.SHAPE);
        if (shape.isPortal() || shape.isJunction()) {
            return;
        }
        final Direction.Axis portalTest = axisForShape(shape);
        if (portalTest == null) {
            return;
        }

        boolean pop = false;
        String fail = null;
        BlockPos failPos = null;

        for (final Direction d : Iterate.directionsInAxis(portalTest)) {
            final Portal portal = findPortalToward(level, pos, d);
            if (portal == null) {
                continue;
            }
            pop = true;

            final Exit exit = resolveExit(level, pos, d, portal);
            if (exit == null) {
                fail = "missing";
                continue;
            }

            final ServerLevel otherLevel = exit.level();
            final BlockPos otherTrackPos = exit.face().getPos();
            final BlockState existing = otherLevel.getBlockState(otherTrackPos);
            if (!existing.canBeReplaced()) {
                fail = "blocked";
                failPos = otherTrackPos;
                continue;
            }

            final Direction otherFace = exit.face().getFace();
            level.setBlock(pos,
                    state.setValue(TrackBlock.SHAPE, TrackShape.asPortal(d))
                            .setValue(TrackBlock.HAS_BE, true),
                    3);
            final BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TrackBlockEntity tbe) {
                tbe.bind(otherLevel.dimension(), otherTrackPos);
            }

            final BlockState otherState = ProperWaterloggedBlock.withWater(otherLevel,
                    state.setValue(TrackBlock.SHAPE, TrackShape.asPortal(otherFace))
                            .setValue(TrackBlock.HAS_BE, true),
                    otherTrackPos);
            otherLevel.setBlock(otherTrackPos, otherState, 3);
            final BlockEntity otherBE = otherLevel.getBlockEntity(otherTrackPos);
            if (otherBE instanceof TrackBlockEntity tbe) {
                tbe.bind(level.dimension(), pos);
            }

            LOGGER.info("[OmniFix/CreateIP] Entity-portal track pair {}@{} ↔ {}@{} via portal {}",
                    level.dimension().location(), pos,
                    otherLevel.dimension().location(), otherTrackPos,
                    portal.getUUID());
            pop = false;
            return;
        }

        if (!pop) {
            return;
        }
        // Mirror Create: destroy the inbound track and notify the nearest player of the failure.
        level.destroyBlock(pos, true);
        if (fail == null) {
            return;
        }
        final Player player = level.getNearestPlayer(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10.0, p -> true);
        if (player == null) {
            return;
        }
        // Match Create's portal_track.* lang keys (CreateLang.translateDirect("portal_track.failed")).
        player.displayClientMessage(Component.literal("<!> ")
                .append(Component.translatable("create.portal_track.failed"))
                .withStyle(ChatFormatting.GOLD), false);
        final MutableComponent detail = failPos != null
                ? Component.translatable("create.portal_track." + fail,
                        failPos.getX(), failPos.getY(), failPos.getZ())
                : Component.translatable("create.portal_track." + fail);
        player.displayClientMessage(Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                .append(detail.withStyle(s -> s.withColor(0xFFCC54))), false);
    }

    /**
     * True when a portal-shaped track at {@code pos} still has a supporting IP portal entity in the
     * portal-facing direction — used to cancel Create's "no portal block → delete track" neighbor
     * update for block-less portals.
     */
    public static boolean hasSupportingEntityPortal(final LevelAccessor level, final BlockPos pos,
                                                   final BlockState state) {
        if (!(state.getBlock() instanceof TrackBlock) || !(level instanceof Level world)) {
            return false;
        }
        final TrackShape shape = state.getValue(TrackBlock.SHAPE);
        if (!shape.isPortal()) {
            return false;
        }
        for (final Direction d : Iterate.horizontalDirections) {
            if (TrackShape.asPortal(d) != shape) {
                continue;
            }
            if (findPortalToward(world, pos, d) != null) {
                return true;
            }
        }
        return false;
    }

    private static Direction.Axis axisForShape(final TrackShape shape) {
        if (shape == TrackShape.XO) {
            return Direction.Axis.X;
        }
        if (shape == TrackShape.ZO) {
            return Direction.Axis.Z;
        }
        return null;
    }

    /**
     * Find the best IP portal whose plane sits in the cell (or immediately beyond) the track faces
     * when looking along {@code towardPortal}.
     */
    private static Portal findPortalToward(final Level level, final BlockPos trackPos,
                                           final Direction towardPortal) {
        final Vec3 probe = Vec3.atCenterOf(trackPos.relative(towardPortal));
        final Vec3 travel = new Vec3(towardPortal.getStepX(), towardPortal.getStepY(), towardPortal.getStepZ());

        Portal best = null;
        double bestDist = Double.MAX_VALUE;
        final List<Portal> nearby = IPMcHelper.getNearbyPortals(level, probe, 3.0)
                .collect(Collectors.toList());
        for (final Portal portal : nearby) {
            if (portal.getDestDim() == null || !portal.isTeleportable()) {
                continue;
            }
            // Portal should face back at the track (normal anti-parallel to travel).
            final Vec3 normal = portal.getNormal();
            if (normal.dot(travel) > NORMAL_DOT_MAX) {
                continue;
            }
            final double dist = portal.getDistanceToNearestPointInPortal(probe);
            if (dist > PORTAL_ADJACENCY) {
                continue;
            }
            if (dist < bestDist) {
                bestDist = dist;
                best = portal;
            }
        }
        return best;
    }

    /**
     * Far-side exit geometry: rotate travel through the portal, map the this-side track center with
     * {@link Portal#transformPoint}, place the twin one step past the mapped portal plane along the
     * far travel direction when the map lands on/near the plane itself.
     */
    private static Exit resolveExit(final ServerLevel level, final BlockPos trackPos,
                                    final Direction towardPortal, final Portal portal) {
        final ServerLevel destLevel = level.getServer().getLevel(portal.getDestDim());
        if (destLevel == null) {
            return null;
        }

        final Vec3 travelVec = new Vec3(
                towardPortal.getStepX(), towardPortal.getStepY(), towardPortal.getStepZ());
        final Vec3 farTravelVec = rotate(portal, travelVec);
        final Direction farTravel = Direction.getNearest(farTravelVec.x, farTravelVec.y, farTravelVec.z);
        if (farTravel.getAxis() == Direction.Axis.Y) {
            LOGGER.debug("[OmniFix/CreateIP] Entity portal rotates track travel vertical; not pairing at {}",
                    trackPos);
            return null;
        }

        // Transform the track center: for a portal sitting one block past the track, this lands on
        // the far-side twin track cell (or immediately adjacent). Walk one step along farTravel when
        // the mapped point still sits on/inside the far portal plane (distance ~0).
        final Vec3 farCenter = portal.transformPoint(Vec3.atCenterOf(trackPos));
        BlockPos otherPos = BlockPos.containing(farCenter);

        // Prefer the cell continuing the travel if the direct map is still "inside" the portal volume
        // (near dest origin) — matches Create's "one past the portal block" exit.
        final Vec3 destOrigin = portal.getDestPos();
        if (farCenter.distanceToSqr(destOrigin) < 0.75 * 0.75) {
            otherPos = BlockPos.containing(destOrigin).relative(farTravel);
        } else {
            // If something solid already occupies the mapped cell and the step-past is free, step past.
            final BlockPos stepped = otherPos.relative(farTravel);
            if (!destLevel.getBlockState(otherPos).canBeReplaced()
                    && destLevel.getBlockState(stepped).canBeReplaced()) {
                otherPos = stepped;
            }
        }

        return new Exit(destLevel, new BlockFace(otherPos, farTravel.getOpposite()));
    }

    private static Vec3 rotate(final Portal portal, final Vec3 vec) {
        final DQuaternion rotation = portal.getRotationD();
        return rotation == null ? vec : rotation.rotate(vec);
    }

    private record Exit(ServerLevel level, BlockFace face) {}
}
