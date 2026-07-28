package org.omnifix.compat.createportals;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.contraption.train.PortalTrackProvider;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import qouteall.imm_ptl.core.IPMcHelper;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.DQuaternion;

/**
 * Routes Create's portal-track pairing through Immersive Portals' portal entities — leg (a) of the
 * "Create train tracks do not link through IP portals" issue (Valkyrien Portals KNOWN_ISSUES #-1).
 *
 * <p>Create pairs train tracks across dimensions with a block-keyed provider registry
 * ({@code PortalTrackProvider.REGISTRY}, decompiled from {@code create-1.20.1-6.0.8.jar}): a track
 * placed against a portal block asks the block's provider for the far-side track position. The stock
 * nether provider simulates <em>vanilla</em> teleportation ({@code ITeleporter.getPortalInfo} →
 * {@code PortalForcer} POI search). With Immersive Portals installed, nether portals are IP
 * {@code Portal} entities overlaid on the vanilla blocks and the pairing is IP's own — the vanilla
 * simulation does not produce IP's linked position, so tracks never pair.
 *
 * <p>This provider resolves the far side through the overlapping IP portal entity instead:
 * {@code Portal.transformPoint} maps the touched portal block to its exact far-side twin and the
 * portal's rotation maps the travel direction — the same exit geometry Create's stock provider
 * derives from {@code PortalInfo} ({@code AllPortalTracks.fromProbe}: exit track one block past the
 * far portal block along the travel direction, facing back at the portal). Create's stock provider,
 * captured at registration, remains the fallback when no IP portal overlaps the block, so
 * vanilla-mechanics portals in the same pack keep working.
 *
 * <p>Registered from {@code FMLLoadCompleteEvent}: Create fills the registry from common-setup
 * {@code enqueueWork} and {@code SimpleRegistry.register} is last-write-wins, so load-complete
 * guarantees this override lands after Create's default regardless of mod-load order. Leg (b) —
 * IP's general-purpose portals with no portal <em>block</em> — is implemented by
 * {@link IpEntityPortalTrackCompat} ({@code create.ip_tracks_b}).
 */
public final class IpPortalTrackCompat {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** IP portal entities within this range of the touched portal block are considered overlapping. */
    private static final double PORTAL_OVERLAP_RANGE = 2.0;

    private IpPortalTrackCompat() {}

    public static void register() {
        final PortalTrackProvider stock = PortalTrackProvider.REGISTRY.get(Blocks.NETHER_PORTAL);
        PortalTrackProvider.REGISTRY.register(Blocks.NETHER_PORTAL,
            (level, inboundTrack) -> findExit(level, inboundTrack, stock));
        LOGGER.info("[OmniFix/CreateIP] Nether-portal track pairing routed through Immersive Portals "
                + "(stock Create provider kept as fallback: {}).", stock != null);
    }

    private static PortalTrackProvider.Exit findExit(final ServerLevel level, final BlockFace inboundTrack,
                                                     final PortalTrackProvider stock) {
        final BlockPos portalPos = inboundTrack.getConnectedPos();
        final Vec3 center = Vec3.atCenterOf(portalPos);

        Portal portal = null;
        double best = Double.MAX_VALUE;
        final List<Portal> nearby = IPMcHelper.getNearbyPortals(level, center, PORTAL_OVERLAP_RANGE)
            .collect(Collectors.toList());
        for (final Portal candidate : nearby) {
            if (candidate.getDestDim() == null) {
                continue;
            }
            final double d = candidate.getDistanceToNearestPointInPortal(center);
            if (d < best) {
                best = d;
                portal = candidate;
            }
        }
        if (portal == null) {
            // No IP portal overlaps this block — a vanilla-mechanics portal; let Create's own
            // teleporter simulation handle it.
            return stock == null ? null : stock.findExit(level, inboundTrack);
        }

        final ServerLevel destLevel = level.getServer().getLevel(portal.getDestDim());
        if (destLevel == null) {
            return null;
        }

        // The travel direction (track → portal), mapped through the portal's rotation. A portal that
        // rotates the track into the vertical has no straight continuation — refuse the pairing.
        final Direction travel = inboundTrack.getFace();
        final Vec3 farTravelVec = rotate(portal, new Vec3(travel.getStepX(), travel.getStepY(), travel.getStepZ()));
        final Direction farTravel = Direction.getNearest(farTravelVec.x, farTravelVec.y, farTravelVec.z);
        if (farTravel.getAxis() == Direction.Axis.Y) {
            LOGGER.debug("[OmniFix/CreateIP] Portal at {} rotates track travel into the vertical; not pairing.",
                portalPos);
            return null;
        }

        // Far-side twin of the touched portal block. transformPoint maps pane-to-pane exactly; the
        // neighbor walk absorbs rounding when the touched block sits at the pane's edge.
        final Vec3 farCenter = portal.transformPoint(center);
        BlockPos farPortalPos = BlockPos.containing(farCenter);
        if (!destLevel.getBlockState(farPortalPos).is(Blocks.NETHER_PORTAL)) {
            BlockPos adjusted = null;
            for (final Direction d : Direction.values()) {
                if (destLevel.getBlockState(farPortalPos.relative(d)).is(Blocks.NETHER_PORTAL)) {
                    adjusted = farPortalPos.relative(d);
                    break;
                }
            }
            if (adjusted == null) {
                LOGGER.debug("[OmniFix/CreateIP] No far-side portal block near {} in {}; not pairing.",
                    farPortalPos, destLevel.dimension().location());
                return null;
            }
            farPortalPos = adjusted;
        }

        // Same exit geometry as Create's fromProbe: the far track sits one block past the far portal
        // block, continuing the travel direction, facing back at the portal.
        final BlockPos otherPos = farPortalPos.relative(farTravel);
        return new PortalTrackProvider.Exit(destLevel, new BlockFace(otherPos, farTravel.getOpposite()));
    }

    private static Vec3 rotate(final Portal portal, final Vec3 vec) {
        final DQuaternion rotation = portal.getRotationD();
        return rotation == null ? vec : rotation.rotate(vec);
    }
}
