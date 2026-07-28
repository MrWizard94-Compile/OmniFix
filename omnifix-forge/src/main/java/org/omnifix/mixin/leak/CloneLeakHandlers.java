package org.omnifix.mixin.leak;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

/**
 * Player-clone leak handlers (Create ExtendoGrip static damage source, Curios cap hygiene,
 * Ars Nouveau cap hygiene, Architectury {@code clientReceivables} re-key, Tool Belt / Traveler's
 * Backpack / Occultism cap hygiene, PneumaticCraft armor UI, Beans Backpacks EnderStorage,
 * AE2WT players map, Forbidden Arcanus cap invalidate, Phosphophyllite ConfigManager players).
 * Uses {@link PlayerEvent.Clone} rather than a {@code restoreFrom} mixin — that method is not a
 * stable inject target across 1.20.1 mapping/remap configurations.
 */
public final class CloneLeakHandlers {

    private static final String ARCHITECTURY_NETWORK_MANAGER =
            "dev.architectury.networking.forge.NetworkManagerImpl";

    private static final String[] ARS_PROBE_CLASSES = {
            "com.hollingsworth.arsnouveau.common.event.EventHandler",
            "com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry",
            "com.hollingsworth.arsnouveau.ArsNouveau"
    };

    private static final String[] TOOLBELT_PROBE = {
            "dev.gigaherz.toolbelt.slot.BeltExtensionSlot",
            "dev.gigaherz.toolbelt.ToolBelt"
    };

    private static final String[] TRAVELERS_BACKPACK_PROBE = {
            "com.tiviacz.travelersbackpack.handlers.ModEventHandler",
            "com.tiviacz.travelersbackpack.handlers.NeoForgeEventHandler",
            "com.tiviacz.travelersbackpack.handlers.ForgeEventHandler",
            "com.tiviacz.travelersbackpack.TravelersBackpack"
    };

    private static final String[] OCCULTISM_PROBE = {
            "com.klikli_dev.occultism.registry.OccultismCapabilities",
            "com.klikli_dev.occultism.Occultism"
    };

    private static final String PNC_ARMOR_MAIN_SCREEN =
            "me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorMainScreen";

    private static final String[] AE2WT_CRAFTING_TERMINAL_HANDLER = {
            "de.mari_023.ae2wtlib.wct.CraftingTerminalHandler",
            "de.mari_023.ae2wtlib.terminal.CraftingTerminalHandler"
    };

    private static final String[] FORBIDDEN_ARCANUS_PROBE = {
            "com.stal111.forbidden_arcanus.common.event.PlayerEvents",
            "com.stal111.forbidden_arcanus.ForbiddenArcanus",
            "com.stal111.forbidden_arcanus.common.ForbiddenArcanus"
    };

    private static final String[] PHOSPHOPHYLLITE_CONFIG_MANAGER = {
            "net.roguelogix.phosphophyllite.config.ConfigManager",
            "net.roguelogix.phosphophyllite.Phosphophyllite"
    };

    private CloneLeakHandlers() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(CloneLeakHandlers.class);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player original = event.getOriginal();
        if (FeatureUnitRegistry.isActive(FeatureUnits.LEAK_CREATE_EXTENDO)) {
            clearExtendoGripStatics();
        }
        if (FeatureUnitRegistry.isActive(FeatureUnits.LEAK_CURIOS_CLONE)) {
            curiosCapHygiene(player);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ARS_NOUVEAU)) {
            arsCapHygiene(player);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ARCHITECTURY)) {
            rekeyArchitecturyClientReceivables(original, player);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_TOOLBELT)) {
            toolbeltCapHygiene(original, player);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_TRAVELERSBACKPACK)) {
            travelersBackpackCapHygiene(original, player);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_OCCULTISM)) {
            occultismCapHygiene(original, player);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_PNC)) {
            clearPncArmorUpgradeOptions();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_BEANSBACKPACKS)) {
            clearBeansBackpacksEnderStorage(original != null ? original.getUUID() : null);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_AE2WT)) {
            removeAe2wtPlayer(original);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_FORBIDDEN_ARCANUS)) {
            forbiddenArcanusCapHygiene(original, event.isWasDeath());
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_PHOSPHOPHYLLITE)) {
            rekeyPhosphophyllitePlayers(original, player);
        }
    }

    /**
     * Soft remove of the original player from {@code CraftingTerminalHandler#players} (server +
     * integrated). Soft-fails when ae2wtlib is absent.
     */
    private static void removeAe2wtPlayer(Player original) {
        if (original == null) {
            return;
        }
        for (String name : AE2WT_CRAFTING_TERMINAL_HANDLER) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    if (!Map.class.isAssignableFrom(f.getType())
                            && !f.getName().equalsIgnoreCase("players")) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Map<?, ?> map) {
                            map.remove(original);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Forbidden Arcanus death-clone path historically omits {@code invalidateCaps} on the original
     * player. Soft-fail when the mod is absent.
     */
    private static void forbiddenArcanusCapHygiene(Player original, boolean wasDeath) {
        if (!wasDeath || original == null) {
            return;
        }
        if (!isAnyClassPresent(FORBIDDEN_ARCANUS_PROBE)) {
            return;
        }
        try {
            original.invalidateCaps();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Re-key Phosphophyllite {@code ConfigManager#players} list: remove original, add new entity.
     */
    private static void rekeyPhosphophyllitePlayers(Player original, Player neu) {
        for (String name : PHOSPHOPHYLLITE_CONFIG_MANAGER) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.equals("players") || n.contains("player"))) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Collection<?> col) {
                            if (original != null) {
                                col.remove(original);
                            }
                            // Add via raw Collection API (list may be ObjectArrayList).
                            if (neu != null) {
                                try {
                                    Method add = Collection.class.getMethod("add", Object.class);
                                    if (!col.contains(neu)) {
                                        add.invoke(col, neu);
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                        } else if (v instanceof Map<?, ?> map && original != null) {
                            Object values = map.remove(original);
                            if (values != null && neu != null) {
                                try {
                                    Method put = Map.class.getMethod("put", Object.class, Object.class);
                                    put.invoke(map, neu, values);
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Soft clear of PneumaticCraft {@code ArmorMainScreen#upgradeOptions} on clone (client and
     * integrated). Soft-fails when the class is absent (dedicated server / mod not installed).
     */
    /**
     * Soft clear of Beans Backpacks {@code EnderStorage#MAP}. Reflection only — safe when the mod
     * is absent. Prefer removing the original player's UUID entry on clone.
     */
    private static void clearBeansBackpacksEnderStorage(java.util.UUID playerUuid) {
        for (String name : new String[]{
                "com.beansgalaxy.backpacks.data.EnderStorage",
                "com.beansgalaxy.backpacks.inventory.EnderStorage"
        }) {
            try {
                Class<?> type = Class.forName(name);
                clearEnderMapOn(type, null, playerUuid);
                Object singleton = null;
                for (String mName : new String[]{"getInstance", "get", "instance"}) {
                    try {
                        Method m = type.getMethod(mName);
                        if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0) {
                            singleton = m.invoke(null);
                            break;
                        }
                    } catch (NoSuchMethodException ignored) {
                    }
                }
                if (singleton != null) {
                    clearEnderMapOn(type, singleton, playerUuid);
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        try {
            Class<?> client = Class.forName("com.beansgalaxy.backpacks.client.network.CommonAtClient");
            Object storage = null;
            for (String mName : new String[]{"getEnderStorage", "enderStorage", "getStorage"}) {
                try {
                    Method m = client.getMethod(mName);
                    if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0) {
                        storage = m.invoke(null);
                        break;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
            if (storage != null) {
                clearEnderMapOn(storage.getClass(), storage, playerUuid);
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void clearEnderMapOn(Class<?> type, Object owner, java.util.UUID playerUuid) {
        for (Field f : type.getDeclaredFields()) {
            if (owner == null && !Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            String n = f.getName();
            if (!(n.equals("MAP") || n.equalsIgnoreCase("map") || n.toLowerCase().contains("map"))) {
                if (!Map.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                if (!n.equals("MAP") && !n.equalsIgnoreCase("map")) {
                    continue;
                }
            }
            try {
                f.setAccessible(true);
                Object v = f.get(owner);
                if (!(v instanceof Map<?, ?> map)) {
                    continue;
                }
                if (playerUuid != null) {
                    map.remove(playerUuid);
                } else {
                    map.clear();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearPncArmorUpgradeOptions() {
        try {
            Class<?> type = Class.forName(PNC_ARMOR_MAIN_SCREEN);
            Object instance = null;
            for (String mName : new String[]{"getInstance", "get", "instance"}) {
                try {
                    Method m = type.getMethod(mName);
                    if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0) {
                        instance = m.invoke(null);
                        break;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
            if (instance == null) {
                Field inst = findStaticField(type, "instance");
                if (inst == null) {
                    inst = findStaticField(type, "INSTANCE");
                }
                if (inst != null) {
                    instance = inst.get(null);
                }
            }
            if (instance == null) {
                return;
            }
            Class<?> c = instance.getClass();
            while (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("upgradeoption") || n.equals("upgradeoptions") || n.contains("options"))) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(instance);
                        if (v instanceof Collection<?> col) {
                            col.clear();
                        } else if (v instanceof Map<?, ?> map) {
                            map.clear();
                        }
                    } catch (Throwable ignored) {
                    }
                }
                c = c.getSuperclass();
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void clearExtendoGripStatics() {
        try {
            Class<?> extendo = Class.forName("com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem");
            for (Field field : extendo.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                String name = field.getName().toLowerCase();
                if (name.contains("damage") || name.contains("lastactive")) {
                    field.setAccessible(true);
                    if (!field.getType().isPrimitive()) {
                        field.set(null, null);
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void curiosCapHygiene(Player player) {
        try {
            Class.forName("top.theillusivec4.curios.common.event.CuriosEventHandler");
            player.invalidateCaps();
            player.reviveCaps();
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable t) {
            try {
                player.invalidateCaps();
                player.reviveCaps();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Ars Nouveau historically called {@code revive()} instead of {@code reviveCaps()} on clone,
     * leaving caps inconsistent and sometimes pinning the prior level. Soft-fail when Ars is absent.
     */
    private static void arsCapHygiene(Player player) {
        if (!isAnyClassPresent(ARS_PROBE_CLASSES)) {
            return;
        }
        try {
            player.invalidateCaps();
            player.reviveCaps();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Tool Belt uses {@code revive()} instead of {@code reviveCaps()} on clone (Forge cap lifecycle
     * mismatch). Invalidate + reviveCaps on both original and new player when Tool Belt is present.
     */
    private static void toolbeltCapHygiene(Player original, Player neu) {
        if (!isAnyClassPresent(TOOLBELT_PROBE)) {
            return;
        }
        reviveCapsHygiene(original);
        reviveCapsHygiene(neu);
    }

    /**
     * Traveler's Backpack historically called {@code revive()} on clone. Force invalidateCaps +
     * reviveCaps so wearable caps do not pin the prior player/level.
     */
    private static void travelersBackpackCapHygiene(Player original, Player neu) {
        if (!isAnyClassPresent(TRAVELERS_BACKPACK_PROBE)) {
            return;
        }
        reviveCapsHygiene(original);
        reviveCapsHygiene(neu);
    }

    /**
     * Occultism copies familiar settings on death clone via {@code reviveCaps} on the original but
     * omits {@code invalidateCaps}. Add full cap hygiene for both entities.
     */
    private static void occultismCapHygiene(Player original, Player neu) {
        if (!isAnyClassPresent(OCCULTISM_PROBE)) {
            return;
        }
        reviveCapsHygiene(original);
        reviveCapsHygiene(neu);
    }

    private static void reviveCapsHygiene(Player player) {
        if (player == null) {
            return;
        }
        try {
            player.invalidateCaps();
            player.reviveCaps();
        } catch (Throwable ignored) {
        }
    }

    /**
     * Architectury keys {@code clientReceivables} by Player identity. On clone the old entity is
     * discarded while the Multimap entry can remain until logout — re-key to the new player.
     */
    private static void rekeyArchitecturyClientReceivables(Player original, Player neu) {
        if (original == null || neu == null || original == neu) {
            return;
        }
        try {
            Class<?> type = Class.forName(ARCHITECTURY_NETWORK_MANAGER);
            Field field = findStaticField(type, "clientReceivables");
            if (field == null) {
                // Any static Multimap / Map field with "receivable" in the name.
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (n.contains("receivable") || n.contains("clientreceiv")) {
                        field = f;
                        field.setAccessible(true);
                        break;
                    }
                }
            }
            if (field == null) {
                return;
            }
            Object multimap = field.get(null);
            if (multimap == null) {
                return;
            }
            if (multimap instanceof Map<?, ?> map) {
                rekeyRawMap(map, original, neu);
                return;
            }
            // Guava Multimap API via reflection (avoid hard dep on Multimap type in our API surface).
            try {
                Method removeAll = multimap.getClass().getMethod("removeAll", Object.class);
                Object removed = removeAll.invoke(multimap, original);
                if (removed instanceof Collection<?> col && !col.isEmpty()) {
                    Method putAll = multimap.getClass().getMethod("putAll", Object.class, Iterable.class);
                    putAll.invoke(multimap, neu, col);
                }
            } catch (NoSuchMethodException e) {
                // Fallback: clear old player only.
                try {
                    Method removeAll = multimap.getClass().getMethod("removeAll", Object.class);
                    removeAll.invoke(multimap, original);
                } catch (Throwable ignored) {
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    /**
     * Reflective re-key for plain Map backends (non-Multimap). Uses Map raw API only via
     * identity ops available on the public Map interface without unchecked casts in callers.
     */
    private static void rekeyRawMap(Map<?, ?> map, Player original, Player neu) {
        try {
            Method remove = Map.class.getMethod("remove", Object.class);
            Object values = remove.invoke(map, original);
            if (values != null) {
                Method put = Map.class.getMethod("put", Object.class, Object.class);
                put.invoke(map, neu, values);
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean isAnyClassPresent(String[] names) {
        for (String name : names) {
            try {
                Class.forName(name);
                return true;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static Field findStaticField(Class<?> type, String name) {
        try {
            Field f = type.getField(name);
            if (Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                return f;
            }
        } catch (NoSuchFieldException ignored) {
        }
        try {
            Field f = type.getDeclaredField(name);
            if (Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                return f;
            }
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }
}
