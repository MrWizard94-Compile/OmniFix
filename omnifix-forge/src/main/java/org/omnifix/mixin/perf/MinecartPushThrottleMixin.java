package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.List;

/**
 * Root cause: every server {@link AbstractMinecart#tick} runs a neighbour entity scan for
 * minecart-to-minecart push (and, when moving + ridable, ride/push). Stationary storage carts,
 * chest-cart farms, and parking sidings re-query the entity section map every tick even when
 * horizontal speed is near zero and nothing can change until another cart arrives.
 *
 * <p>Vanilla (MC 1.20.1) {@code tick} server branch:
 * <ul>
 *   <li><b>Moving + ridable</b> ({@code horizontalDistanceSqr() > 0.01D}):
 *       {@link Level#getEntities(Entity, AABB, java.util.function.Predicate)} with
 *       {@code EntitySelector.pushableBy} — ride pickup / push at full rate.</li>
 *   <li><b>Else</b> (nearly still, or non-ridable):
 *       {@link Level#getEntities(Entity, AABB)} — only pushes other minecarts.</li>
 * </ul>
 *
 * <p>Approach: {@link WrapOperation} on the <em>no-predicate</em> {@code getEntities} invoke
 * inside {@code tick} only. On the server, when horizontal speed squared is {@code <= 0.01D}
 * and {@code tickCount} is odd, return {@link Collections#emptyList()} without scanning.
 * Even ticks and any cart with speed above the threshold keep the vanilla call. The moving
 * ridable branch (predicate overload) is intentionally <em>not</em> wrapped so ride/push
 * stays full-rate when carts are actually rolling.
 *
 * <p>The exclude-{@code Entity} argument of {@code getEntities} is the minecart itself, so
 * delta-movement and {@code tickCount} are taken from that receiver without a cast on
 * {@code this}. Client never reaches this invoke (client tick returns before the scan), but
 * {@code isClientSide} is still gated for safety.
 *
 * <p><b>Trade-off:</b> nearly still minecarts resolve neighbour minecart push every other
 * server tick (~50 ms extra latency at 20 TPS). Rolling carts and ride-on-contact while
 * moving are unchanged. Acceptable for parked / storage cart density; matches Lithium-class
 * mild behavioural change for low-speed push only.
 *
 * <p>Unit: {@code perf.minecart_push_throttle}
 */
@Mixin(AbstractMinecart.class)
public abstract class MinecartPushThrottleMixin {

    /**
     * Still-branch neighbour scan: {@code Level#getEntities(Entity, AABB)} when not
     * (ridable and moving). Throttle only when nearly still on the server.
     * Speed gate uses the same {@code 0.01D} horizontalDistanceSqr threshold vanilla uses
     * to choose the ride/push scan vs this still-push scan.
     */
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<Entity> omnifix$throttleStillPushScan(
            Level level,
            Entity self,
            AABB box,
            Operation<List<Entity>> original
    ) {
        // Server + nearly still + odd tick → skip entity section walk (empty neighbour list).
        if (!level.isClientSide
                && self.getDeltaMovement().horizontalDistanceSqr() <= 0.01D
                && (self.tickCount & 1) != 0) {
            return Collections.emptyList();
        }
        return original.call(level, self, box);
    }
}
