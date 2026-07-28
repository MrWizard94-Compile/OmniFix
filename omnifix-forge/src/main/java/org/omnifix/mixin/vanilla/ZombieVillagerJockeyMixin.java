package org.omnifix.mixin.vanilla;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.ZombieVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-200418: Cured baby zombie villagers stay as jockey (remain riding chickens).
 * Dismount / eject passengers as conversion finishes so the cured villager is not left mounted.
 */
@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerJockeyMixin {

    @Inject(method = "finishConversion", at = @At("HEAD"), require = 0)
    private void omnifix$dismountBeforeCure(ServerLevel level, CallbackInfo ci) {
        ZombieVillager self = (ZombieVillager) (Object) this;
        if (self.isPassenger()) {
            self.stopRiding();
        }
        if (self.isVehicle()) {
            self.ejectPassengers();
        }
    }
}
