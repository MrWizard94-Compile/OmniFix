package org.omnifix.mixin.vanilla;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-214147: Skeletons wearing leather armor still convert to strays in powder snow.
 * Cancel freeze conversion when any leather armor piece is equipped (leather prevents
 * freeze damage; conversion should respect the same insulation).
 */
@Mixin(Skeleton.class)
public abstract class LeatherSkeletonStrayMixin {

    @Inject(method = "isFreezeConverting", at = @At("HEAD"), cancellable = true, require = 0)
    private void omnifix$leatherBlocksStrayConversion(CallbackInfoReturnable<Boolean> cir) {
        Skeleton self = (Skeleton) (Object) this;
        if (self.getItemBySlot(EquipmentSlot.HEAD).is(Items.LEATHER_HELMET)
                || self.getItemBySlot(EquipmentSlot.CHEST).is(Items.LEATHER_CHESTPLATE)
                || self.getItemBySlot(EquipmentSlot.LEGS).is(Items.LEATHER_LEGGINGS)
                || self.getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS)) {
            cir.setReturnValue(false);
        }
    }
}
