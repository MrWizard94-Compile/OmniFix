package org.omnifix.forge.registry;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.ObjectHolderRegistry;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Forge ObjectHolderRegistry retains large throwables and redundant refs that never override.
 * Soft reflection cleanup after register events (ModernFix-class, independent reimplementation).
 */
public final class ObjectHolderClearer {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ObjectHolderClearer() {}

    public static void clearThrowables() {
        Set<Consumer<Predicate<ResourceLocation>>> holders =
                ObfuscationReflectionHelper.getPrivateValue(ObjectHolderRegistry.class, null, "objectHolders");
        if (holders == null) {
            return;
        }
        int numCleared = 0;
        HashMap<Class<?>, Field> throwableField = new HashMap<>();
        Throwable singleton = new Throwable("[OmniFix cleared object-holder stacktrace to save memory]");
        try {
            for (Consumer<Predicate<ResourceLocation>> holder : holders) {
                Field target = throwableField.computeIfAbsent(holder.getClass(), clz -> {
                    for (Field f : clz.getDeclaredFields()) {
                        if (Throwable.class.isAssignableFrom(f.getType())) {
                            f.setAccessible(true);
                            return f;
                        }
                    }
                    return null;
                });
                if (target != null) {
                    target.set(holder, singleton);
                    numCleared++;
                }
            }
        } catch (RuntimeException | ReflectiveOperationException | NoClassDefFoundError ignored) {
        }
        LOGGER.debug("[OmniFix] Cleared {} object holder stacktrace references", numCleared);
    }

    @SuppressWarnings("unchecked")
    public static void removeRedundantHolders() {
        try {
            Field holdersField = ObjectHolderRegistry.class.getDeclaredField("objectHolders");
            holdersField.setAccessible(true);
            Set<Consumer<Predicate<ResourceLocation>>> holders =
                    (Set<Consumer<Predicate<ResourceLocation>>>) holdersField.get(null);

            Class<?> refClass = Class.forName("net.minecraftforge.registries.ObjectHolderRef");
            Field registryField = refClass.getDeclaredField("registry");
            registryField.setAccessible(true);
            Field injectedObjectField = refClass.getDeclaredField("injectedObject");
            injectedObjectField.setAccessible(true);

            Method getOverrideOwnersMethod = ForgeRegistry.class.getDeclaredMethod("getOverrideOwners");
            getOverrideOwnersMethod.setAccessible(true);

            HashMap<ForgeRegistry<?>, Map<ResourceLocation, String>> overrideCache = new HashMap<>();
            int removed = 0;

            var it = holders.iterator();
            while (it.hasNext()) {
                var holder = it.next();
                if (!refClass.isInstance(holder)) {
                    continue;
                }
                ForgeRegistry<?> registry = (ForgeRegistry<?>) registryField.get(holder);
                ResourceLocation injectedObject = (ResourceLocation) injectedObjectField.get(holder);
                Map<ResourceLocation, String> overrides = overrideCache.computeIfAbsent(registry, r -> {
                    try {
                        return (Map<ResourceLocation, String>) getOverrideOwnersMethod.invoke(r);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                });
                if (!overrides.containsKey(injectedObject)) {
                    it.remove();
                    removed++;
                }
            }
            LOGGER.debug("[OmniFix] Removed {} redundant object holders", removed);
            clearThrowables();
        } catch (Exception e) {
            LOGGER.error("[OmniFix] Failed to clean object holders", e);
        }
    }
}
