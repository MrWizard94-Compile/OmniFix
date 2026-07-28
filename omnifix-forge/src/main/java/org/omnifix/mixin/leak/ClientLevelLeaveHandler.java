package org.omnifix.mixin.leak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

/**
 * Client-side optional-mod leak hygiene (GeckoLib, JEI, FTB Library, EMI, EMF, ETF,
 * Iron's Spellbooks, JourneyMap, Tombstone, Mouse Tweaks, FindMe, Corpse, Easy Villagers,
 * Traveler's Backpack layer, Citadel animator, Moonlight, Flywheel, MineColonies, PNC armor UI,
 * Twilight Forest, BetterF3, Beans Backpacks, Serene Seasons, Mowzie's Mobs, AE2WT, badpackets,
 * Blue Skies, Iceberg, JER, MNA, NuclearCraft, vanilla residual soft caches).
 *
 * <p>Uses Forge event subscribers and reflection only — no hard {@code @Mixin} on optional
 * classes that may be absent. Gates each path on its FeatureUnit and soft-fails when the
 * target class/field layout is missing.
 *
 * <p>Symptom catalog: AllTheLeaks rows (independent reimplementation).
 */
public final class ClientLevelLeaveHandler {

    private static final String GECKO_MOLANG_PARSER = "software.bernie.geckolib.core.molang.MolangParser";
    private static final String GECKO_ARMOR_RENDERER = "software.bernie.geckolib.renderer.GeoArmorRenderer";
    private static final String JEI_TRANSFER_BUTTON = "mezz.jei.gui.recipes.RecipeTransferButton";
    private static final String JEI_RECIPES_GUI = "mezz.jei.gui.recipes.RecipesGui";
    private static final String JEI_INTERNAL = "mezz.jei.common.Internal";
    private static final String FTB_GUI_HELPER = "dev.ftb.mods.ftblibrary.ui.GuiHelper";
    private static final String FTB_BASE_SCREEN = "dev.ftb.mods.ftblibrary.ui.BaseScreen";

    private static final String EMI_HISTORY = "dev.emi.emi.runtime.EmiHistory";

    private static final String[] EMF_PROBE = {
            "traben.entity_model_features.EMF",
            "traben.entity_model_features.EMFClient",
            "traben.entity_model_features.models.animation.EMFAnimationEntityContext"
    };

    private static final String[] ETF_MANAGER = {
            "traben.entity_texture_features.features.ETFManager",
            "traben.entity_texture_features.ETFManager"
    };

    private static final String[] ETF_PROBE = {
            "traben.entity_texture_features.ETF",
            "traben.entity_texture_features.features.ETFManager"
    };

    private static final String IRONS_CLIENT_MAGIC = "io.redspace.ironsspellbooks.player.ClientMagicData";
    private static final String IRONS_SPELL_BAR = "io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay";
    private static final String IRONS_DEAD_KING_MUSIC =
            "io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingMusicManager";

    private static final String[] JOURNEYMAP_COMPARATORS = {
            "journeymap.client.model.EntityDistanceComparator",
            "journeymap.client.model.EntityDTODistanceComparator",
            "journeymap.client.render.draw.EntityDistanceComparator",
            "journeymap.client.render.draw.EntityDTODistanceComparator",
            "journeymap.client.waypoint.EntityDistanceComparator"
    };

    private static final String[] TOMBSTONE_PROBE = {
            "ovh.corail.tombstone.helper.Helper",
            "ovh.corail.tombstone.ModTombstone",
            "ovh.corail.tombstone.core.ModTombstone"
    };

    private static final String MOUSE_TWEAKS_MAIN = "yalter.mousetweaks.Main";

    private static final String FINDME_CLIENT = "com.buuz135.findme.FindMeModClient";
    private static final String CORPSE_RENDERER = "de.maxhenkel.corpse.entities.CorpseRenderer";
    private static final String EASY_VILLAGER_TE_CACHE = "de.maxhenkel.easyvillagers.ItemTileEntityCache";
    private static final String EASY_VILLAGER_ITEM = "de.maxhenkel.easyvillagers.items.VillagerItem";
    private static final String[] TRAVELERS_BACKPACK_PROBE = {
            "com.tiviacz.travelersbackpack.handlers.ModEventHandler",
            "com.tiviacz.travelersbackpack.handlers.NeoForgeEventHandler",
            "com.tiviacz.travelersbackpack.handlers.ForgeEventHandler",
            "com.tiviacz.travelersbackpack.TravelersBackpack"
    };
    private static final String[] TRAVELERS_BACKPACK_MODEL = {
            "com.tiviacz.travelersbackpack.client.model.BackpackModel",
            "com.tiviacz.travelersbackpack.client.model.BackpackLayerModel",
            "com.tiviacz.travelersbackpack.client.renderer.BackpackLayer"
    };
    private static final String CITADEL_MODEL_ANIMATOR =
            "com.github.alexthe666.citadel.client.model.ModelAnimator";
    private static final String[] MOONLIGHT_PROBE = {
            "net.mehvahdjukaar.moonlight.core.Moonlight",
            "net.mehvahdjukaar.moonlight.core.MoonlightClient",
            "net.mehvahdjukaar.moonlight.api.client.TextureCache"
    };
    private static final String MOONLIGHT_TEXTURE_CACHE =
            "net.mehvahdjukaar.moonlight.api.client.TextureCache";

    private static final String[] FLYWHEEL_WORLD_ATTACHED = {
            "com.jozufozu.flywheel.util.WorldAttached",
            "dev.engine_room.flywheel.lib.util.LevelAttached",
            "dev.engine_room.flywheel.lib.util.WorldAttached"
    };

    private static final String[] MINECOLONIES_RECIPE = {
            "com.minecolonies.core.compatibility.jei.JobBasedRecipeCategory",
            "com.minecolonies.api.crafting.GenericRecipe",
            "com.minecolonies.core.colony.crafting.GenericRecipe"
    };

    private static final String PNC_ARMOR_MAIN_SCREEN =
            "me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorMainScreen";

    private static final String[] TWILIGHT_ENTITY_RENDERER = {
            "twilightforest.compat.jei.renderers.EntityRenderer",
            "twilightforest.compat.jei.EntityRenderer"
    };
    private static final String[] TWILIGHT_HYDRA_MODEL = {
            "twilightforest.client.model.entity.HydraModel",
            "twilightforest.client.model.entity.PartContainerHydraModel"
    };
    private static final String[] TWILIGHT_TRANSFORM_CATEGORY = {
            "twilightforest.compat.jei.categories.TransformationPowderCategory",
            "twilightforest.compat.jei.TransformationPowderCategory"
    };

    private static final String[] BETTERF3_LOCATION = {
            "me.cominixo.betterf3.modules.LocationModule",
            "dev.cominixo.betterf3.modules.LocationModule"
    };
    private static final String[] BETTERF3_MODULES = {
            "me.cominixo.betterf3.utils.Utils",
            "me.cominixo.betterf3.modules.BaseModule",
            "me.cominixo.betterf3.BetterF3Config"
    };

    private static final String[] BEANS_ENDER_STORAGE = {
            "com.beansgalaxy.backpacks.data.EnderStorage",
            "com.beansgalaxy.backpacks.inventory.EnderStorage",
            "com.beansgalaxy.backpacks.client.network.CommonAtClient"
    };

    private static final String[] SERENE_SEASON_HANDLER = {
            "sereneseasons.season.SeasonHandler",
            "sereneseasons.handler.season.SeasonHandler"
    };

    private static final String[] MOWZIES_CLIENT_PROXY = {
            "com.bobmowzie.mowziesmobs.client.ClientProxy",
            "com.bobmowzie.mowziesmobs.client.ClientEventHandler"
    };
    private static final String[] MOWZIES_BOSS_MUSIC = {
            "com.bobmowzie.mowziesmobs.client.sound.BossMusicPlayer",
            "com.bobmowzie.mowziesmobs.client.sound.BossMusic"
    };
    private static final String[] MOWZIES_MODEL_ANIMATOR = {
            "com.bobmowzie.mowziesmobs.client.model.tools.MMModelAnimator",
            "com.bobmowzie.mowziesmobs.client.model.tools.ModelAnimator"
    };

    /** Entity-bound Molang query names set by GeoModel#applyMolangQueries. */
    private static final String[] GECKO_ENTITY_QUERY_HINTS = {
            "query.health",
            "query.max_health",
            "query.is_on_fire",
            "query.ground_speed",
            "query.yaw_speed",
            "query.distance_from_camera",
            "query.is_on_ground",
            "query.is_in_water",
            "query.is_in_water_or_rain",
            "query.life_time",
            "query.actor_count",
            "query.time_of_day",
            "query.moon_phase",
            "query.anim_time",
            "query.controller_speed",
            "health",
            "max_health",
            "is_on_fire",
            "ground_speed",
            "yaw_speed",
            "distance_from_camera",
            "is_on_ground",
            "is_in_water",
            "is_in_water_or_rain",
            "life_time",
            "actor_count",
            "time_of_day",
            "moon_phase",
            "anim_time",
            "controller_speed"
    };

    private ClientLevelLeaveHandler() {}

    /**
     * Registers on the game bus. Must only be invoked on the physical client
     * ({@link Dist#CLIENT}) so client-only event classes are not loaded dedicated.
     */
    public static void register() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        MinecraftForge.EVENT_BUS.register(ClientLevelLeaveHandler.class);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clearAllClientLeaks(null);
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        var local = event.getPlayer();
        if (local != null && local.level() instanceof net.minecraft.client.multiplayer.ClientLevel cl) {
            if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_CYCLOPS)
                    || FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_EMI_LOOT)
                    || FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_LDLIB)) {
                InstanceLeakRegistry.onClientLevelReady(cl, local);
            }
        }
    }

    @SubscribeEvent
    public static void onClientClone(ClientPlayerNetworkEvent.Clone event) {
        clearAllClientLeaks(event.getNewPlayer());
    }

    /**
     * Also covers logical-client {@link PlayerEvent.Clone} (respawn / dimension) when the
     * Forge client network clone event is not the only path.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        if (player == null || !player.level().isClientSide) {
            return;
        }
        clearAllClientLeaks(player);
    }

    private static void clearAllClientLeaks(Player newPlayer) {
        clearGeckoLibCaches();
        clearJeiTransferButtons(newPlayer);
        clearFtbBlankGuiPrevScreen();
        clearEmiHistory();
        clearEmfHeldIteration();
        clearEtfHeldEntityAndPlayerMap();
        clearIronsSpellbooks(newPlayer);
        clearJourneyMapPlayerRefs();
        clearTombstoneRendererEntity();
        clearMouseTweaksOpenScreen();
        clearFindMeLastStack();
        clearCorpseRendererCaches();
        clearEasyVillagersCaches();
        clearTravelersBackpackLayerEntity();
        clearCitadelModelAnimatorEntity();
        clearMoonlightSoftCaches();
        clearFlywheelWorldAttached();
        clearMinecoloniesRecipeEntities();
        clearPncArmorUpgradeOptions();
        clearTwilightForestEntityMapAndHydra();
        clearBetterF3LocationChunk();
        clearBeansBackpacksEnderStorage(null);
        clearSereneSeasonsClientSnowField();
        clearMowziesBossMusicAndAnimator();
        clearAe2wtClient(newPlayer);
        clearBadpacketsClient();
        clearBlueSkiesClient();
        clearIcebergClient();
        clearJerEntityResiduals();
        clearMnaRenderEntity();
        clearNuclearcraftTooltipEvent();
        clearMcVanillaClientResiduals();
        clearTrackedOptionalModInstances(newPlayer);
    }

    /**
     * Hard-instance ATL-class leaks (Cyclops / EMI Loot / LDLib) registered via
     * {@link InstanceLeakRegistry} Pseudo mixins.
     */
    private static void clearTrackedOptionalModInstances(Player newPlayer) {
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_CYCLOPS)
                || FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_EMI_LOOT)
                || FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_LDLIB)) {
            InstanceLeakRegistry.onClientLevelLeave();
            if (newPlayer instanceof net.minecraft.client.player.LocalPlayer local
                    && newPlayer.level() instanceof net.minecraft.client.multiplayer.ClientLevel cl) {
                InstanceLeakRegistry.onClientLevelReady(cl, local);
            }
        }
    }

    // -------------------------------------------------------------------------
    // GeckoLib
    // -------------------------------------------------------------------------

    private static void clearGeckoLibCaches() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_GECKOLIB)) {
            return;
        }
        try {
            Class.forName(GECKO_MOLANG_PARSER);
        } catch (ClassNotFoundException ignored) {
            return;
        } catch (Throwable ignored) {
            return;
        }
        resetMolangMemoizedSuppliers();
        clearGeoArmorRendererFieldsOnScreenGraph();
    }

    /**
     * GeoModel#applyMolangQueries installs DoubleSuppliers that capture Entity/Level strongly
     * into LazyVariable slots on MolangParser.VARIABLES. Reset those suppliers to zero so prior
     * client levels can be collected after logout / level leave.
     */
    private static void resetMolangMemoizedSuppliers() {
        try {
            Class<?> parserClass = Class.forName(GECKO_MOLANG_PARSER);
            Field variablesField = findStaticField(parserClass, "VARIABLES");
            if (variablesField == null) {
                return;
            }
            Object variables = variablesField.get(null);
            if (!(variables instanceof Map<?, ?> map) || map.isEmpty()) {
                return;
            }
            DoubleSupplier zero = () -> 0.0d;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object lazy = entry.getValue();
                if (lazy == null) {
                    continue;
                }
                String name = key instanceof String s ? s : String.valueOf(key);
                if (!isEntityBoundQuery(name)) {
                    // Still reset suppliers that close over entities: any LazyVariable with a
                    // non-constant supplier is best-effort zeroed for known query prefixes.
                    if (!name.contains("query.") && !name.contains("math.")) {
                        continue;
                    }
                    if (name.startsWith("math.")) {
                        continue;
                    }
                }
                trySetLazyVariable(lazy, zero);
            }
            // Named query constants may use full MolangQueries strings — also zero by name list.
            for (String hint : GECKO_ENTITY_QUERY_HINTS) {
                Object lazy = map.get(hint);
                if (lazy != null) {
                    trySetLazyVariable(lazy, zero);
                }
            }
        } catch (Throwable ignored) {
            // Optional path: soft fail across GeckoLib minor versions.
        }
    }

    private static boolean isEntityBoundQuery(String name) {
        String n = name.toLowerCase();
        return n.contains("health")
                || n.contains("ground_speed")
                || n.contains("yaw_speed")
                || n.contains("distance_from_camera")
                || n.contains("is_on_ground")
                || n.contains("is_in_water")
                || n.contains("is_on_fire")
                || n.contains("life_time")
                || n.contains("actor_count")
                || n.contains("time_of_day")
                || n.contains("moon_phase")
                || n.contains("anim_time")
                || n.contains("controller_speed");
    }

    private static void trySetLazyVariable(Object lazy, DoubleSupplier zero) {
        try {
            Method setSupplier = null;
            for (Method m : lazy.getClass().getMethods()) {
                if (!"set".equals(m.getName()) || m.getParameterCount() != 1) {
                    continue;
                }
                Class<?> p = m.getParameterTypes()[0];
                if (DoubleSupplier.class.isAssignableFrom(p) || p.getName().contains("DoubleSupplier")) {
                    setSupplier = m;
                    break;
                }
            }
            if (setSupplier != null) {
                setSupplier.invoke(lazy, zero);
                return;
            }
            // Fallback: zero via set(double)
            Method setDouble = lazy.getClass().getMethod("set", double.class);
            setDouble.invoke(lazy, 0.0d);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Best-effort: if the current screen graph holds GeoArmorRenderer instances (item GUIs),
     * null entity/stack/slot fields so the prior ClientLevel is not pinned.
     */
    private static void clearGeoArmorRendererFieldsOnScreenGraph() {
        try {
            Class<?> armorClass = Class.forName(GECKO_ARMOR_RENDERER);
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return;
            }
            IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
            walkAndClearArmorRenderer(mc.screen, armorClass, seen, 0);
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void walkAndClearArmorRenderer(Object root, Class<?> armorClass,
                                                  IdentityHashMap<Object, Boolean> seen, int depth) {
        if (root == null || depth > 8 || seen.containsKey(root)) {
            return;
        }
        if (root.getClass().isArray() || root instanceof Enum<?>) {
            return;
        }
        String cn = root.getClass().getName();
        if (cn.startsWith("java.") || cn.startsWith("javax.") || cn.startsWith("sun.")
                || cn.startsWith("jdk.") || cn.startsWith("com.sun.")) {
            return;
        }
        seen.put(root, Boolean.TRUE);
        if (armorClass.isInstance(root)) {
            clearArmorRendererEntityFields(root);
            return;
        }
        Class<?> c = root.getClass();
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(root);
                    if (v == null) {
                        continue;
                    }
                    if (armorClass.isInstance(v)) {
                        clearArmorRendererEntityFields(v);
                    } else if (v instanceof Collection<?> col) {
                        for (Object o : col) {
                            walkAndClearArmorRenderer(o, armorClass, seen, depth + 1);
                        }
                    } else if (v instanceof Map<?, ?> map) {
                        for (Object o : map.values()) {
                            walkAndClearArmorRenderer(o, armorClass, seen, depth + 1);
                        }
                    } else {
                        walkAndClearArmorRenderer(v, armorClass, seen, depth + 1);
                    }
                } catch (Throwable ignored) {
                }
            }
            c = c.getSuperclass();
        }
    }

    private static void clearArmorRendererEntityFields(Object renderer) {
        for (Field f : renderer.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                continue;
            }
            String name = f.getName().toLowerCase();
            if (!(name.contains("currententity")
                    || name.contains("currentstack")
                    || name.contains("currentslot")
                    || name.equals("animatable")
                    || name.equals("basemodel"))) {
                continue;
            }
            try {
                f.setAccessible(true);
                f.set(renderer, null);
            } catch (Throwable ignored) {
            }
        }
        // Also scan superclass GeoArmorRenderer declared fields by exact known names.
        Class<?> c = renderer.getClass();
        while (c != null && c != Object.class) {
            for (String exact : new String[]{"currentEntity", "currentStack", "currentSlot", "animatable", "baseModel"}) {
                try {
                    Field f = c.getDeclaredField(exact);
                    f.setAccessible(true);
                    if (!f.getType().isPrimitive()) {
                        f.set(renderer, null);
                    }
                } catch (NoSuchFieldException ignored) {
                } catch (Throwable ignored) {
                }
            }
            c = c.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // JEI
    // -------------------------------------------------------------------------

    private static void clearJeiTransferButtons(Player newPlayer) {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_JEI)) {
            return;
        }
        try {
            Class.forName(JEI_TRANSFER_BUTTON);
        } catch (ClassNotFoundException ignored) {
            return;
        } catch (Throwable ignored) {
            return;
        }
        try {
            Class<?> buttonClass = Class.forName(JEI_TRANSFER_BUTTON);
            Minecraft mc = Minecraft.getInstance();
            AbstractContainerMenu menu = null;
            if (newPlayer != null) {
                menu = newPlayer.containerMenu;
            }
            // Prefer JEI runtime recipes GUI when available.
            Object recipesGui = tryGetJeiRecipesGui();
            if (recipesGui != null) {
                updateTransferButtonsIn(recipesGui, buttonClass, menu, newPlayer, new IdentityHashMap<>(), 0);
            }
            if (mc != null && mc.screen != null) {
                updateTransferButtonsIn(mc.screen, buttonClass, menu, newPlayer, new IdentityHashMap<>(), 0);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Object tryGetJeiRecipesGui() {
        try {
            Class<?> internal = Class.forName(JEI_INTERNAL);
            Method getRuntime = null;
            for (Method m : internal.getMethods()) {
                if (Modifier.isStatic(m.getModifiers())
                        && m.getParameterCount() == 0
                        && m.getName().toLowerCase().contains("runtime")) {
                    getRuntime = m;
                    break;
                }
            }
            if (getRuntime == null) {
                return null;
            }
            Object runtime = getRuntime.invoke(null);
            if (runtime == null) {
                return null;
            }
            for (Method m : runtime.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getName().toLowerCase().contains("recipesgui")) {
                    return m.invoke(runtime);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void updateTransferButtonsIn(Object root, Class<?> buttonClass,
                                                AbstractContainerMenu menu, Player player,
                                                IdentityHashMap<Object, Boolean> seen, int depth) {
        if (root == null || depth > 10 || seen.containsKey(root)) {
            return;
        }
        if (root.getClass().isArray() || root instanceof Enum<?>) {
            return;
        }
        String cn = root.getClass().getName();
        if (cn.startsWith("java.") || cn.startsWith("javax.") || cn.startsWith("sun.")
                || cn.startsWith("jdk.") || cn.startsWith("com.sun.")
                || cn.startsWith("net.minecraft.") && depth > 2) {
            // Allow shallow minecraft screen graph; avoid walking whole MC.
            if (!Screen.class.isInstance(root) && depth > 0) {
                return;
            }
        }
        seen.put(root, Boolean.TRUE);
        if (buttonClass.isInstance(root)) {
            applyRecipeTransferUpdate(root, menu, player);
            return;
        }
        Class<?> c = root.getClass();
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(root);
                    if (v == null) {
                        continue;
                    }
                    if (buttonClass.isInstance(v)) {
                        applyRecipeTransferUpdate(v, menu, player);
                    } else if (v instanceof Collection<?> col) {
                        for (Object o : col) {
                            if (buttonClass.isInstance(o)) {
                                applyRecipeTransferUpdate(o, menu, player);
                            } else {
                                updateTransferButtonsIn(o, buttonClass, menu, player, seen, depth + 1);
                            }
                        }
                    } else if (v instanceof Map<?, ?> map) {
                        for (Object o : map.values()) {
                            updateTransferButtonsIn(o, buttonClass, menu, player, seen, depth + 1);
                        }
                    } else if (cn.startsWith("mezz.jei.") || depth < 4) {
                        updateTransferButtonsIn(v, buttonClass, menu, player, seen, depth + 1);
                    }
                } catch (Throwable ignored) {
                }
            }
            c = c.getSuperclass();
        }
    }

    /**
     * Prefer {@code RecipeTransferButton#update(container, player)}; fall back to nulling fields.
     * On logout {@code player} is null so fields are cleared. On clone, refresh to the new player.
     */
    private static void applyRecipeTransferUpdate(Object button, AbstractContainerMenu menu, Player player) {
        try {
            Method update = null;
            for (Method m : button.getClass().getMethods()) {
                if (!"update".equals(m.getName()) || m.getParameterCount() != 2) {
                    continue;
                }
                Class<?>[] p = m.getParameterTypes();
                if (AbstractContainerMenu.class.isAssignableFrom(p[0])
                        || p[0].getName().contains("AbstractContainerMenu")) {
                    if (Player.class.isAssignableFrom(p[1]) || p[1].getName().contains("Player")) {
                        update = m;
                        break;
                    }
                }
            }
            if (update != null) {
                update.invoke(button, menu, player);
                return;
            }
        } catch (Throwable ignored) {
        }
        // Field clear fallback (older JEI / unexpected shape).
        for (Field f : button.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                continue;
            }
            String name = f.getName().toLowerCase();
            if (!(name.contains("player") || name.contains("parentcontainer") || name.contains("container"))) {
                continue;
            }
            try {
                f.setAccessible(true);
                if (player != null && name.contains("player") && f.getType().isInstance(player)) {
                    f.set(button, player);
                } else if (menu != null && name.contains("container") && f.getType().isInstance(menu)) {
                    f.set(button, menu);
                } else {
                    f.set(button, null);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // FTB Library
    // -------------------------------------------------------------------------

    private static void clearFtbBlankGuiPrevScreen() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_FTB_LIBRARY)) {
            return;
        }
        try {
            Class<?> guiHelper = Class.forName(FTB_GUI_HELPER);
            Field blankField = guiHelper.getField("BLANK_GUI");
            Object blankGui = blankField.get(null);
            if (blankGui == null) {
                return;
            }
            Class<?> baseScreen = Class.forName(FTB_BASE_SCREEN);
            nullPrevScreenFields(blankGui, baseScreen);
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void nullPrevScreenFields(Object blankGui, Class<?> baseScreen) {
        Class<?> c = baseScreen;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType().isPrimitive()) {
                    continue;
                }
                String name = f.getName().toLowerCase();
                boolean isPrev = name.contains("prev") && (name.contains("screen") || name.contains("gui"));
                boolean isScreenRef = Screen.class.isAssignableFrom(f.getType())
                        && (name.contains("prev") || name.equals("parentscreen") || name.equals("lastscreen"));
                if (!isPrev && !isScreenRef) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    // prevScreen is often final — setAccessible + set still works for instance
                    // finals on HotSpot 17 for non-static fields in practice.
                    f.set(blankGui, null);
                } catch (Throwable ignored) {
                }
            }
            c = c.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // EMI
    // -------------------------------------------------------------------------

    private static void clearEmiHistory() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_EMI)) {
            return;
        }
        try {
            Class<?> historyClass = Class.forName(EMI_HISTORY);
            // Prefer public static clear().
            try {
                Method clear = historyClass.getMethod("clear");
                if (Modifier.isStatic(clear.getModifiers())) {
                    clear.invoke(null);
                    return;
                }
            } catch (NoSuchMethodException ignored) {
            }
            // Fallback: clear static stacks/lists whose names contain "history".
            for (Field f : historyClass.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                if (!(n.contains("history") || n.contains("histories") || n.equals("stack")
                        || n.contains("forward"))) {
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
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // EMF — LivingEntityRenderer emf$heldIteration
    // -------------------------------------------------------------------------

    private static void clearEmfHeldIteration() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_EMF)) {
            return;
        }
        if (!isAnyClassPresent(EMF_PROBE)) {
            return;
        }
        clearLivingRendererFieldsByHint("emf", "helditeration", "held_iteration", "iteration");
    }

    // -------------------------------------------------------------------------
    // ETF — heldEntity + PLAYER_TEXTURE_MAP
    // -------------------------------------------------------------------------

    private static void clearEtfHeldEntityAndPlayerMap() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ETF)) {
            return;
        }
        if (!isAnyClassPresent(ETF_PROBE)) {
            return;
        }
        clearLivingRendererFieldsByHint("etf", "heldentity", "held_entity");
        clearEtfPlayerTextureMap();
    }

    private static void clearEtfPlayerTextureMap() {
        for (String name : ETF_MANAGER) {
            try {
                Class<?> type = Class.forName(name);
                Object manager = resolveSingleton(type);
                if (manager == null) {
                    // Static map field on the class itself.
                    clearNamedMapField(type, null, "PLAYER_TEXTURE_MAP", "player_texture");
                    continue;
                }
                clearNamedMapField(type, manager, "PLAYER_TEXTURE_MAP", "player_texture");
                return;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static Object resolveSingleton(Class<?> type) {
        try {
            Field instance = findStaticField(type, "instance");
            if (instance == null) {
                instance = findStaticField(type, "INSTANCE");
            }
            if (instance != null) {
                Object v = instance.get(null);
                if (v != null) {
                    return v;
                }
            }
        } catch (Throwable ignored) {
        }
        for (String mName : new String[]{"getInstance", "get", "instance"}) {
            try {
                Method m = type.getMethod(mName);
                if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0) {
                    return m.invoke(null);
                }
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static void clearNamedMapField(Class<?> type, Object instance, String exact, String hint) {
        try {
            Field f = null;
            try {
                f = type.getField(exact);
            } catch (NoSuchFieldException e) {
                try {
                    f = type.getDeclaredField(exact);
                } catch (NoSuchFieldException ignored) {
                }
            }
            if (f == null) {
                for (Field cand : type.getDeclaredFields()) {
                    if (cand.getName().toLowerCase().contains(hint.toLowerCase())) {
                        f = cand;
                        break;
                    }
                }
            }
            if (f == null) {
                return;
            }
            f.setAccessible(true);
            Object owner = Modifier.isStatic(f.getModifiers()) ? null : instance;
            Object v = f.get(owner);
            if (v instanceof Map<?, ?> map) {
                map.clear();
                return;
            }
            if (v != null) {
                try {
                    Method clear = v.getClass().getMethod("clear");
                    clear.invoke(v);
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Iron's Spellbooks
    // -------------------------------------------------------------------------

    private static void clearIronsSpellbooks(Player newPlayer) {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_IRONS_SPELLBOOKS)) {
            return;
        }
        try {
            Class.forName(IRONS_CLIENT_MAGIC);
        } catch (ClassNotFoundException ignored) {
            return;
        } catch (Throwable ignored) {
            return;
        }
        clearIronsSpellSelection(newPlayer);
        clearIronsSpellBarLastSelection();
        hardStopDeadKingMusic();
    }

    private static void clearIronsSpellSelection(Player newPlayer) {
        try {
            Class<?> type = Class.forName(IRONS_CLIENT_MAGIC);
            if (newPlayer != null) {
                // Prefer refresh to the new player when available.
                try {
                    Method update = type.getMethod("updateSpellSelectionManager");
                    if (Modifier.isStatic(update.getModifiers())) {
                        update.invoke(null);
                        return;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
            Field field = findStaticField(type, "spellSelectionManager");
            if (field == null) {
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (n.contains("spellselection") || n.contains("selectionmanager")) {
                        field = f;
                        field.setAccessible(true);
                        break;
                    }
                }
            }
            if (field != null && !field.getType().isPrimitive()) {
                field.set(null, null);
            }
            // Also drop targeting data that may pin entities.
            try {
                Method reset = type.getMethod("resetTargetingData");
                if (Modifier.isStatic(reset.getModifiers())) {
                    reset.invoke(null);
                }
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Throwable ignored) {
        }
    }

    private static void clearIronsSpellBarLastSelection() {
        try {
            Class<?> type = Class.forName(IRONS_SPELL_BAR);
            Field field = findStaticField(type, "lastSelection");
            if (field == null) {
                for (Field f : type.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers())
                            && f.getName().toLowerCase().contains("lastselection")) {
                        field = f;
                        field.setAccessible(true);
                        break;
                    }
                }
            }
            if (field != null && !field.getType().isPrimitive()) {
                field.set(null, null);
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void hardStopDeadKingMusic() {
        try {
            Class<?> type = Class.forName(IRONS_DEAD_KING_MUSIC);
            try {
                Method hardStop = type.getMethod("hardStop");
                if (Modifier.isStatic(hardStop.getModifiers())) {
                    hardStop.invoke(null);
                    return;
                }
            } catch (NoSuchMethodException ignored) {
            }
            Field instance = findStaticField(type, "INSTANCE");
            if (instance != null) {
                Object mgr = instance.get(null);
                if (mgr != null) {
                    try {
                        Method stopLayers = mgr.getClass().getMethod("stopLayers");
                        stopLayers.invoke(mgr);
                    } catch (Throwable ignored) {
                    }
                }
                instance.set(null, null);
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // JourneyMap
    // -------------------------------------------------------------------------

    private static void clearJourneyMapPlayerRefs() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_JOURNEYMAP)) {
            return;
        }
        boolean any = false;
        for (String name : JOURNEYMAP_COMPARATORS) {
            try {
                Class<?> type = Class.forName(name);
                nullStaticPlayerLikeFields(type);
                any = true;
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        if (any) {
            return;
        }
        // Soft fallback: only if JourneyMap is present at all.
        try {
            Class.forName("journeymap.common.Journeymap");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("journeymap.client.JourneymapClient");
            } catch (ClassNotFoundException e2) {
                try {
                    Class.forName("journeymap.JourneyMap");
                } catch (ClassNotFoundException e3) {
                    return;
                } catch (Throwable ignored) {
                    return;
                }
            } catch (Throwable ignored) {
                return;
            }
        } catch (Throwable ignored) {
            return;
        }
        // No known comparator class — nothing further without hard classpath scanning.
    }

    private static void nullStaticPlayerLikeFields(Class<?> type) {
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                continue;
            }
            String n = f.getName().toLowerCase();
            boolean nameHint = n.contains("player") || n.equals("p") || n.contains("viewer");
            boolean typeHint = Player.class.isAssignableFrom(f.getType())
                    || f.getType().getName().contains("Player")
                    || f.getType().getName().contains("Entity");
            if (!nameHint && !typeHint) {
                continue;
            }
            if (!nameHint && !n.contains("player") && !n.contains("entity")) {
                // Avoid wiping unrelated entity caches when only type matches.
                if (!Player.class.isAssignableFrom(f.getType())) {
                    continue;
                }
            }
            try {
                f.setAccessible(true);
                f.set(null, null);
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tombstone — LivingEntityRenderer.entity mixin residual
    // -------------------------------------------------------------------------

    private static void clearTombstoneRendererEntity() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_TOMBSTONE)) {
            return;
        }
        if (!isAnyClassPresent(TOMBSTONE_PROBE)) {
            return;
        }
        clearLivingRendererFieldsByHint("tombstone", "entity");
        // ATL documents a plain "entity" field injected onto LivingEntityRenderer.
        clearLivingRendererExactField("entity");
    }

    // -------------------------------------------------------------------------
    // Mouse Tweaks
    // -------------------------------------------------------------------------

    private static void clearMouseTweaksOpenScreen() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_MOUSE_TWEAKS)) {
            return;
        }
        try {
            Class<?> main = Class.forName(MOUSE_TWEAKS_MAIN);
            for (Field f : main.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                if (!(n.equals("openscreen")
                        || n.equals("handler")
                        || n.equals("oldselectedslot")
                        || n.contains("openscreen")
                        || n.contains("selectedslot"))) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    f.set(null, null);
                } catch (Throwable ignored) {
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // LivingEntityRenderer field sweeps (EMF / ETF / Tombstone)
    // -------------------------------------------------------------------------

    /**
     * Walks {@link EntityRenderDispatcher} renderer maps and nulls non-primitive instance fields
     * whose names match any of the lowercase hints (or contain them).
     */
    private static void clearLivingRendererFieldsByHint(String... hints) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        try {
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            if (dispatcher == null) {
                return;
            }
            IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
            // Direct player renderer when a local player is available.
            try {
                if (mc.player != null) {
                    EntityRenderer<?> pr = dispatcher.getRenderer(mc.player);
                    if (pr != null) {
                        nullFieldsMatchingHints(pr, hints, seen);
                    }
                }
            } catch (Throwable ignored) {
            }
            // Reflect renderer maps on the dispatcher.
            Class<?> c = dispatcher.getClass();
            while (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(dispatcher);
                        if (v == null) {
                            continue;
                        }
                        if (v instanceof EntityRenderer<?> er) {
                            nullFieldsMatchingHints(er, hints, seen);
                        } else if (v instanceof Map<?, ?> map) {
                            for (Object o : map.values()) {
                                if (o instanceof EntityRenderer<?> er) {
                                    nullFieldsMatchingHints(er, hints, seen);
                                }
                            }
                        } else if (v instanceof Collection<?> col) {
                            for (Object o : col) {
                                if (o instanceof EntityRenderer<?> er) {
                                    nullFieldsMatchingHints(er, hints, seen);
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
                c = c.getSuperclass();
            }
        } catch (Throwable ignored) {
        }
    }

    private static void clearLivingRendererExactField(String fieldName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }
        try {
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            if (dispatcher == null) {
                return;
            }
            IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
            Class<?> c = dispatcher.getClass();
            while (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(dispatcher);
                        if (v instanceof EntityRenderer<?> er) {
                            nullExactField(er, fieldName, seen);
                        } else if (v instanceof Map<?, ?> map) {
                            for (Object o : map.values()) {
                                if (o instanceof EntityRenderer<?> er) {
                                    nullExactField(er, fieldName, seen);
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
                c = c.getSuperclass();
            }
            // LivingEntityRenderer class-level declared field (mixin merge target).
            try {
                Field shared = LivingEntityRenderer.class.getDeclaredField(fieldName);
                // Instance field — must clear per renderer instance (handled above).
                shared.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }
        } catch (Throwable ignored) {
        }
    }

    private static void nullFieldsMatchingHints(Object renderer, String[] hints,
                                                IdentityHashMap<Object, Boolean> seen) {
        if (renderer == null || seen.containsKey(renderer)) {
            return;
        }
        seen.put(renderer, Boolean.TRUE);
        Class<?> c = renderer.getClass();
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                boolean match = false;
                for (String hint : hints) {
                    String h = hint.toLowerCase();
                    if (n.equals(h) || n.contains(h) || n.contains(h.replace("$", ""))) {
                        match = true;
                        break;
                    }
                }
                // EMF/ETF mixin names often look like emf$heldIteration / etf$heldEntity.
                if (!match && (n.startsWith("emf$") || n.startsWith("etf$") || n.startsWith("tombstone$"))) {
                    for (String hint : hints) {
                        if (n.contains(hint.toLowerCase().replace("$", ""))) {
                            match = true;
                            break;
                        }
                    }
                }
                if (!match) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    f.set(renderer, null);
                } catch (Throwable ignored) {
                }
            }
            c = c.getSuperclass();
        }
    }

    private static void nullExactField(Object renderer, String fieldName,
                                       IdentityHashMap<Object, Boolean> seen) {
        if (renderer == null || seen.containsKey(renderer)) {
            return;
        }
        seen.put(renderer, Boolean.TRUE);
        Class<?> c = renderer.getClass();
        while (c != null && c != Object.class) {
            try {
                Field f = c.getDeclaredField(fieldName);
                if (!Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
                    f.setAccessible(true);
                    f.set(renderer, null);
                }
            } catch (NoSuchFieldException ignored) {
            } catch (Throwable ignored) {
            }
            c = c.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // FindMe — lastRenderedStack pins ItemStack → level
    // -------------------------------------------------------------------------

    private static void clearFindMeLastStack() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_FINDME)) {
            return;
        }
        try {
            Class<?> type = Class.forName(FINDME_CLIENT);
            Field field = findStaticField(type, "lastRenderedStack");
            if (field == null) {
                for (Field f : type.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers())
                            && f.getName().toLowerCase().contains("lastrendered")) {
                        field = f;
                        field.setAccessible(true);
                        break;
                    }
                }
            }
            if (field == null || field.getType().isPrimitive()) {
                return;
            }
            // Prefer EMPTY if ItemStack type; otherwise null.
            Object empty = null;
            try {
                Field emptyField = field.getType().getField("EMPTY");
                if (Modifier.isStatic(emptyField.getModifiers())) {
                    empty = emptyField.get(null);
                }
            } catch (Throwable ignored) {
            }
            field.set(null, empty);
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Corpse — CorpseRenderer players/skeletons CachedMap
    // -------------------------------------------------------------------------

    private static void clearCorpseRendererCaches() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_CORPSE)) {
            return;
        }
        try {
            Class.forName(CORPSE_RENDERER);
        } catch (ClassNotFoundException ignored) {
            return;
        } catch (Throwable ignored) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return;
            }
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            if (dispatcher == null) {
                return;
            }
            Class<?> corpseRendererClass = Class.forName(CORPSE_RENDERER);
            IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
            // Walk public/reflective renderer maps on the dispatcher.
            Class<?> c = dispatcher.getClass();
            while (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(dispatcher);
                        if (v == null) {
                            continue;
                        }
                        if (corpseRendererClass.isInstance(v)) {
                            clearCachedMapFields(v, "players", "skeletons");
                        } else if (v instanceof Map<?, ?> map) {
                            for (Object o : map.values()) {
                                if (corpseRendererClass.isInstance(o) && !seen.containsKey(o)) {
                                    seen.put(o, Boolean.TRUE);
                                    clearCachedMapFields(o, "players", "skeletons");
                                }
                            }
                        } else if (v instanceof Collection<?> col) {
                            for (Object o : col) {
                                if (corpseRendererClass.isInstance(o) && !seen.containsKey(o)) {
                                    seen.put(o, Boolean.TRUE);
                                    clearCachedMapFields(o, "players", "skeletons");
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
                c = c.getSuperclass();
            }
        } catch (Throwable ignored) {
        }
    }

    private static void clearCachedMapFields(Object owner, String... names) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (String name : names) {
                try {
                    Field f = type.getDeclaredField(name);
                    f.setAccessible(true);
                    Object v = f.get(owner);
                    if (v == null) {
                        continue;
                    }
                    try {
                        Method clear = v.getClass().getMethod("clear");
                        clear.invoke(v);
                    } catch (NoSuchMethodException e) {
                        if (v instanceof Map<?, ?> map) {
                            map.clear();
                        } else if (v instanceof Collection<?> col) {
                            col.clear();
                        }
                    }
                } catch (NoSuchFieldException ignored) {
                } catch (Throwable ignored) {
                }
            }
            // Also any field whose name contains players/skeletons.
            for (Field f : type.getDeclaredFields()) {
                String n = f.getName().toLowerCase();
                if (!(n.contains("player") || n.contains("skeleton"))) {
                    continue;
                }
                if (f.getType().isPrimitive()) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(owner);
                    if (v == null) {
                        continue;
                    }
                    try {
                        Method clear = v.getClass().getMethod("clear");
                        clear.invoke(v);
                    } catch (Throwable t) {
                        if (v instanceof Map<?, ?> map) {
                            map.clear();
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // Easy Villagers — ItemTileEntityCache + VillagerItem caches
    // -------------------------------------------------------------------------

    private static void clearEasyVillagersCaches() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_EASY_VILLAGERS)) {
            return;
        }
        clearStaticNamedCache(EASY_VILLAGER_TE_CACHE, "CACHE");
        try {
            Class<?> itemClass = Class.forName(EASY_VILLAGER_ITEM);
            // Instance field cachedVillagers on each VillagerItem registry entry.
            try {
                Class<?> forgeItems = Class.forName("net.minecraftforge.registries.ForgeRegistries");
                Field itemsField = forgeItems.getField("ITEMS");
                Object items = itemsField.get(null);
                Method iterator = items.getClass().getMethod("iterator");
                Object it = iterator.invoke(items);
                Method hasNext = it.getClass().getMethod("hasNext");
                Method next = it.getClass().getMethod("next");
                while (Boolean.TRUE.equals(hasNext.invoke(it))) {
                    Object item = next.invoke(it);
                    if (item != null && itemClass.isInstance(item)) {
                        clearInstanceCacheField(item, "cachedVillagers");
                    }
                }
            } catch (Throwable t) {
                // Soft: clear static-like fields if layout differs.
                clearStaticNamedCache(EASY_VILLAGER_ITEM, "cachedVillagers");
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void clearStaticNamedCache(String className, String fieldHint) {
        try {
            Class<?> type = Class.forName(className);
            for (Field f : type.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName();
                if (!n.equals(fieldHint) && !n.toLowerCase().contains(fieldHint.toLowerCase())
                        && !n.toLowerCase().contains("cache")) {
                    continue;
                }
                f.setAccessible(true);
                Object v = f.get(null);
                if (v == null) {
                    continue;
                }
                try {
                    Method clear = v.getClass().getMethod("clear");
                    clear.invoke(v);
                } catch (Throwable t) {
                    if (v instanceof Map<?, ?> map) {
                        map.clear();
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void clearInstanceCacheField(Object owner, String fieldName) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            try {
                Field f = type.getDeclaredField(fieldName);
                f.setAccessible(true);
                Object v = f.get(owner);
                if (v != null) {
                    try {
                        Method clear = v.getClass().getMethod("clear");
                        clear.invoke(v);
                    } catch (Throwable t) {
                        if (v instanceof Map<?, ?> map) {
                            map.clear();
                        }
                    }
                }
                return;
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            } catch (Throwable ignored) {
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Traveler's Backpack — layer/model entity residual
    // -------------------------------------------------------------------------

    private static void clearTravelersBackpackLayerEntity() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_TRAVELERSBACKPACK)) {
            return;
        }
        if (!isAnyClassPresent(TRAVELERS_BACKPACK_PROBE) && !isAnyClassPresent(TRAVELERS_BACKPACK_MODEL)) {
            return;
        }
        for (String name : TRAVELERS_BACKPACK_MODEL) {
            try {
                Class<?> type = Class.forName(name);
                // Static model instances (BACKPACK_MODEL) and any livingEntity-like fields.
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object model = f.get(null);
                        if (model == null) {
                            continue;
                        }
                        nullEntityLikeFields(model);
                    } catch (Throwable ignored) {
                    }
                }
                // Also null static entity fields directly on the class.
                nullStaticEntityLikeFields(type);
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void nullEntityLikeFields(Object owner) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                boolean nameHint = n.contains("livingentity") || n.equals("entity")
                        || n.contains("currententity") || n.contains("wearer");
                boolean typeHint = Player.class.isAssignableFrom(f.getType())
                        || f.getType().getName().contains("LivingEntity")
                        || f.getType().getName().contains("Entity");
                if (!nameHint && !typeHint) {
                    continue;
                }
                if (!nameHint && !Player.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    f.set(owner, null);
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    private static void nullStaticEntityLikeFields(Class<?> type) {
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                continue;
            }
            String n = f.getName().toLowerCase();
            if (!(n.contains("livingentity") || n.equals("entity") || n.contains("currententity"))) {
                continue;
            }
            try {
                f.setAccessible(true);
                f.set(null, null);
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Citadel — ModelAnimator.entity residual
    // -------------------------------------------------------------------------

    private static void clearCitadelModelAnimatorEntity() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_CITADEL)) {
            return;
        }
        try {
            Class<?> type = Class.forName(CITADEL_MODEL_ANIMATOR);
            // Null static instances if present; also any static entity field.
            nullStaticEntityLikeFields(type);
            for (Field f : type.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                if (!type.isAssignableFrom(f.getType()) && !f.getName().toLowerCase().contains("instance")) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object animator = f.get(null);
                    if (animator != null) {
                        nullEntityLikeFields(animator);
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Moonlight (soft) — texture / client caches that may pin a ClientLevel
    // -------------------------------------------------------------------------

    private static void clearMoonlightSoftCaches() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_MOONLIGHT)) {
            return;
        }
        if (!isAnyClassPresent(MOONLIGHT_PROBE)) {
            return;
        }
        // Soft best-effort: clear TextureCache and any static Map/Collection named *level* / *cache*.
        try {
            Class<?> type = Class.forName(MOONLIGHT_TEXTURE_CACHE);
            for (Field f : type.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(null);
                    if (v instanceof Map<?, ?> map) {
                        map.clear();
                    } else if (v instanceof Collection<?> col) {
                        col.clear();
                    } else if (v != null) {
                        try {
                            Method clear = v.getClass().getMethod("clear");
                            clear.invoke(v);
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
        // Soft: MoonlightClient static level-ish fields.
        try {
            Class<?> client = Class.forName("net.mehvahdjukaar.moonlight.core.MoonlightClient");
            for (Field f : client.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                if (!(n.contains("level") || n.contains("world") || n.contains("cache"))) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(null);
                    if (v instanceof Map<?, ?> map) {
                        map.clear();
                    } else if (v instanceof Collection<?> col) {
                        col.clear();
                    } else if (v != null && !f.getType().isEnum()) {
                        // Only null non-primitive object refs that look like level pins.
                        if (n.contains("level") || n.contains("world")) {
                            f.set(null, null);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Flywheel — WorldAttached / LevelAttached world maps
    // -------------------------------------------------------------------------

    /**
     * Soft stand-in for ATL's cancel of {@code WorldAttached#put} for non-current worlds: flush
     * attached world maps on client leave so prior ClientLevels do not stick.
     */
    private static void clearFlywheelWorldAttached() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_FLYWHEEL)) {
            return;
        }
        for (String name : FLYWHEEL_WORLD_ATTACHED) {
            try {
                Class<?> type = Class.forName(name);
                clearAllStaticMapsAndWorldLists(type);
                // WorldAttached instances held in a static ALL/INSTANCES list.
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Collection<?> col) {
                            for (Object entry : col) {
                                if (entry != null && type.isInstance(entry)) {
                                    clearInstanceMaps(entry);
                                }
                            }
                        } else if (v instanceof Map<?, ?> map) {
                            // Class-level world map.
                            try {
                                map.clear();
                            } catch (Throwable ignored) {
                            }
                        } else if (v != null && type.isInstance(v)) {
                            clearInstanceMaps(v);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearAllStaticMapsAndWorldLists(Class<?> type) {
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                continue;
            }
            String n = f.getName().toLowerCase();
            try {
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof Map<?, ?> map
                        && (n.contains("world") || n.contains("level") || n.contains("attached")
                        || n.contains("map") || n.contains("cache"))) {
                    map.clear();
                } else if (v instanceof Collection<?> col
                        && (n.contains("world") || n.contains("level") || n.contains("all")
                        || n.contains("instance"))) {
                    // Prefer clearing map entries inside list members rather than nuking the registry.
                    for (Object entry : col) {
                        if (entry != null) {
                            clearInstanceMaps(entry);
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearInstanceMaps(Object owner) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(owner);
                    if (v instanceof Map<?, ?> map) {
                        map.clear();
                    } else if (v instanceof Collection<?> col) {
                        String n = f.getName().toLowerCase();
                        if (n.contains("world") || n.contains("level") || n.contains("attached")) {
                            col.clear();
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // MineColonies — JobBasedRecipeCategory / GenericRecipe entity caches
    // -------------------------------------------------------------------------

    private static void clearMinecoloniesRecipeEntities() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_MINECOLONIES)) {
            return;
        }
        if (!isAnyClassPresent(MINECOLONIES_RECIPE)) {
            return;
        }
        for (String name : MINECOLONIES_RECIPE) {
            try {
                Class<?> type = Class.forName(name);
                nullStaticEntityLikeFields(type);
                // Static category instances / caches holding entities.
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v == null) {
                            continue;
                        }
                        if (type.isInstance(v)) {
                            nullEntityLikeFieldsLoose(v);
                        } else if (v instanceof Map<?, ?> map) {
                            for (Object o : map.values()) {
                                if (o != null) {
                                    nullEntityLikeFieldsLoose(o);
                                }
                            }
                        } else if (v instanceof Collection<?> col) {
                            for (Object o : col) {
                                if (o != null) {
                                    nullEntityLikeFieldsLoose(o);
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
        // Soft: walk JEI runtime recipe categories when present and null entity-ish fields.
        nullEntityFieldsOnJeiMinecoloniesCategories();
    }

    private static void nullEntityFieldsOnJeiMinecoloniesCategories() {
        try {
            Class<?> internal = Class.forName("mezz.jei.common.Internal");
            Object runtime = null;
            for (String mName : new String[]{"getJeiRuntime", "getRuntime"}) {
                try {
                    Method m = internal.getMethod(mName);
                    if (Modifier.isStatic(m.getModifiers())) {
                        runtime = m.invoke(null);
                        break;
                    }
                } catch (Throwable ignored) {
                }
            }
            if (runtime == null) {
                return;
            }
            Method getRecipeManager = runtime.getClass().getMethod("getRecipeManager");
            Object manager = getRecipeManager.invoke(runtime);
            if (manager == null) {
                return;
            }
            // createRecipeCategoryLookup / getRecipeCategories vary by JEI version — soft walk fields.
            for (Field f : manager.getClass().getDeclaredFields()) {
                if (f.getType().isPrimitive()) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(manager);
                    walkAndNullMinecoloniesEntities(v, new IdentityHashMap<>());
                } catch (Throwable ignored) {
                }
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void walkAndNullMinecoloniesEntities(Object node, IdentityHashMap<Object, Boolean> seen) {
        if (node == null || seen.containsKey(node)) {
            return;
        }
        seen.put(node, Boolean.TRUE);
        if (seen.size() > 512) {
            return;
        }
        String cn = node.getClass().getName();
        if (cn.contains("minecolonies") && (cn.contains("Recipe") || cn.contains("Category"))) {
            nullEntityLikeFieldsLoose(node);
        }
        if (node instanceof Map<?, ?> map) {
            for (Object o : map.values()) {
                walkAndNullMinecoloniesEntities(o, seen);
            }
            return;
        }
        if (node instanceof Collection<?> col) {
            for (Object o : col) {
                walkAndNullMinecoloniesEntities(o, seen);
            }
            return;
        }
        if (node instanceof Object[] arr) {
            for (Object o : arr) {
                walkAndNullMinecoloniesEntities(o, seen);
            }
        }
    }

    private static void nullEntityLikeFieldsLoose(Object owner) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                boolean nameHint = n.contains("citizen") || n.contains("entity") || n.equals("requiredentity")
                        || n.contains("living");
                boolean typeHint = f.getType().getName().contains("Entity")
                        || f.getType().getName().contains("Citizen");
                if (!nameHint && !typeHint) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(owner);
                    if (v != null && !f.getType().isEnum()) {
                        f.set(owner, null);
                    }
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // PneumaticCraft — ArmorMainScreen.upgradeOptions
    // -------------------------------------------------------------------------

    private static void clearPncArmorUpgradeOptions() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_PNC)) {
            return;
        }
        try {
            Class<?> type = Class.forName(PNC_ARMOR_MAIN_SCREEN);
            Object instance = resolveSingleton(type);
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
            clearNamedCollectionField(instance, "upgradeOptions", "upgrade_options", "options");
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void clearNamedCollectionField(Object owner, String... nameHints) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                boolean match = false;
                for (String hint : nameHints) {
                    if (n.equals(hint.toLowerCase()) || n.contains(hint.toLowerCase())) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
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
    // Twilight Forest — ENTITY_MAP + HydraModel.hydra
    // -------------------------------------------------------------------------

    private static void clearTwilightForestEntityMapAndHydra() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_TWILIGHTFOREST)) {
            return;
        }
        for (String name : TWILIGHT_ENTITY_RENDERER) {
            try {
                Class<?> type = Class.forName(name);
                // Static maps.
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    String n = f.getName();
                    if (!(n.equals("ENTITY_MAP") || n.toLowerCase().contains("entity_map")
                            || n.toLowerCase().contains("entitymap"))) {
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
                // Instance maps on static renderer refs / JEI category fields.
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v != null && type.isInstance(v)) {
                            clearNamedMapField(type, v, "ENTITY_MAP", "entity_map");
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        for (String catName : TWILIGHT_TRANSFORM_CATEGORY) {
            try {
                Class<?> cat = Class.forName(catName);
                for (Field f : cat.getDeclaredFields()) {
                    if (f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("entityrenderer") || n.contains("renderer") || n.contains("entity"))) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object renderer = Modifier.isStatic(f.getModifiers()) ? f.get(null) : null;
                        if (renderer == null && !Modifier.isStatic(f.getModifiers())) {
                            // Skip instance fields without an owner.
                            continue;
                        }
                        if (renderer != null) {
                            clearNamedMapField(renderer.getClass(), renderer, "ENTITY_MAP", "entity_map");
                        }
                    } catch (Throwable ignored) {
                    }
                }
                // Soft: static category instance.
                Object singleton = resolveSingleton(cat);
                if (singleton != null) {
                    for (Field f : cat.getDeclaredFields()) {
                        if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                            continue;
                        }
                        try {
                            f.setAccessible(true);
                            Object renderer = f.get(singleton);
                            if (renderer != null) {
                                clearNamedMapField(renderer.getClass(), renderer, "ENTITY_MAP", "entity_map");
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        // HydraModel.hydra — null on model instances reachable from entity renderers.
        for (String name : TWILIGHT_HYDRA_MODEL) {
            try {
                Class<?> modelClass = Class.forName(name);
                nullStaticEntityLikeFields(modelClass);
                Minecraft mc = Minecraft.getInstance();
                if (mc == null || mc.getEntityRenderDispatcher() == null) {
                    continue;
                }
                EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
                Class<?> d = dispatcher.getClass();
                while (d != null && d != Object.class) {
                    for (Field f : d.getDeclaredFields()) {
                        if (f.getType().isPrimitive()) {
                            continue;
                        }
                        try {
                            f.setAccessible(true);
                            Object v = f.get(dispatcher);
                            if (v instanceof Map<?, ?> map) {
                                for (Object renderer : map.values()) {
                                    nullHydraOnRenderer(renderer, modelClass);
                                }
                            } else if (v != null) {
                                nullHydraOnRenderer(v, modelClass);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    d = d.getSuperclass();
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void nullHydraOnRenderer(Object renderer, Class<?> modelClass) {
        if (renderer == null) {
            return;
        }
        Class<?> type = renderer.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (f.getType().isPrimitive()) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object model = f.get(renderer);
                    if (model == null) {
                        continue;
                    }
                    if (modelClass.isInstance(model) || model.getClass().getName().contains("Hydra")) {
                        nullNamedField(model, "hydra", "entity", "currentEntity");
                    }
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    private static void nullNamedField(Object owner, String... names) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                for (String want : names) {
                    if (n.equals(want.toLowerCase()) || n.contains(want.toLowerCase())) {
                        try {
                            f.setAccessible(true);
                            f.set(owner, null);
                        } catch (Throwable ignored) {
                        }
                        break;
                    }
                }
            }
            type = type.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // BetterF3 — LocationModule chunk fields
    // -------------------------------------------------------------------------

    private static void clearBetterF3LocationChunk() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_BETTERF3)) {
            return;
        }
        if (!isAnyClassPresent(BETTERF3_LOCATION)) {
            return;
        }
        for (String name : BETTERF3_LOCATION) {
            try {
                Class<?> type = Class.forName(name);
                nullStaticChunkFields(type);
                // Module instances held on BaseModule / Utils lists.
                for (String reg : BETTERF3_MODULES) {
                    try {
                        Class<?> regType = Class.forName(reg);
                        for (Field f : regType.getDeclaredFields()) {
                            if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                                continue;
                            }
                            try {
                                f.setAccessible(true);
                                Object v = f.get(null);
                                if (v instanceof Collection<?> col) {
                                    for (Object mod : col) {
                                        if (mod != null && type.isInstance(mod)) {
                                            nullChunkFieldsOn(mod);
                                        }
                                    }
                                } else if (v instanceof Map<?, ?> map) {
                                    for (Object mod : map.values()) {
                                        if (mod != null && type.isInstance(mod)) {
                                            nullChunkFieldsOn(mod);
                                        }
                                    }
                                } else if (v != null && type.isInstance(v)) {
                                    nullChunkFieldsOn(v);
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                    } catch (ClassNotFoundException ignored) {
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void nullStaticChunkFields(Class<?> type) {
        for (Field f : type.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                continue;
            }
            String n = f.getName().toLowerCase();
            if (!(n.contains("chunk") || n.contains("future"))) {
                continue;
            }
            try {
                f.setAccessible(true);
                f.set(null, null);
            } catch (Throwable ignored) {
            }
        }
    }

    private static void nullChunkFieldsOn(Object owner) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                if (!(n.contains("chunk") || n.contains("future"))) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    f.set(owner, null);
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    // -------------------------------------------------------------------------
    // Beans Backpacks — EnderStorage.MAP
    // -------------------------------------------------------------------------

    /**
     * @param playerUuid when non-null, remove only that UUID entry; otherwise clear the map (logout).
     */
    private static void clearBeansBackpacksEnderStorage(java.util.UUID playerUuid) {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_BEANSBACKPACKS)) {
            return;
        }
        // Direct EnderStorage class.
        for (String name : new String[]{
                "com.beansgalaxy.backpacks.data.EnderStorage",
                "com.beansgalaxy.backpacks.inventory.EnderStorage"
        }) {
            try {
                Class<?> type = Class.forName(name);
                clearOrRemoveMap(type, null, playerUuid, "MAP", "map", "ENDER", "storage");
                Object singleton = resolveSingleton(type);
                if (singleton != null) {
                    clearOrRemoveMap(type, singleton, playerUuid, "MAP", "map");
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        // CommonAtClient.getEnderStorage().MAP
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
            if (storage == null) {
                Field sf = findStaticField(client, "enderStorage");
                if (sf == null) {
                    sf = findStaticField(client, "ENDER_STORAGE");
                }
                if (sf != null) {
                    storage = sf.get(null);
                }
            }
            if (storage != null) {
                clearOrRemoveMap(storage.getClass(), storage, playerUuid, "MAP", "map");
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
    }

    private static void clearOrRemoveMap(Class<?> type, Object owner, java.util.UUID playerUuid,
                                         String... nameHints) {
        for (Field f : type.getDeclaredFields()) {
            if (owner == null && !Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (owner != null && Modifier.isStatic(f.getModifiers())) {
                // Prefer instance fields when owner is provided, but also allow static.
            }
            String n = f.getName();
            boolean match = false;
            for (String hint : nameHints) {
                if (n.equals(hint) || n.equalsIgnoreCase(hint) || n.toLowerCase().contains(hint.toLowerCase())) {
                    match = true;
                    break;
                }
            }
            if (!match && !Map.class.isAssignableFrom(f.getType())) {
                continue;
            }
            if (!match && Map.class.isAssignableFrom(f.getType()) && !n.equals("MAP") && !n.equalsIgnoreCase("map")) {
                continue;
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

    // -------------------------------------------------------------------------
    // Serene Seasons — LevelRenderer snow/rain level residual
    // -------------------------------------------------------------------------

    private static void clearSereneSeasonsClientSnowField() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_SERENESEASONS)) {
            return;
        }
        if (!isAnyClassPresent(SERENE_SEASON_HANDLER)
                && !isAnyClassPresent(new String[]{"sereneseasons.SereneSeasons", "sereneseasons.init.ModConfig"})) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.levelRenderer == null) {
                return;
            }
            Object renderer = mc.levelRenderer;
            Class<?> type = renderer.getClass();
            while (type != null && type != Object.class) {
                for (Field f : type.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    // Serene Seasons mixin field renderSnowAndRain_level (and similar).
                    boolean nameHint = n.contains("snow") || n.contains("rain")
                            || n.contains("renderSnowAndRain".toLowerCase())
                            || (n.contains("level") && (n.contains("season") || n.contains("snow") || n.contains("rain")));
                    boolean typeHint = f.getType().getName().contains("Level")
                            && (n.contains("snow") || n.contains("rain") || n.contains("season")
                            || n.equals("rendersnowandrain_level"));
                    if (!nameHint && !typeHint) {
                        // Also any non-vanilla Level field with "level" in name that is not the normal private level.
                        if (!(n.contains("rendersnowandrain") || n.endsWith("_level") && n.contains("snow"))) {
                            continue;
                        }
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(renderer);
                        if (v != null && f.getType().getName().contains("Level")) {
                            f.set(renderer, null);
                        }
                    } catch (Throwable ignored) {
                    }
                }
                type = type.getSuperclass();
            }
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Mowzie's Mobs — boss music + MMModelAnimator entity
    // -------------------------------------------------------------------------

    private static void clearMowziesBossMusicAndAnimator() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_MOWZIES)) {
            return;
        }
        if (!isAnyClassPresent(MOWZIES_CLIENT_PROXY) && !isAnyClassPresent(MOWZIES_BOSS_MUSIC)
                && !isAnyClassPresent(MOWZIES_MODEL_ANIMATOR)) {
            return;
        }
        // Clear ClientProxy.sunblockSounds list.
        for (String name : MOWZIES_CLIENT_PROXY) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("sunblock") || n.contains("sound"))) {
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
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        // BossMusicPlayer.bossMusic / currentMusic — setBoss(null) or null field.
        for (String name : MOWZIES_BOSS_MUSIC) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("music") || n.contains("boss") || n.contains("current")
                            || n.contains("instance"))) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object music = f.get(null);
                        if (music == null) {
                            continue;
                        }
                        // Prefer setBoss(null).
                        boolean set = false;
                        for (Method m : music.getClass().getMethods()) {
                            if (m.getName().equals("setBoss") && m.getParameterCount() == 1) {
                                try {
                                    m.invoke(music, new Object[]{null});
                                    set = true;
                                    break;
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                        if (!set) {
                            nullNamedField(music, "boss", "entity", "currentBoss");
                            // Or null the static field itself when it is the music holder.
                            if (n.contains("instance") || n.contains("current") || n.equals("bossmusic")) {
                                try {
                                    if (!Modifier.isFinal(f.getModifiers())) {
                                        f.set(null, null);
                                    }
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
        // MMModelAnimator entity residual (static instances / static entity field).
        for (String name : MOWZIES_MODEL_ANIMATOR) {
            try {
                Class<?> type = Class.forName(name);
                nullStaticEntityLikeFields(type);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object animator = f.get(null);
                        if (animator != null && type.isInstance(animator)) {
                            nullEntityLikeFieldsLoose(animator);
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
    // AE2 Wireless Terminals — CraftingTerminalHandler.players + creative tab
    // -------------------------------------------------------------------------

    private static final String[] AE2WT_CRAFTING_TERMINAL_HANDLER = {
            "de.mari_023.ae2wtlib.wct.CraftingTerminalHandler",
            "de.mari_023.ae2wtlib.terminal.CraftingTerminalHandler"
    };
    private static final String[] AE2WT_CREATIVE_TAB = {
            "de.mari_023.ae2wtlib.AE2WTLibCreativeTab",
            "de.mari_023.ae2wtlib.AE2wtlibCreativeTab"
    };

    /**
     * Soft stand-in for ATL: remove prior player from {@code CraftingTerminalHandler#players}
     * and clear creative-tab ItemStack list so stacks cannot pin a ClientLevel.
     */
    private static void clearAe2wtClient(Player newPlayer) {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_AE2WT)) {
            return;
        }
        for (String name : AE2WT_CRAFTING_TERMINAL_HANDLER) {
            try {
                Class<?> type = Class.forName(name);
                Field playersField = findStaticField(type, "players");
                if (playersField == null) {
                    for (Field f : type.getDeclaredFields()) {
                        if (!Modifier.isStatic(f.getModifiers())) {
                            continue;
                        }
                        if (Map.class.isAssignableFrom(f.getType())
                                || f.getName().equalsIgnoreCase("players")) {
                            playersField = f;
                            playersField.setAccessible(true);
                            break;
                        }
                    }
                }
                if (playersField == null) {
                    continue;
                }
                Object mapObj = playersField.get(null);
                if (!(mapObj instanceof Map<?, ?> map)) {
                    continue;
                }
                // Clear entirely on leave; WeakHashMap may still retain until GC keys die.
                try {
                    LocalPlayer old = Minecraft.getInstance().player;
                    if (old != null && old != newPlayer) {
                        map.remove(old);
                    }
                } catch (Throwable ignored) {
                }
                if (newPlayer == null) {
                    try {
                        map.clear();
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        for (String name : AE2WT_CREATIVE_TAB) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.equals("items") || n.contains("item") || n.contains("display"))) {
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
    // badpackets — ChannelRegistry handlers
    // -------------------------------------------------------------------------

    private static final String[] BADPACKETS_CHANNEL_REGISTRY = {
            "lol.bai.badpackets.impl.registry.ChannelRegistry",
            "lol.bai.badpackets.api.PacketSender"
    };
    private static final String[] BADPACKETS_CLIENT_HANDLER = {
            "lol.bai.badpackets.impl.handler.ClientPacketHandler",
            "lol.bai.badpackets.impl.handler.PacketHandler"
    };

    /**
     * Soft clear of client-side channel handler sets and best-effort {@code onDisconnect}.
     */
    private static void clearBadpacketsClient() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_BADPACKETS)) {
            return;
        }
        for (String name : BADPACKETS_CHANNEL_REGISTRY) {
            try {
                Class<?> type = Class.forName(name);
                for (String channelName : new String[]{"S2C", "C2S", "s2c", "c2s", "CLIENT", "SERVER"}) {
                    Field channelField = findStaticField(type, channelName);
                    if (channelField == null) {
                        continue;
                    }
                    Object channel = channelField.get(null);
                    if (channel != null) {
                        clearHandlersFieldOn(channel);
                    }
                }
                // Also clear any static handler collections on the class itself.
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
                            clearHandlersFieldOn(v);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        for (String name : BADPACKETS_CLIENT_HANDLER) {
            try {
                Class<?> type = Class.forName(name);
                Object instance = resolveSingleton(type);
                if (instance != null) {
                    invokeNoArg(instance, "onDisconnect", "handleDisconnect", "disconnect", "clear");
                }
                // Static clear methods.
                for (String mName : new String[]{"onDisconnect", "clear", "reset"}) {
                    try {
                        Method m = type.getMethod(mName);
                        if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0) {
                            m.invoke(null);
                        }
                    } catch (NoSuchMethodException ignored) {
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    private static void clearHandlersFieldOn(Object owner) {
        Class<?> type = owner.getClass();
        while (type != null && type != Object.class) {
            for (Field f : type.getDeclaredFields()) {
                if (f.getType().isPrimitive()) {
                    continue;
                }
                String n = f.getName().toLowerCase();
                if (!(n.contains("handler") || n.equals("handlers") || n.contains("receiver")
                        || n.contains("listener"))) {
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

    private static void invokeNoArg(Object owner, String... methodNames) {
        for (String mName : methodNames) {
            try {
                Method m = owner.getClass().getMethod(mName);
                if (m.getParameterCount() == 0) {
                    m.invoke(owner);
                    return;
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Throwable ignored) {
            }
            try {
                Method m = owner.getClass().getDeclaredMethod(mName);
                if (m.getParameterCount() == 0) {
                    m.setAccessible(true);
                    m.invoke(owner);
                    return;
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Blue Skies — lastRidden + dungeon ambient handler
    // -------------------------------------------------------------------------

    private static final String[] BLUE_SKIES_CLIENT_EVENTS = {
            "com.legacy.blue_skies.client.events.SkiesClientEvents",
            "com.legacy.blue_skies.client.SkiesClientEvents"
    };
    private static final String[] BLUE_SKIES_AMBIENT = {
            "com.legacy.blue_skies.client.audio.ambient.DungeonAmbientSoundHandler"
    };

    private static void clearBlueSkiesClient() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_BLUESKIES)) {
            return;
        }
        // Null mixin field blue_skies$lastRidden (static or instance on connection).
        try {
            Class<?> cpl = Class.forName("net.minecraft.client.multiplayer.ClientPacketListener");
            for (String fname : new String[]{"blue_skies$lastRidden", "lastRidden", "atl$lastRidden"}) {
                Field f = findStaticField(cpl, fname);
                if (f != null) {
                    try {
                        f.set(null, null);
                    } catch (Throwable ignored) {
                    }
                }
            }
            // Instance field on current connection.
            try {
                var connection = Minecraft.getInstance().getConnection();
                if (connection != null) {
                    Class<?> type = connection.getClass();
                    while (type != null && type != Object.class) {
                        for (Field f : type.getDeclaredFields()) {
                            if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                                continue;
                            }
                            String n = f.getName();
                            if (!(n.contains("lastRidden") || n.contains("blue_skies"))) {
                                continue;
                            }
                            try {
                                f.setAccessible(true);
                                if (f.getType().getName().contains("Entity")
                                        || n.toLowerCase().contains("ridden")) {
                                    f.set(connection, null);
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                        type = type.getSuperclass();
                    }
                }
            } catch (Throwable ignored) {
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable ignored) {
        }
        // Null or replace dungeonAmbientSoundHandler so it cannot pin old LocalPlayer.
        for (String name : BLUE_SKIES_CLIENT_EVENTS) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("ambient") || n.contains("dungeon") || n.contains("soundhandler")
                            || n.contains("sound_handler"))) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        if (!Modifier.isFinal(f.getModifiers())) {
                            f.set(null, null);
                        } else {
                            // Final: try clear player field inside the handler instance.
                            Object handler = f.get(null);
                            if (handler != null) {
                                nullNamedField(handler, "player", "entity", "localPlayer");
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        // Soft probe ambient class statics.
        for (String name : BLUE_SKIES_AMBIENT) {
            try {
                Class<?> type = Class.forName(name);
                nullStaticEntityLikeFields(type);
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Iceberg — EntityCollector + CustomItemRenderer
    // -------------------------------------------------------------------------

    private static final String[] ICEBERG_ENTITY_COLLECTOR = {
            "com.anthonyhilyard.iceberg.util.EntityCollector",
            "com.anthonyhilyard.iceberg.EntityCollector"
    };
    private static final String[] ICEBERG_CUSTOM_ITEM_RENDERER = {
            "com.anthonyhilyard.iceberg.renderer.CustomItemRenderer",
            "com.anthonyhilyard.iceberg.client.CustomItemRenderer"
    };

    private static void clearIcebergClient() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_ICEBERG)) {
            return;
        }
        for (String name : ICEBERG_ENTITY_COLLECTOR) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("level") || n.contains("world") || n.contains("map")
                            || n.contains("wrapped"))) {
                        if (!Map.class.isAssignableFrom(f.getType())) {
                            continue;
                        }
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Map<?, ?> map) {
                            map.clear();
                        } else if (v instanceof Collection<?> col) {
                            col.clear();
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
        for (String name : ICEBERG_CUSTOM_ITEM_RENDERER) {
            try {
                Class<?> type = Class.forName(name);
                for (String fname : new String[]{
                        "horse", "armorStand", "entity", "blockEntity",
                        "armor_stand", "block_entity", "armorstand", "blockentity"
                }) {
                    Field f = findStaticField(type, fname);
                    if (f == null) {
                        continue;
                    }
                    try {
                        f.set(null, null);
                    } catch (Throwable ignored) {
                    }
                }
                // Fallback: any static Entity / BlockEntity residual.
                nullStaticEntityLikeFields(type);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String cn = f.getType().getName();
                    if (cn.contains("Entity") || cn.contains("BlockEntity") || cn.contains("Horse")
                            || cn.contains("ArmorStand")) {
                        try {
                            f.setAccessible(true);
                            f.set(null, null);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Just Enough Resources — MobEntry entity residual (best-effort statics)
    // -------------------------------------------------------------------------

    private static final String[] JER_ENTRY_CLASSES = {
            "jeresources.entry.MobEntry",
            "jeresources.entry.AbstractVillagerEntry",
            "jeresources.entry.VillagerEntry",
            "jeresources.util.MobTableBuilder",
            "jeresources.jei.mob.MobCategory",
            "jeresources.jei.villager.VillagerCategory"
    };

    private static void clearJerEntityResiduals() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_JER)) {
            return;
        }
        for (String name : JER_ENTRY_CLASSES) {
            try {
                Class<?> type = Class.forName(name);
                // Null static entity-like fields / clear static maps of entries.
                nullStaticEntityLikeFields(type);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        Object v = f.get(null);
                        if (v instanceof Map<?, ?> map) {
                            for (Object o : map.values()) {
                                if (o != null) {
                                    nullNamedField(o, "entity", "livingEntity", "villager", "mob");
                                    nullEntityLikeFieldsLoose(o);
                                }
                            }
                        } else if (v instanceof Collection<?> col) {
                            for (Object o : col) {
                                if (o != null) {
                                    nullNamedField(o, "entity", "livingEntity", "villager", "mob");
                                    nullEntityLikeFieldsLoose(o);
                                }
                            }
                        } else if (v != null && (f.getName().equalsIgnoreCase("level")
                                || f.getName().equalsIgnoreCase("world"))) {
                            if (!Modifier.isFinal(f.getModifiers())) {
                                f.set(null, null);
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

    // -------------------------------------------------------------------------
    // Mana and Artifice — FeyArmorItem.renderEntity
    // -------------------------------------------------------------------------

    private static final String[] MNA_FEY_ARMOR = {
            "com.mna.items.armor.FeyArmorItem",
            "com.mna.items.renderers.FeyArmorRenderer"
    };

    private static void clearMnaRenderEntity() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_MNA)) {
            return;
        }
        for (String name : MNA_FEY_ARMOR) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("renderentity") || n.equals("render_entity")
                            || n.contains("entity") || n.contains("animatable"))) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        f.set(null, null);
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // NuclearCraft — TooltipHandler.processedEvent
    // -------------------------------------------------------------------------

    private static final String[] NC_TOOLTIP_HANDLER = {
            "igentuman.nc.handler.event.client.TooltipHandler",
            "igentuman.nuclearcraft.handler.event.client.TooltipHandler",
            "igentuman.nc.client.TooltipHandler"
    };

    private static void clearNuclearcraftTooltipEvent() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_NUCLEARCRAFT)) {
            return;
        }
        for (String name : NC_TOOLTIP_HANDLER) {
            try {
                Class<?> type = Class.forName(name);
                for (Field f : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) {
                        continue;
                    }
                    String n = f.getName().toLowerCase();
                    if (!(n.contains("processed") || n.contains("event") || n.contains("tooltip"))) {
                        continue;
                    }
                    try {
                        f.setAccessible(true);
                        f.set(null, null);
                    } catch (Throwable ignored) {
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Throwable ignored) {
            }
        }
    }

    // -------------------------------------------------------------------------
    // Vanilla residual soft clear
    // -------------------------------------------------------------------------

    /**
     * Soft residual clear: null {@code Minecraft#crosshairPickEntity}/{@code hitResult} and clear
     * {@code ItemStack.EMPTY} entity representation so a prior entity/level can be collected.
     */
    private static void clearMcVanillaClientResiduals() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.LEAK_MC_VANILLA)) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            mc.crosshairPickEntity = null;
            mc.hitResult = null;
        } catch (Throwable ignored) {
        }
        try {
            Class<?> stackClass = Class.forName("net.minecraft.world.item.ItemStack");
            Field empty = findStaticField(stackClass, "EMPTY");
            if (empty != null) {
                Object emptyStack = empty.get(null);
                if (emptyStack != null) {
                    // Prefer setEntityRepresentation(null).
                    try {
                        Method set = stackClass.getMethod("setEntityRepresentation",
                                Class.forName("net.minecraft.world.entity.Entity"));
                        set.invoke(emptyStack, new Object[]{null});
                    } catch (Throwable t) {
                        nullNamedField(emptyStack, "entityRepresentation", "entity");
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Reflect helpers
    // -------------------------------------------------------------------------

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
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getName().equals(name)) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }
}
