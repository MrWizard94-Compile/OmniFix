package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-108948 — Boats appear to hover / stutter on slime (and similar) when not controlled by the
 * local client instance.
 *
 * <p>Root cause: {@link Boat#tick} only runs {@code floatBoat}/{@code move} when
 * {@link Entity#isControlledByLocalInstance()} is true. Remote or non-controlling client views
 * zero velocity and never re-apply buoyancy/collision, so the boat visually hangs above slime.
 * Always run the float/move path on the client, but still restrict paddle input packets to the
 * true local controller.
 */
@Mixin(Boat.class)
public abstract class BoatSlimeHoverMixin extends Entity {

    protected BoatSlimeHoverMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    /**
     * Force the {@code isControlledByLocalInstance()} branch so floatBoat/move always run.
     */
    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/Boat;isControlledByLocalInstance()Z"
            )
    )
    private boolean omnifix$alwaysUpdateBoatPhysics(boolean controlledByLocal) {
        return true;
    }

    /**
     * The second {@code level.isClientSide} read in {@code tick} gates {@code controlBoat} and the
     * paddle packet. Keep that path only for the real local controller so non-controlling clients
     * do not spam paddle packets or clobber remote paddle state.
     */
    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;isClientSide:Z",
                    ordinal = 1
            )
    )
    private boolean omnifix$paddleOnlyWhenLocal(boolean isClientSide) {
        return isClientSide && this.isControlledByLocalInstance();
    }
}
