package org.omnifix.mixin.bugfix.concurrency;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * Double-checked locking for ForgeRegistryTagManager.getTag to avoid race conditions.
 */
@Mixin(targets = "net/minecraftforge/registries/ForgeRegistryTagManager")
public class ForgeRegistryTagManagerMixin<V> {
    @Shadow
    private volatile Map<TagKey<V>, ITag<V>> tags;

    /**
     * @author embeddedt (issue found by Uncandango) / OmniFix port
     * @reason Forge does not use the correct double-checked locking paradigm, which leads to race conditions
     */
    @WrapMethod(method = "getTag", remap = false)
    private ITag<V> omnifix$getTagSafe(TagKey<V> name, Operation<ITag<V>> original) {
        ITag<V> tag = this.tags.get(name);
        if (tag == null) {
            synchronized (this) {
                tag = original.call(name);
            }
        }
        return tag;
    }
}
