package org.omnifix.mixin.perf;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import net.minecraft.util.datafix.DataFixers;
import org.omnifix.dfu.LazyDataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(DataFixers.class)
public abstract class DataFixersLazyMixin {

    @Shadow
    protected static DataFixer createFixerUpper(Set<DSL.TypeReference> set) {
        throw new AssertionError();
    }

    @Unique
    private static LazyDataFixer omnifix$lazyDataFixer;

    @Inject(method = "createFixerUpper", at = @At("HEAD"), cancellable = true)
    private static void omnifix$createLazyFixerUpper(
            Set<DSL.TypeReference> set,
            CallbackInfoReturnable<DataFixer> cir
    ) {
        if (omnifix$lazyDataFixer == null) {
            omnifix$lazyDataFixer = new LazyDataFixer(() -> createFixerUpper(set));
            cir.setReturnValue(omnifix$lazyDataFixer);
        }
    }
}
