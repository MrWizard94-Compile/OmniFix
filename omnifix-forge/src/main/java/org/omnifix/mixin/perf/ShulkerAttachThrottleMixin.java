package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Root cause: {@link Shulker#tick} calls {@code findNewAttachment()} whenever the current attach
 * face is invalid ({@code !canStayAt}). That method scans neighboring solid surfaces and may
 * teleport the shulker — non-trivial block/state work on every tick while a shulker is floating
 * or its support was destroyed.
 *
 * <p>Throttle: {@link WrapWithCondition} on the {@code findNewAttachment()V} invoke so attachment
 * search runs only when {@code (tickCount % 3) == 0}. {@link Shulker} extends LivingEntity, so
 * {@code tickCount} is available on the entity instance.
 *
 * <p>Trade-off: re-attach / teleport after the support surface becomes invalid may wait up to 2
 * ticks (~100&nbsp;ms) before the next search. Open/close animation, peek, and other tick work are
 * unchanged.
 *
 * <p>Unit: {@code perf.shulker_attach_throttle}
 */
@Mixin(Shulker.class)
public abstract class ShulkerAttachThrottleMixin {

    /**
     * Allows {@code findNewAttachment} only every third entity tick.
     *
     * @param self shulker whose attach face is invalid this tick
     * @return {@code true} to run {@code findNewAttachment}; {@code false} to skip this tick
     */
    @WrapWithCondition(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Shulker;findNewAttachment()V"
            )
    )
    private boolean omnifix$throttleAttach(Shulker self) {
        return (self.tickCount % 3) == 0;
    }
}
