package org.omnifix.mixin.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-2025 — Mobs slip through thin fences / get stuck after chunk reload.
 *
 * <p>Root cause: entity AABB is not serialized. On load, {@link Entity#load} rebuilds the box
 * from position + dimensions via {@link Entity#reapplyPosition}, which centers a full-size box on
 * the saved feet position. Entities that had been slightly offset into a fence collision (valid
 * at save time) lose that offset and no longer collide until they move again.
 *
 * <p>Fix: persist the live AABB under the {@code AABB} double-list key on
 * {@link Entity#saveWithoutId}, and restore it after {@code reapplyPosition} on load.
 */
@Mixin(Entity.class)
public abstract class MobFenceEscapeMixin {

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract void setBoundingBox(AABB boundingBox);

    @Shadow
    protected abstract ListTag newDoubleList(double... values);

    @Inject(
            method = "saveWithoutId",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;putFloat(Ljava/lang/String;F)V",
                    ordinal = 0
            )
    )
    private void omnifix$writeAabb(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        AABB aabb = this.getBoundingBox();
        tag.put(
                "AABB",
                this.newDoubleList(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ)
        );
    }

    @Inject(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;reapplyPosition()V",
                    shift = At.Shift.BY,
                    by = 2
            )
    )
    private void omnifix$readAabb(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("AABB")) {
            ListTag aabbNbt = tag.getList("AABB", Tag.TAG_DOUBLE);
            this.setBoundingBox(new AABB(
                    aabbNbt.getDouble(0),
                    aabbNbt.getDouble(1),
                    aabbNbt.getDouble(2),
                    aabbNbt.getDouble(3),
                    aabbNbt.getDouble(4),
                    aabbNbt.getDouble(5)
            ));
        }
    }
}
