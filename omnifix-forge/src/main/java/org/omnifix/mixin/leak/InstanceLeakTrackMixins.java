package org.omnifix.mixin.leak;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optional-mod instance registration for {@link InstanceLeakRegistry}.
 * All targets are {@link Pseudo} — absent mods simply never apply these mixins.
 */
public final class InstanceLeakTrackMixins {
    private InstanceLeakTrackMixins() {}

    @Pseudo
    @Mixin(targets = "org.cyclops.cyclopscore.client.model.DelegatingDynamicItemAndBlockModel", remap = false)
    public static abstract class CyclopsDynamicModelMixin {
        @Inject(method = "<init>*", at = @At("RETURN"), require = 0)
        private void omnifix$track(CallbackInfo ci) {
            InstanceLeakRegistry.track(InstanceLeakRegistry.Kind.CYCLOPS_MODEL_WORLD, this);
        }
    }

    @Pseudo
    @Mixin(targets = "fzzyhmstrs.emi_loot.util.EntityEmiStack", remap = false)
    public static abstract class EmiLootEntityStackMixin {
        @Inject(method = "<init>*", at = @At("RETURN"), require = 0)
        private void omnifix$track(CallbackInfo ci) {
            InstanceLeakRegistry.track(InstanceLeakRegistry.Kind.EMI_LOOT_ENTITY, this);
        }
    }

    @Pseudo
    @Mixin(targets = "com.lowdragmc.lowdraglib.gui.modular.ModularUI", remap = false)
    public static abstract class LdLibModularUiMixin {
        @Inject(method = "<init>*", at = @At("RETURN"), require = 0)
        private void omnifix$track(CallbackInfo ci) {
            InstanceLeakRegistry.track(InstanceLeakRegistry.Kind.LDLIB_MODULAR_UI_PLAYER, this);
        }
    }
}
