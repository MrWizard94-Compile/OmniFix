package org.omnifix.mixin.bugfix.registry_ops_cme;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replaces the RegistryOps memoized lookup map with a ConcurrentHashMap to avoid CME under concurrent access.
 *
 * <p>Reflection is used instead of {@code @Shadow}: Mixin AP 0.8.5 crashes with
 * {@code StringIndexOutOfBoundsException} when validating the nested-wildcard {@code lookups} field
 * on the anonymous {@code RegistryOps$1} target (ModernFix uses an identical shadow that only works
 * under their multi-loader AP configuration).
 */
@Mixin(targets = "net.minecraft.resources.RegistryOps$1")
public class RegistryOpsMemoizedMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$useConcurrentMap(CallbackInfo ci) {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || !Map.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object current = field.get(this);
                if (current instanceof Map<?, ?> map && !(current instanceof ConcurrentHashMap)) {
                    field.set(this, new ConcurrentHashMap<>(map));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("OmniFix failed to make RegistryOps memo map concurrent", e);
            }
            return;
        }
    }
}
