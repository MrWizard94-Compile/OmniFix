package com.valkyrienportals.mixin.client;

import java.util.TreeMap;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

/**
 * DIAGNOSTIC PROBE (throttled, main-pass only): decides whether the fix for ships-invisible-through-
 * portals is a client-side loading shim or needs server cooperation.
 *
 * <p>The {@code [VP-SHIP]} probe proved the render pipeline is fine and that
 * {@code getLoadedShips()} (render-ready {@code ClientShip}s) only holds the player's current
 * dimension. The open question: does the client even KNOW about the remote dimension's ships?
 * The client ship world exposes two views — {@code getAllShips()} (all ships it has metadata for)
 * and {@code getLoadedShips()} (the subset promoted to render-ready). This logs both, bucketed by
 * {@code ship.getChunkClaimDimension()}, plus the player's current dimension.
 *
 * <ul>
 *   <li><b>all[remoteDim] &gt; 0, loaded[remoteDim] == 0</b> → the metadata IS present; the server
 *       sends it. The fix is a CLIENT-side shim: promote those ships to loaded + load their shipyard
 *       chunks into the remote {@code ClientLevel} so the RSM builds them (we proved it then does).</li>
 *   <li><b>all[remoteDim] == 0</b> → the server never sends the remote dimension's ships while the
 *       player is elsewhere. The fix needs SERVER-side ship tracking for portal-viewed dimensions —
 *       a much larger change, likely not doable purely from this compat mod.</li>
 * </ul>
 *
 * <p>Test: stand in dim A near a portal with a ship on the far side in dim B; read the {@code [VP-DATA]}
 * line and compare the {@code all=}/{@code loaded=} counts for dim B.
 */
@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class MixinRenderSectionManagerShipDataProbe {

    private static final Logger VP_DATA_LOG = LoggerFactory.getLogger("ValkyrienPortals-ShipData");
    private static long vp$nextDataLog;

    @Inject(method = "createTerrainRenderList", at = @At("TAIL"))
    private void vp$probeShipData(Camera camera, Viewport viewport, int frame, boolean spectator,
                                  CallbackInfo ci) {
        // The client ship world is global, so one main-pass dump per second is enough; skip the
        // nested portal passes to avoid multiplying the log line per rendered dimension.
        if (PortalRendering.isRendering()) {
            return;
        }
        long now = System.nanoTime();
        if (now < vp$nextDataLog) {
            return;
        }
        vp$nextDataLog = now + 1_000_000_000L;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        String playerDim = level == null ? "null" : VSGameUtilsKt.getDimensionId(level);

        VsiClientShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(mc);

        // dim -> [allCount, loadedCount]
        TreeMap<String, int[]> byDim = new TreeMap<>();
        for (ClientShip ship : shipWorld.getAllShips()) {
            byDim.computeIfAbsent(ship.getChunkClaimDimension(), k -> new int[2])[0]++;
        }
        for (ClientShip ship : shipWorld.getLoadedShips()) {
            byDim.computeIfAbsent(ship.getChunkClaimDimension(), k -> new int[2])[1]++;
        }

        StringBuilder sb = new StringBuilder();
        for (var entry : byDim.entrySet()) {
            sb.append(' ').append(entry.getKey())
                .append("{all=").append(entry.getValue()[0])
                .append(",loaded=").append(entry.getValue()[1]).append('}');
        }
        if (byDim.isEmpty()) {
            sb.append(" <none>");
        }

        VP_DATA_LOG.info("[VP-DATA] playerDim={} shipsByDim:{}", playerDim, sb);
    }
}
