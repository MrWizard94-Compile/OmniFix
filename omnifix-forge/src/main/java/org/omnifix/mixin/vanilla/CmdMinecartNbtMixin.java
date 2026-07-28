package org.omnifix.mixin.vanilla;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MC-121903 — Command block minecart activation cooldown is not saved to NBT.
 *
 * <p>Root cause: {@link MinecartCommandBlock} tracks {@code lastActivated} (tick of last successful
 * {@code activateMinecart}) only in memory. Chunk unload / world save drops it, so a minecart that
 * just fired can fire again immediately after reload. Persist it under the established
 * {@code LastExecuted} key (Debugify-compatible).
 */
@Mixin(MinecartCommandBlock.class)
public abstract class CmdMinecartNbtMixin {

    @Shadow
    private int lastActivated;

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void omnifix$readLastActivated(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("LastExecuted")) {
            this.lastActivated = tag.getInt("LastExecuted");
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void omnifix$writeLastActivated(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("LastExecuted", this.lastActivated);
    }
}
