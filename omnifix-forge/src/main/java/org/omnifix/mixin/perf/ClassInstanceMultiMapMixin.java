package org.omnifix.mixin.perf;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Root cause: every entity section's {@link ClassInstanceMultiMap} uses Guava HashMap + ArrayList
 * for by-class indexes. Entity add/remove/find is hot; fastutil open-hashing reduces overhead.
 *
 * <p>After construction, replace backing collections while preserving the base-class list entry.
 */
@Mixin(ClassInstanceMultiMap.class)
public abstract class ClassInstanceMultiMapMixin<T> {

    @Shadow
    @Final
    @Mutable
    private Map<Class<?>, List<T>> byClass;

    @Shadow
    @Final
    @Mutable
    private List<T> allInstances;

    @Shadow
    @Final
    private Class<T> baseClass;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$useFastutilBackings(Class<T> base, CallbackInfo ci) {
        List<T> fastAll = new ObjectArrayList<>(this.allInstances);
        Map<Class<?>, List<T>> fastByClass = new Object2ObjectOpenHashMap<>();
        // Preserve identity of the base-class list (must equal allInstances reference).
        fastByClass.put(this.baseClass, fastAll);
        for (Map.Entry<Class<?>, List<T>> e : this.byClass.entrySet()) {
            if (e.getKey() == this.baseClass) {
                continue;
            }
            fastByClass.put(e.getKey(), new ObjectArrayList<>(e.getValue()));
        }
        this.allInstances = fastAll;
        this.byClass = fastByClass;
    }
}
