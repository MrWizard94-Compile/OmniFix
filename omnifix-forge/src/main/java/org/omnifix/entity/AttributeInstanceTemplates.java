package org.omnifix.entity;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

/**
 * Interns identical default {@link AttributeInstance} templates used when building
 * {@link net.minecraft.world.entity.ai.attributes.AttributeSupplier}s. Many entity types share
 * the same base values/modifiers; sharing the template instances cuts memory at scale.
 *
 * <p>Only pure vanilla AttributeInstance objects are interned; subclasses are left alone.
 */
public final class AttributeInstanceTemplates {

    private static final ObjectOpenCustomHashSet<AttributeInstance> INTERNER =
            new ObjectOpenCustomHashSet<>(new Hash.Strategy<>() {
                @Override
                public int hashCode(AttributeInstance o) {
                    if (o == null) {
                        return 0;
                    }
                    int h = System.identityHashCode(o.getAttribute());
                    h = 31 * h + Double.hashCode(o.getBaseValue());
                    h = 31 * h + o.getModifiers().hashCode();
                    return h;
                }

                @Override
                public boolean equals(AttributeInstance a, AttributeInstance b) {
                    if (a == b) {
                        return true;
                    }
                    if (a == null || b == null) {
                        return false;
                    }
                    return a.getAttribute() == b.getAttribute()
                            && a.getBaseValue() == b.getBaseValue()
                            && a.getModifiers().equals(b.getModifiers());
                }
            });

    private AttributeInstanceTemplates() {}

    public static AttributeInstance intern(AttributeInstance a) {
        if (a == null || a.getClass() != AttributeInstance.class) {
            return a;
        }
        synchronized (INTERNER) {
            return INTERNER.addOrGet(a);
        }
    }
}
