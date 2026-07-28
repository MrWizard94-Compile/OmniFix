package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VanillaRegistries.class)
public abstract class VanillaRegistriesMemoizeMixin {

    @Unique
    private static HolderLookup.Provider omnifix$staticProvider;

    @WrapMethod(method = "createLookup")
    private static HolderLookup.Provider omnifix$memoizeLookup(Operation<HolderLookup.Provider> original) {
        synchronized (VanillaRegistries.class) {
            if (omnifix$staticProvider == null) {
                omnifix$staticProvider = original.call();
            }
            return omnifix$staticProvider;
        }
    }
}
