package org.omnifix.mixin.perf;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import org.omnifix.registry.LifecycleMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryLifecycleMixin<T> {

    @Shadow
    @Final
    @Mutable
    private Map<T, Lifecycle> lifecycles;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$replaceLifecycleStorage(CallbackInfo ci) {
        this.lifecycles = new LifecycleMap<>();
    }
}
