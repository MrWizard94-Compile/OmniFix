package org.omnifix.mixin.perf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;

/**
 * Root cause: On the client, every {@link Entity#move}/{@link Entity#collide} still resolves
 * entity–entity collision shapes via {@link Level#getEntityCollisions}. That walks nearby
 * entities and builds {@link VoxelShape}s even for remote mobs, items, and other players —
 * work the server already owns. In crowded scenes this burns client FPS (MC-228976 class;
 * Entity Collision FPS Fix / CorgiTaco-class issue). Independent reimplementation — not a
 * copy of any GPL project.
 *
 * <p>Fix: during {@code Entity#collide}, replace the entity-collision shape list with empty
 * when this is a client-side entity that is <em>not</em> the local player and <em>not</em> a
 * vehicle the local player is riding/controlling. {@link Entity#collideBoundingBox} still
 * applies block (and world-border) collisions, so ground/wall prediction stays intact for
 * everyone; only redundant entity–entity resolution is dropped for non-predicted entities.
 *
 * <p>Local-player and ridden-vehicle paths keep full entity collisions so client-side
 * movement prediction is unchanged.
 */
@Mixin(Entity.class)
public abstract class ClientEntityCollisionMixin {

    /**
     * Redirect the sole {@link Level#getEntityCollisions} call inside private {@code collide}.
     * Returning an empty list skips entity–entity shape collection; block collision is
     * unchanged (handled later in {@link Entity#collideBoundingBox}).
     */
    @Redirect(
            method = "collide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<VoxelShape> omnifix$skipRemoteEntityCollisions(Level level, Entity entity, AABB box) {
        if (level.isClientSide && omnifix$shouldSkipEntityEntityCollision(entity)) {
            return Collections.emptyList();
        }
        return level.getEntityCollisions(entity, box);
    }

    /**
     * {@code true} when this entity does not need client-side entity–entity collision
     * prediction (server is authoritative).
     */
    @Unique
    private static boolean omnifix$shouldSkipEntityEntityCollision(Entity entity) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            // Pre-join / teardown: no local prediction consumer.
            return true;
        }
        // Keep full resolution for the local player (client prediction).
        if (entity == player) {
            return false;
        }
        // Keep for the vehicle the player is riding (client predicts vehicle move).
        Entity vehicle = player.getVehicle();
        if (vehicle != null && (entity == vehicle || entity == player.getRootVehicle())) {
            return false;
        }
        // Keep any other entity controlled by the local instance (e.g. controlling passenger
        // edge cases not covered by getVehicle identity alone).
        if (entity.isControlledByLocalInstance()) {
            return false;
        }
        return true;
    }
}
