package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Root cause: idle End gateways invoke {@link Level#getEntitiesOfClass} every tick from
 * {@code TheEndGatewayBlockEntity.teleportTick} while not cooling down. On empty AABBs that scan is
 * pure entity-map cost with no teleport work.
 *
 * <p>Approach: {@link WrapOperation} on the {@code getEntitiesOfClass} call only. Odd game ticks
 * return an empty list (skip scan); even ticks run vanilla. Age increment, cooldown countdown,
 * spawn path, setChanged, and the {@code age % 2400} auto-cooldown all remain on the vanilla path
 * because this wrap never cancels {@code teleportTick}. The cooling-down branch never reaches
 * {@code getEntitiesOfClass}, so cooldown ticks are unaffected.
 *
 * <p>Trade-off: entity enter-gateway detection runs every other game tick when not cooling
 * (~50 ms extra latency at 20 TPS). Acceptable for rare portal use; halves idle empty-scan cost.
 *
 * <p>Unit: {@code perf.end_gateway_entity_scan_throttle}
 */
@Mixin(TheEndGatewayBlockEntity.class)
public abstract class EndGatewayEntityScanMixin {

    @WrapOperation(
            method = "teleportTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private static <T extends Entity> List<T> omnifix$throttleEntityScan(
            Level level,
            Class<T> entityClass,
            AABB box,
            Predicate<? super T> predicate,
            Operation<List<T>> original
    ) {
        if ((level.getGameTime() & 1L) != 0L) {
            return Collections.emptyList();
        }
        return original.call(level, entityClass, box, predicate);
    }
}
