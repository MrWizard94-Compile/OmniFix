package org.omnifix.mixin.perf.model;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * All {@link BooleanProperty} instances share the same {false,true} value set — set equality is redundant.
 */
@Mixin(BooleanProperty.class)
public abstract class BooleanPropertyEqualsMixin {

    @Redirect(
            method = "equals",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableSet;equals(Ljava/lang/Object;)Z",
                    remap = false
            ),
            remap = false
    )
    private boolean omnifix$skipSetEquals(ImmutableSet<?> instance, Object object) {
        return true;
    }
}
