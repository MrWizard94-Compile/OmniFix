package org.omnifix.mixin.leak;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Soft registry for optional-mod instances that pin {@link ClientLevel}/{@link LocalPlayer}
 * (ATL UpdateableLevel / UpdateablePlayer class of leaks). Mixins register weak refs; leave/login
 * handlers clear or retarget fields via reflection so OmniFix never hard-depends on those mods.
 */
public final class InstanceLeakRegistry {

    public enum Kind {
        CYCLOPS_MODEL_WORLD,
        EMI_LOOT_ENTITY,
        LDLIB_MODULAR_UI_PLAYER
    }

    private static final Map<Kind, List<WeakReference<Object>>> TRACKED = new ConcurrentHashMap<>();

    private InstanceLeakRegistry() {}

    public static void track(Kind kind, Object instance) {
        if (instance == null) {
            return;
        }
        TRACKED.computeIfAbsent(kind, k -> new ArrayList<>()).add(new WeakReference<>(instance));
    }

    /** Null world/entity or rebind player after client level leave / logout. */
    public static void onClientLevelLeave() {
        sweep(Kind.CYCLOPS_MODEL_WORLD, o -> setFieldIfPresent(o, "world", Level.class, null));
        sweep(Kind.EMI_LOOT_ENTITY, o -> setFieldIfPresent(o, "entity", Entity.class, null));
        // ModularUI keeps last player; nulling avoids pinning dead LocalPlayer across logout.
        sweep(Kind.LDLIB_MODULAR_UI_PLAYER, o -> setFieldIfPresent(o, "entityPlayer", Player.class, null));
    }

    /** After a new ClientLevel/player exists, retarget surviving instances. */
    public static void onClientLevelReady(ClientLevel level, LocalPlayer player) {
        if (level != null) {
            sweep(Kind.CYCLOPS_MODEL_WORLD, o -> setFieldIfPresent(o, "world", Level.class, level));
            sweep(Kind.EMI_LOOT_ENTITY, o -> recreateEmiLootEntity(o, level));
        }
        if (player != null) {
            sweep(Kind.LDLIB_MODULAR_UI_PLAYER, o -> setFieldIfPresent(o, "entityPlayer", Player.class, player));
        }
    }

    private static void recreateEmiLootEntity(Object stack, ClientLevel level) {
        try {
            Field f = findField(stack.getClass(), "entity");
            if (f == null) {
                return;
            }
            f.setAccessible(true);
            Object old = f.get(stack);
            if (!(old instanceof Entity oldEntity)) {
                return;
            }
            Entity neu = oldEntity.getType().create(level);
            if (neu == null) {
                f.set(stack, null);
                return;
            }
            if (oldEntity instanceof Sheep sheep && neu instanceof Sheep ns) {
                ns.setColor(sheep.getColor());
            }
            if (oldEntity instanceof Slime slime && neu instanceof Slime nsl) {
                nsl.setSize(slime.getSize(), false);
            }
            f.set(stack, neu);
        } catch (Throwable ignored) {
            // soft-fail
        }
    }

    private static void setFieldIfPresent(Object target, String name, Class<?> type, Object value) {
        try {
            Field f = findField(target.getClass(), name);
            if (f == null) {
                return;
            }
            if (!type.isAssignableFrom(f.getType()) && value != null) {
                return;
            }
            f.setAccessible(true);
            if (Modifier.isFinal(f.getModifiers())) {
                try {
                    Field mods = Field.class.getDeclaredField("modifiers");
                    mods.setAccessible(true);
                    mods.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                } catch (Throwable ignored) {
                    // Java 12+ may block; still try set
                }
            }
            f.set(target, value);
        } catch (Throwable ignored) {
            // soft-fail
        }
    }

    private static Field findField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private static void sweep(Kind kind, java.util.function.Consumer<Object> action) {
        List<WeakReference<Object>> list = TRACKED.get(kind);
        if (list == null) {
            return;
        }
        synchronized (list) {
            Iterator<WeakReference<Object>> it = list.iterator();
            while (it.hasNext()) {
                Object o = it.next().get();
                if (o == null) {
                    it.remove();
                    continue;
                }
                action.accept(o);
            }
        }
    }
}
