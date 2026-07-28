package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import net.minecraft.util.SortedArraySet;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Carpet and similar mods assume a non-null spawn ticket always exists; with spawn chunks
 * removed, null keys can reach SortedArraySet.add.
 */
@Mixin(SortedArraySet.class)
public abstract class SortedArraySetNullGuardMixin<T> {

    @Unique
    private static final Logger OMNIFIX$LOGGER = LogUtils.getLogger();

    @WrapOperation(
            method = "add",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/SortedArraySet;findIndex(Ljava/lang/Object;)I"
            ),
            require = 0
    )
    private int omnifix$checkNull(SortedArraySet<T> instance, T object, Operation<Integer> original) {
        if (object == null) {
            OMNIFIX$LOGGER.error("[OmniFix] Attempted to insert a null key into SortedArraySet, ignoring");
            return 0;
        }
        return original.call(instance, object);
    }
}
