package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraftforge.network.HandshakeHandler;
import net.minecraftforge.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

/**
 * Forge handshake sends one login payload per tick by default, freezing the loading screen for
 * many seconds on large packs. Re-tick until progress stalls, synchronize {@code sentMessages},
 * and correct an off-by-one completion check (ModernFix fix_handshake_stall).
 */
@Mixin(value = HandshakeHandler.class, remap = false)
public class HandshakeHandlerMixin {

    @Shadow
    private int packetPosition;

    @Shadow
    private List<NetworkRegistry.LoginPayload> messageList;

    @Shadow
    private List<Integer> sentMessages;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$synchronizeSentMessages(CallbackInfo ci) {
        this.sentMessages = Collections.synchronizedList(this.sentMessages);
    }

    @WrapMethod(method = "tickServer")
    private boolean omnifix$retickHandshake(Operation<Boolean> original) {
        boolean isDoneTicking;
        int prevPacketPosition;
        do {
            prevPacketPosition = this.packetPosition;
            isDoneTicking = original.call();
        } while (!isDoneTicking && this.packetPosition > prevPacketPosition);
        return isDoneTicking;
    }

    @WrapOperation(
            method = "tickServer",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/util/List;removeIf(Ljava/util/function/Predicate;)Z", ordinal = 0)),
            require = 0)
    private boolean omnifix$preventEarlyExit(List<?> instance, Operation<Boolean> original) {
        if (instance != this.sentMessages) {
            return original.call(instance);
        }
        return original.call(instance) && this.packetPosition >= this.messageList.size();
    }
}
