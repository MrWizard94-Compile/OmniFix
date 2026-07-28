package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Memoize {@link CreativeModeTab#buildContents} when display parameters are unchanged.
 * Mods that rebuild tabs early (before search trees) force expensive recomputation; memoizing
 * at this level avoids that without changing CreativeModeTabs orchestration.
 */
@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMemoizeMixin {

    @Shadow
    public abstract CreativeModeTab.Type getType();

    @Unique
    private CreativeModeTab.ItemDisplayParameters omnifix$oldParameters;

    @Unique
    private static boolean omnifix$rebuiltNonCategory;

    @WrapMethod(method = "buildContents")
    private synchronized void omnifix$buildIfChanged(
            CreativeModeTab.ItemDisplayParameters parameters,
            Operation<Void> original
    ) {
        synchronized (CreativeModeTab.class) {
            if (omnifix$oldParameters == null
                    || omnifix$oldParameters.needsUpdate(
                    parameters.enabledFeatures(),
                    parameters.hasPermissions(),
                    parameters.holders())) {
                original.call(parameters);
                if (this.getType() == CreativeModeTab.Type.CATEGORY) {
                    if (omnifix$rebuiltNonCategory) {
                        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                            if (tab.getType() != CreativeModeTab.Type.CATEGORY) {
                                ((CreativeModeTabMemoizeMixin) (Object) tab).omnifix$oldParameters = null;
                            }
                        }
                        omnifix$rebuiltNonCategory = false;
                    }
                } else {
                    omnifix$rebuiltNonCategory = true;
                }
            }
            omnifix$oldParameters = parameters;
        }
    }
}
