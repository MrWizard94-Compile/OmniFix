package org.omnifix.mixin.perf;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Avoid Optional/lambda allocation on every {@link RegistryObject#get()} (hot path during
 * registry lookups and datapack reload).
 */
@Mixin(value = RegistryObject.class, remap = false)
public abstract class RegistryObjectGetMixin<T> {

    @Shadow
    private @Nullable T value;

    @Shadow
    @Final
    private ResourceLocation name;

    /**
     * @author OmniFix (ModernFix-class)
     * @reason avoid lambda allocation on every call
     */
    @Overwrite
    public T get() {
        T ret = this.value;
        if (ret == null) {
            throw new NullPointerException("Registry Object not present: " + this.name);
        }
        return ret;
    }
}
