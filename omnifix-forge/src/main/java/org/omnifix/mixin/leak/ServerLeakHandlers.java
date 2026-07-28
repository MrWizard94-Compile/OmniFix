package org.omnifix.mixin.leak;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Server-side optional-mod leak hygiene (Create Crafts &amp; Additions energy networks,
 * Ars Nouveau caster-tome registry, Citadel server data, Alex's Mobs world maps, Serene Seasons
 * season maps, Aether dropped-item owners, PneumaticCraft drone bus unregister, AE2WT players,
 * badpackets handlers, Iceberg collector, Phosphophyllite ticking map, Railcraft charge nets,
 * Small Ships ChunkMap fields, vanilla last-damage tick). Reflection + Forge events only — no
 * hard {@code @Mixin} on optional classes.
 *
 * <p>Symptom catalog: AllTheLeaks rows (independent reimplementation).
 */
public final class ServerLeakHandlers {

    private static final String ENERGY_NETWORK_MANAGER =
            "com.mrh0.createaddition.energy.network.EnergyNetworkManager";

    private static final String[] CASTER_TOME_REGISTRY = {
            "com.hollingsworth.arsnouveau.api.registry.CasterTomeRegistry",
            "com.hollingsworth.arsnouveau.setup.registry.CasterTomeRegistry",
            "com.hollingsworth.arsnouveau.common.items.CasterTomeRegistry",
            "com.hollingsworth.arsnouveau.api.loot.CasterTomeRegistry"
    };

    private static final String[] DUNGEON_LOOT_TABLES = {
            "com.hollingsworth.arsnouveau.api.loot.DungeonLootTables",
            "com.hollingsworth.arsnouveau.common.loot.DungeonLootTables"
    };

    private static final String[] CITADEL_SERVER_DATA = {
            "com.github.alexthe666.citadel.server.world.CitadelServerData",
            "com.github.alexthe666.citadel.server.CitadelServerData"
    };

    private static final String ALEXS_SERVER_EVENTS =
            "com.github.alexthe666.alexsmobs.event.ServerEvents";
    private static final String ALEXS_WORLD_DATA =
            "com.github.alexthe666.alexsmobs.world.AMWorldData";

    private static final String[] SERENE_SEASON_HANDLER = {
            "sereneseasons.season.SeasonHandler",
            "sereneseasons.handler.season.SeasonHandler"
    };

    private static final String[] AETHER_DROPPED_ITEM = {
            "com.aetherteam.aether.capability.item.DroppedItem",
            "com.aetherteam.aether.capability.item.DroppedItemCapability"
    };
    private static final String AETHER_CAPABILITIES =
            "com.aetherteam.aether.capability.AetherCapabilities";

    private static final String[] PNC_DRONE_ENTITY = {
            "me.desht.pneumaticcraft.common.entity.drone.DroneEntity",
            "me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity",
            "me.desht.pneumaticcraft.common.entity.living.EntityDrone"
    };

    private static final String[] AE2WT_CRAFTING_TERMINAL_HANDLER = {
            "de.mari_023.ae2wtlib.wct.CraftingTerminalHandler",
            "de.mari_023.ae2wtlib.terminal.CraftingTerminalHandler"
    };

    private static final String[] BADPACKETS_CHANNEL_REGISTRY = {
            "lol.bai.badpackets.impl.registry.ChannelRegistry"
    };

    private static final String[] ICEBERG_ENTITY_COLLECTOR = {
            "com.anthonyhilyard.iceberg.util.EntityCollector",
            "com.anthonyhilyard.iceberg.EntityCollector"
    };

    private static final String[] PHOS_CONFIG_MANAGER = {
            "net.roguelogix.phosphophyllite.config.ConfigManager"
    };
    private static final String[] PHOS_TICKING_TRACKER = {
            "net.roguelogix.phosphophyllite.modular.tile.IIsTickingTracker$Module",
            "net.roguelogix.phosphophyllite.modular.tile.IIsTickingTracker.Module"
    };

    private static final String[] RAILCRAFT_CHARGE_PROVIDER = {
            "mods.railcraft.charge.ChargeProviderImpl",
            "mods.railcraft.api.charge.Charge"
    };

    private ServerLeakHandlers() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ServerLeakHandlers.class);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();
        if (level == null) {
            return;
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_CREATEADDITION)) {
            removeEnergyNetworkLevel(level);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ALEXSMOBS)) {
            removeAlexsMobsLevelMaps(level);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_SERENESEASONS)) {
            removeSereneSeasonsLevelMaps(level);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_AETHER)) {
            // Soft: clear any static entity maps on DroppedItemCapability / related holders.
            clearAetherDroppedItemStaticMaps();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ICEBERG)) {
            removeIcebergLevel(level);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_RAILCRAFT)) {
            removeRailcraftChargeNetwork(level);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_SMALLSHIPS)) {
            clearSmallshipsChunkMapFields(level);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_AETHER) && entity instanceof ItemEntity item) {
            clearAetherDroppedItemOwner(item);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_PNC)) {
            unregisterPncDroneIfPresent(entity);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_AE2WT)) {
            removeAe2wtPlayer(player);
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_PHOSPHOPHYLLITE)) {
            removePhosphophyllitePlayer(player);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_MC_VANILLA)) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        // Soft stand-in for ATL: poke lastDamageSource on a slow cadence so removed-entity
        // sources can be dropped by vanilla's internal last-damage expiry path.
        if (entity.tickCount % 41 == 0) {
            try {
                entity.getLastDamageSource();
            } catch (Throwable ignored) {
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_CREATEADDITION)) {
            clearAllEnergyNetworks();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ARS_NOUVEAU)) {
            clearCasterTomeRegistry();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_CITADEL)) {
            clearCitadelServerData();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ALEXSMOBS)) {
            clearAllAlexsMobsLevelMaps();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_SERENESEASONS)) {
            clearAllSereneSeasonsLevelMaps();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_AETHER)) {
            clearAetherDroppedItemStaticMaps();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_AE2WT)) {
            clearAe2wtPlayers();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_BADPACKETS)) {
            clearBadpacketsHandlers();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_PHOSPHOPHYLLITE)) {
            clearPhosphophylliteTickingMap();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_SMALLSHIPS) && event.getServer() != null) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                clearSmallshipsChunkMapFields(level);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_AE2WT)) {
            clearAe2wtPlayers();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_BADPACKETS)) {
            clearBadpacketsHandlers();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_PHOSPHOPHYLLITE)) {
            clearPhosphophylliteTickingMap();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ICEBERG)) {
            clearIcebergAllLevels();
        }
    }

    // -------------------------------------------------------------------------
    // AE2WT — CraftingTerminalHandler.players
    // -------------------------------------------------------------------------

    private static void removeAe2wtPlayer(Player player) {
        Map<?, ?> players = getAe2wtPlayersMap();
        if (players == null) {
            return;
        }
        try {
            players.remove(player);
        } catch (Throwable ignored) {
        }
    }

    private static void clearAe2wtPlayers() {
        Map<?, ?> players = getAe2wtPlayersMap();
        if (players == null) {
            return;
        }
        try {
            players.clear();
        } catch (Throwable ignored) {
        }
    }

    private static Map<?, ?> getAe2wtPlayersMap() {
        for (String name : AE2WT_CRAFTING_TERMINAL_HANDLER) {
            try {
                Class<?> type = Class.forName(name);
                Field f = findStaticField(type, "players");
                if (f == null) {
                    for (Field field : type.getDeclaredFields()) {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        if (Map.class.isAssignableFrom(field.getType())
                                || field.getName().equalsIgnoreCase("players")) {
                            f = field;
                            f.setAccessible(true);
                            break;
                        }
                    }
                }
                if (f == null) {
                    continue;
                }
                Object v = f.get(null);
                if (v instanceof Map<?, ?> map) {
                    return map;
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // badpackets — ChannelRegistry.handlers
    // -------------------------------------------------------------------------

    private static void clearBadpacketsHandlers() {
        for (String name : BADPACKETS_CHANNEL_REGISTRY) {
            try {
                Class<?> type = Class.forName(name);
                for (String channelName : new String[]{"C2S", "S2C", "c2s", "s2c"}) {
                    Field channelField = findStaticField(type, channelName);
                    if (channelField == null) {
                        continue;
                    }
                    Object channel = channelField.get(null);
                    if (channel == null) {
                        continue;
                    }
                    clearNamedCollectionOrMap(channel, "handlers", "handler", "receivers");
                }
                // Static handler collections fallback.
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("handler") || n.contains("channel"))) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Collection<?> col) {
                            col.clear();
                        } else if (v instanceof Map<?, ?> map) {
                            map.clear();
                        } else if (v != null) {
                            clearNamedCollectionOrMap(v, "handlers", "handler");
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearNamedCollectionOrMap(Object owner, String... nameHints) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (f.getType().isPrimitive()) {
                    continue;
                }
                if (!fieldNameMatches(f.getName(), nameHints)) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(owner);
                    if (v instanceof Collection<?> col) {
                        col.clear();
                    } else if (v instanceof Map<?, ?> map) {
                        map.clear();
                    }
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // Iceberg — EntityCollector.wrappedLevelsMap (client unload also hits here)
    // -------------------------------------------------------------------------

    private static void removeIcebergLevel(LevelAccessor level) {
        for (String name : ICEBERG_ENTITY_COLLECTOR) {
            try {
                Class<?> type = Class.forName(name);
                removeFromTypeMaps(type, level, "wrappedLevelsMap", "wrapped", "levels", "map");
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearIcebergAllLevels() {
        for (String name : ICEBERG_ENTITY_COLLECTOR) {
            try {
                Class<?> type = Class.forName(name);
                clearTypeMaps(type, "wrappedLevelsMap", "wrapped", "levels", "map");
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Phosphophyllite — ConfigManager.players + isTickingMap
    // -------------------------------------------------------------------------

    private static void removePhosphophyllitePlayer(Player player) {
        for (String name : PHOS_CONFIG_MANAGER) {
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
                            col.remove(player);
                        } else if (v instanceof Map<?, ?> map) {
                            map.remove(player);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearPhosphophylliteTickingMap() {
        for (String name : PHOS_TICKING_TRACKER) {
            try {
                Class<?> type = Class.forName(name);
                clearTypeMaps(type, "isTickingMap", "ticking", "map");
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Map<?, ?> map) {
                            map.clear();
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        // Also soft-clear ConfigManager players list on full stop.
        for (String name : PHOS_CONFIG_MANAGER) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    if (!f.getName().toLowerCase().contains("player")) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Collection<?> col) {
                            col.clear();
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Railcraft — ChargeProviderImpl.DISTRIBUTION.networks
    // -------------------------------------------------------------------------

    private static void removeRailcraftChargeNetwork(LevelAccessor level) {
        if (level.isClientSide()) {
            return;
        }
        for (String name : RAILCRAFT_CHARGE_PROVIDER) {
            try {
                Class<?> type = Class.forName(name);
                // DISTRIBUTION (or similar) static instance with networks map.
                for (Field distField : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(distField.getModifiers()) || distField.getType().isPrimitive()) {
                        continue;
                    }
                    String dn = distField.getName().toLowerCase();
                    if (!(dn.contains("distribution") || dn.contains("charge") || dn.equals("instance")
                            || dn.contains("network") || dn.contains("provider"))) {
                        // Still try nested enum constants / known names.
                        if (!dn.equals("distribution") && !Modifier.isFinal(distField.getModifiers())) {
                            continue;
                        }
                    }
                    try {
                        distField.setAccessible(true);
                        Object dist = distField.get(null);
                        if (dist == null) {
                            continue;
                        }
                        // networks map on the distribution instance.
                        Class<?> dType = dist.getClass();
                        while (dType != null && dType != Object.class) {
                            for (Field nf : dType.getDeclaredFields()) {
                                if (nf.getType().isPrimitive()) {
                                    continue;
                                }
                                String nn = nf.getName().toLowerCase();
                                if (!(nn.contains("network") || Map.class.isAssignableFrom(nf.getType()))) {
                                    continue;
                                }
                                if (!nn.contains("network") && !nn.equals("networks")) {
                                    continue;
                                }
                                try {
                                    nf.setAccessible(true);
                                    Object v = nf.get(dist);
                                    if (v instanceof Map<?, ?> map) {
                                        map.remove(level);
                                        // Also remove by identity/equals for Level vs LevelAccessor.
                                        Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
                                        while (it.hasNext()) {
                                            Map.Entry<?, ?> e = it.next();
                                            Object key = e.getKey();
                                            if (key == level || (key != null && key.equals(level))) {
                                                it.remove();
                                            }
                                        }
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                            dType = dType.getSuperclass();
                        }
                    } catch (Throwable ignored) {
                    }
                }
                // Fallback: static map named networks on the class itself.
                removeFromTypeMaps(type, level, "networks", "network");
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Small Ships — ChunkMap.serverPlayer / list mixin residuals
    // -------------------------------------------------------------------------

    private static void clearSmallshipsChunkMapFields(LevelAccessor level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // Only touch ChunkMap when Small Ships is present — vanilla may use generic field names.
        if (!isSmallshipsPresent()) {
            return;
        }
        try {
            ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
            nullAndClearChunkMapMixinFields(chunkMap);
        } catch (Throwable ignored) {
        }
    }

    private static boolean isSmallshipsPresent() {
        for (String name : new String[]{
                "com.talhanation.smallships.SmallShipsMod",
                "com.talhanation.smallships.SmallshipsMod",
                "com.talhanation.smallships.Main",
                "com.talhanation.smallships.init.ModEntityTypes",
                "com.talhanation.smallships.world.entity.ship.Ship"
        }) {
            try {
                Class.forName(name);
                return true;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static void nullAndClearChunkMapMixinFields(ChunkMap chunkMap) {
        if (chunkMap == null) {
            return;
        }
        Class<?> type = chunkMap.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName();
                // ATL targets smallships-injected ChunkMap#serverPlayer / #list only.
                if (n.equals("serverPlayer") || n.equals("smallships$serverPlayer")) {
                    try {
                        f.setAccessible(true);
                        // Only null when the type looks like a player.
                        if (Player.class.isAssignableFrom(f.getType())
                                || f.getType().getName().contains("Player")
                                || f.getType() == Object.class) {
                            f.set(chunkMap, null);
                        }
                    } catch (Throwable ignored) {
                    }
                } else if (n.equals("list") || n.equals("smallships$list")) {
                    try {
                        f.setAccessible(true);
                        Object v = f.get(chunkMap);
                        // Only clear list-like values that smallships uses as a temporary scratch list.
                        if (v instanceof List<?> list && list.size() <= 64) {
                            list.clear();
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
            type = type.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // Create Crafts & Additions — EnergyNetworkManager.instances
    // -------------------------------------------------------------------------

    private static void removeEnergyNetworkLevel(LevelAccessor level) {
        Map<?, ?> instances = getEnergyNetworkInstances();
        if (instances == null || instances.isEmpty()) {
            return;
        }
        try {
            instances.remove(level);
        } catch (Throwable ignored) {
        }
        // Some builds key by ServerLevel / Level rather than the Unload LevelAccessor identity.
        try {
            Iterator<? extends Map.Entry<?, ?>> it = instances.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> e = it.next();
                Object key = e.getKey();
                if (key == level || (key != null && key.equals(level))) {
                    it.remove();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void clearAllEnergyNetworks() {
        Map<?, ?> instances = getEnergyNetworkInstances();
        if (instances == null) {
            return;
        }
        try {
            instances.clear();
        } catch (Throwable ignored) {
        }
    }

    private static Map<?, ?> getEnergyNetworkInstances() {
        try {
            Class<?> type = Class.forName(ENERGY_NETWORK_MANAGER);
            Field instancesField = findStaticField(type, "instances");
            if (instancesField == null) {
                // Fallback: any static Map field.
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    if (!Map.class.isAssignableFrom(f.getType())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object v = f.get(null);
                    if (v instanceof Map<?, ?> map) {
                        return map;
                    }
                }
                return null;
            }
            Object v = instancesField.get(null);
            return v instanceof Map<?, ?> map ? map : null;
        } catch (ClassNotFoundException ignored) {
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Ars Nouveau — CasterTomeRegistry level-capturing suppliers
    // -------------------------------------------------------------------------

    private static void clearCasterTomeRegistry() {
        boolean found = false;
        for (String name : CASTER_TOME_REGISTRY) {
            try {
                Class<?> type = Class.forName(name);
                clearStaticCollections(type);
                found = true;
                break;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        if (!found) {
            // Soft probe: any class named CasterTomeRegistry under ars package is unlikely offline.
            return;
        }
        for (String name : DUNGEON_LOOT_TABLES) {
            try {
                Class<?> type = Class.forName(name);
                clearStaticFieldNamed(type, "CASTER_TOMES");
                clearStaticCollectionsMatching(type, "tome");
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearStaticCollections(Class<?> type) {
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                continue;
            }
            try {
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof Collection<?> col) {
                    col.clear();
                } else if (v instanceof Map<?, ?> map) {
                    map.clear();
                } else if (v instanceof Iterable<?> it && !(v instanceof CharSequence)) {
                    // Immutable wrappers: replace with empty list when field is mutable ref.
                    String n = f.getName().toLowerCase();
                    if (n.contains("tome") || n.contains("data") || n.contains("recipe")) {
                        tryClearOrNull(f, v);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearStaticCollectionsMatching(Class<?> type, String nameHint) {
        String hint = nameHint.toLowerCase();
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (!f.getName().toLowerCase().contains(hint)) {
                continue;
            }
            try {
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof Collection<?> col) {
                    col.clear();
                } else if (v instanceof Map<?, ?> map) {
                    map.clear();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearStaticFieldNamed(Class<?> type, String name) {
        try {
            Field f = findStaticField(type, name);
            if (f == null) {
                return;
            }
            Object v = f.get(null);
            if (v instanceof Collection<?> col) {
                col.clear();
            } else if (v instanceof Map<?, ?> map) {
                map.clear();
            }
        } catch (Throwable ignored) {
        }
    }

    private static void tryClearOrNull(Field f, Object value) {
        try {
            Method clear = value.getClass().getMethod("clear");
            clear.invoke(value);
        } catch (Throwable t) {
            try {
                if (!Modifier.isFinal(f.getModifiers())) {
                    f.set(null, null);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Alex's Mobs — BEACHED_CACHALOT_WHALE_SPAWNER_MAP + AMWorldData.dataMap
    // -------------------------------------------------------------------------

    private static void removeAlexsMobsLevelMaps(LevelAccessor level) {
        removeFromStaticMap(ALEXS_SERVER_EVENTS, level, "BEACHED_CACHALOT_WHALE_SPAWNER_MAP", "beached", "cachalot", "spawner");
        removeFromStaticMap(ALEXS_WORLD_DATA, level, "dataMap", "datamap", "data_map");
    }

    private static void clearAllAlexsMobsLevelMaps() {
        clearStaticMap(ALEXS_SERVER_EVENTS, "BEACHED_CACHALOT_WHALE_SPAWNER_MAP", "beached", "cachalot", "spawner");
        clearStaticMap(ALEXS_WORLD_DATA, "dataMap", "datamap", "data_map");
    }

    // -------------------------------------------------------------------------
    // Serene Seasons — SeasonHandler.updateTicks / lastDayTimes
    // -------------------------------------------------------------------------

    private static void removeSereneSeasonsLevelMaps(LevelAccessor level) {
        if (level.isClientSide()) {
            return;
        }
        for (String name : SERENE_SEASON_HANDLER) {
            try {
                Class<?> type = Class.forName(name);
                removeFromTypeMaps(type, level, "updateTicks", "lastDayTimes", "updateticks", "lastdaytimes");
                return;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearAllSereneSeasonsLevelMaps() {
        for (String name : SERENE_SEASON_HANDLER) {
            try {
                Class<?> type = Class.forName(name);
                clearTypeMaps(type, "updateTicks", "lastDayTimes", "updateticks", "lastdaytimes");
                return;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Aether — DroppedItemCapability owner entity residual
    // -------------------------------------------------------------------------

    /**
     * Soft stand-in for ATL's UUID-based owner rewrite: when an ItemEntity leaves the level, null
     * its DroppedItem capability owner so the cap cannot pin a prior Entity/Level. Also clears any
     * static entity maps if present across versions.
     */
    private static void clearAetherDroppedItemOwner(ItemEntity item) {
        try {
            // DroppedItem.get(item) -> LazyOptional; setOwner(null).
            for (String iface : AETHER_DROPPED_ITEM) {
                try {
                    Class<?> type = Class.forName(iface);
                    Method get = null;
                    try {
                        get = type.getMethod("get", ItemEntity.class);
                    } catch (NoSuchMethodException e) {
                        try {
                            get = type.getMethod("get", Entity.class);
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                    if (get != null && Modifier.isStatic(get.getModifiers())) {
                        Object opt = get.invoke(null, item);
                        if (opt instanceof LazyOptional<?> lazy) {
                            lazy.ifPresent(cap -> invokeSetOwnerNull(cap));
                        } else if (opt != null) {
                            invokeSetOwnerNull(opt);
                        }
                        return;
                    }
                } catch (ClassNotFoundException ignored) {
                } catch (Throwable ignored) {
                }
            }
            // Capability token path: item.getCapability(AetherCapabilities.DROPPED_ITEM_CAPABILITY)
            try {
                Class<?> caps = Class.forName(AETHER_CAPABILITIES);
                Field capField = findStaticField(caps, "DROPPED_ITEM_CAPABILITY");
                if (capField == null) {
                    for (Field f : caps.getDeclaredFields()) {
                        if (!Modifier.isStatic(f.getModifiers())) {
                            continue;
                        }
                        if (f.getName().toLowerCase().contains("dropped")) {
                            capField = f;
                            capField.setAccessible(true);
                            break;
                        }
                    }
                }
                if (capField != null) {
                    Object capability = capField.get(null);
                    Method getCapability = Entity.class.getMethod("getCapability",
                            Class.forName("net.minecraftforge.common.capabilities.Capability"));
                    Object opt = getCapability.invoke(item, capability);
                    if (opt instanceof LazyOptional<?> lazy) {
                        lazy.ifPresent(ServerLeakHandlers::invokeSetOwnerNull);
                    }
                }
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
    }

    private static void invokeSetOwnerNull(Object cap) {
        try {
            Method setOwner = cap.getClass().getMethod("setOwner", Entity.class);
            setOwner.invoke(cap, new Object[]{null});
            return;
        } catch (Throwable ignored) {
        }
        // Direct field null.
        Class<?> type = cap.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                if (!(n.equals("owner") || n.contains("owner"))) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    f.set(cap, null);
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    private static void clearAetherDroppedItemStaticMaps() {
        for (String name : AETHER_DROPPED_ITEM) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Map<?, ?> map
                                && (n.contains("entity") || n.contains("owner") || n.contains("map")
                                || n.contains("item") || n.contains("cache"))) {
                            map.clear();
                        } else if (v instanceof Collection<?> col
                                && (n.contains("entity") || n.contains("owner") || n.contains("item"))) {
                            col.clear();
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // PneumaticCraft — unregister drone entities from Forge bus on leave
    // -------------------------------------------------------------------------

    /**
     * Soft stand-in for ATL's {@code onRemovedFromWorld} mixin: when a drone entity leaves the
     * world, unregister it from {@link MinecraftForge#EVENT_BUS} so server listeners cannot pin it.
     */
    private static void unregisterPncDroneIfPresent(Entity entity) {
        if (entity.level() != null && entity.level().isClientSide()) {
            return;
        }
        String cn = entity.getClass().getName();
        if (!(cn.contains("pneumaticcraft") && (cn.toLowerCase().contains("drone")
                || cn.contains("ProgrammableController") || cn.contains("AerialInterface")))) {
            // Also accept exact probe class types.
            boolean match = false;
            for (String name : PNC_DRONE_ENTITY) {
                try {
                    if (Class.forName(name).isInstance(entity)) {
                        match = true;
                        break;
                    }
                } catch (ClassNotFoundException ignored) {
                } catch (Throwable ignored) {
                }
            }
            if (!match) {
                return;
            }
        }
        try {
            MinecraftForge.EVENT_BUS.unregister(entity);
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Shared map helpers
    // -------------------------------------------------------------------------

    private static void removeFromStaticMap(String className, LevelAccessor level, String... nameHints) {
        try {
            Class<?> type = Class.forName(className);
            removeFromTypeMaps(type, level, nameHints);
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void clearStaticMap(String className, String... nameHints) {
        try {
            Class<?> type = Class.forName(className);
            clearTypeMaps(type, nameHints);
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void removeFromTypeMaps(Class<?> type, LevelAccessor level, String... nameHints) {
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (!fieldNameMatches(f.getName(), nameHints)) {
                continue;
            }
            try {
                f.setAccessible(true);
                Object v = f.get(null);
                if (!(v instanceof Map<?, ?> map)) {
                    continue;
                }
                try {
                    map.remove(level);
                } catch (Throwable ignored) {
                }
                try {
                    Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<?, ?> e = it.next();
                        Object key = e.getKey();
                        if (key == level || (key != null && key.equals(level))) {
                            it.remove();
                        }
                    }
                } catch (Throwable ignored) {
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearTypeMaps(Class<?> type, String... nameHints) {
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (!fieldNameMatches(f.getName(), nameHints)) {
                continue;
            }
            try {
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof Map<?, ?> map) {
                    map.clear();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static boolean fieldNameMatches(String fieldName, String... hints) {
        String n = fieldName.toLowerCase();
        for (String hint : hints) {
            if (fieldName.equals(hint) || n.equals(hint.toLowerCase()) || n.contains(hint.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Citadel — server data / tick tracker static maps (version-soft)
    // -------------------------------------------------------------------------

    /**
     * Clears any static server/level maps on {@code CitadelServerData} (historical {@code dataMap}
     * layout) and nulls tick-rate tracker fields that retain a server after stop.
     */
    private static void clearCitadelServerData() {
        for (String name : CITADEL_SERVER_DATA) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Map<?, ?> map
                                && (n.contains("data") || n.contains("map") || n.contains("server")
                                || n.contains("level") || n.contains("world"))) {
                            map.clear();
                        } else if (v instanceof Collection<?> col
                                && (n.contains("data") || n.contains("server"))) {
                            col.clear();
                        } else if (v != null && (n.contains("tracker") || n.contains("instance")
                                || n.contains("server") || n.contains("datamap"))) {
                            // Prefer clear() then null when non-final.
                            try {
                                Method clear = v.getClass().getMethod("clear");
                                clear.invoke(v);
                            } catch (Throwable t) {
                                if (!Modifier.isFinal(f.getModifiers())) {
                                    f.set(null, null);
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
                return;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
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
