package org.omnifix.mixin.perf;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * After reading a section from the network, if bit storage is oversized but all zeros, recreate
 * as a single-value container — common waste from some servers/mod generators.
 */
@Mixin(PalettedContainer.class)
public abstract class PalettedContainerCompactMixin<T> {

    @Shadow
    private volatile PalettedContainer.Data<T> data;

    @Shadow
    protected abstract PalettedContainer.Data<T> createOrReuseData(@Nullable PalettedContainer.Data<T> data, int id);

    @Inject(
            method = "read(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/chunk/PalettedContainer;data:Lnet/minecraft/world/level/chunk/PalettedContainer$Data;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void omnifix$compactEmptyStorage(FriendlyByteBuf buffer, CallbackInfo ci, int bits) {
        if (bits <= 1) {
            return;
        }
        long[] storArray = this.data.storage().getRaw();
        boolean empty = true;
        for (long l : storArray) {
            if (l != 0) {
                empty = false;
                break;
            }
        }
        if (!empty || storArray.length == 0) {
            return;
        }
        T value;
        try {
            value = this.data.palette().valueFor(0);
        } catch (RuntimeException e) {
            // Buggy remote palette — leave as-is; game will likely fail later for other reasons.
            return;
        }
        this.data = this.createOrReuseData(null, 0);
        this.data.palette().idFor(value);
    }
}
