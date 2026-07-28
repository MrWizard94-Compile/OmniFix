package org.omnifix.mixin.perf;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Forge NamespacedWrapper.freeze builds streams to check unbound dummy holders. Fast-path iterate
 * and return early when all holders are bound (common case at freeze).
 */
@Mixin(targets = "net.minecraftforge.registries.NamespacedWrapper")
public abstract class NamespacedWrapperFastDummyMixin<T> extends MappedRegistry<T> {

    @Shadow(remap = false)
    private Map<ResourceLocation, Holder.Reference<T>> holdersByName;

    public NamespacedWrapperFastDummyMixin(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
    }

    @Inject(
            method = "freeze",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraftforge/registries/NamespacedWrapper;holdersByName:Ljava/util/Map;",
                    remap = false
            ),
            cancellable = true
    )
    private void omnifix$fastDummyCheck(CallbackInfoReturnable<Registry<T>> cir) {
        for (Holder.Reference<T> ref : this.holdersByName.values()) {
            if (!ref.isBound()) {
                return;
            }
        }
        if (this.unregisteredIntrusiveHolders != null) {
            for (Holder.Reference<T> ref : this.unregisteredIntrusiveHolders.values()) {
                if (ref.getType() == Holder.Reference.Type.INTRUSIVE && !ref.isBound()) {
                    return;
                }
            }
        }
        cir.setReturnValue(this);
    }
}
