package org.omnifix.registry;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

/**
 * Compact lifecycle storage: stable() is the default and is not stored per entry.
 */
public final class LifecycleMap<T> extends Reference2ReferenceOpenHashMap<T, Lifecycle> {

    public LifecycleMap() {
        this.defaultReturnValue(Lifecycle.stable());
    }

    @Override
    public Lifecycle put(T t, Lifecycle lifecycle) {
        if (lifecycle != defRetValue) {
            return super.put(t, lifecycle);
        }
        return super.containsKey(t) ? super.get(t) : null;
    }
}
