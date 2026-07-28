package org.omnifix.kernel.feature;

import org.omnifix.kernel.StackDomain;

/**
 * Built-in FeatureUnit catalog for Wave-0 foundation, VS×IP compat, Create×IP, and Wave-1 Band A
 * ModernFix bugfix ports. Additional units from {@code RESEARCH_MASTER.md} register here as they
 * are implemented.
 */
public final class FeatureUnits {

    // --- VS × Immersive Portals (existing OmniFix layer) ---
    public static final String VP_FRUSTUM = "vp.frustum_deadloop";
    public static final String VP_PORTAL_CAMERA = "vp.portal_camera";
    public static final String VP_PORTAL_FOG = "vp.portal_fog";
    public static final String VP_SHIP_UNLOAD_CCE = "vp.ship_unload_cce";
    public static final String VP_SHIP_VIS = "vp.ship_visibility";
    public static final String VP_SHIP_TRANSIT = "vp.ship_transit";
    public static final String VP_ENTITY_DRAG = "vp.entity_drag";
    public static final String VP_DIAGNOSTICS = "vp.diagnostics";

    // --- Create × Immersive Portals ---
    public static final String CREATE_IP_TRACKS_A = "create.ip_tracks_nether";
    /** Block-less IP general portals (wand/command) — placement hook on TrackBlock. */
    public static final String CREATE_IP_TRACKS_B = "create.ip_tracks_b";
    /**
     * Train entity transit: Create owns dimensional carriages; IP must not steal
     * {@code CarriageContraptionEntity} or seated passengers, and player dismount teleports
     * must go through IP's teleport manager.
     */
    public static final String CREATE_IP_TRAIN_TRANSIT = "create.ip_train_transit";

    // --- VS × IP interaction ---
    /** VS #1525: feed IP remote hit into VS originalCrosshairTarget on use. */
    public static final String VP_CROSS_PORTAL_INTERACT = "vp.cross_portal_interact";
    /**
     * Create {@code RaycastHelper.rayTraceRange} (Clockwork wanderwand etc.) must use
     * {@code clipIncludeShips} so ship blocks are selectable — bypasses fragile Level.clip paths.
     */
    public static final String VS_CLOCKWORK_CLIP = "vs.clockwork_clip";

    // --- Vanilla Mojira high-value self-contained fixes ---
    public static final String VANILLA_SPECTATOR_STUCK = "vanilla.spectator_stuck_effects";
    public static final String VANILLA_SPECTATOR_BED = "vanilla.spectator_bed";
    public static final String VANILLA_MENDING_BREAK = "vanilla.mending_break_progress";
    public static final String VANILLA_SAPLING_2X2 = "vanilla.sapling_2x2";
    public static final String VANILLA_FULLSCREEN = "vanilla.fullscreen_state";
    public static final String VANILLA_SPECTATOR_BREAK = "vanilla.spectator_break";
    public static final String VANILLA_SPECTATOR_PROJECTILE = "vanilla.spectator_projectile";
    public static final String VANILLA_SP_CHAT_SPAM = "vanilla.sp_chat_spam";
    public static final String VANILLA_RAW_COPPER_SOUND = "vanilla.raw_copper_sound";
    public static final String VANILLA_CREEPER_DEFUSE = "vanilla.creeper_defuse";
    public static final String VANILLA_TITLE_CLEAR = "vanilla.title_clear";
    public static final String VANILLA_HOTBAR_RESPAWN = "vanilla.hotbar_respawn";
    public static final String VANILLA_USE_SLOW_AFTER_DROP = "vanilla.use_slow_after_drop";
    public static final String VANILLA_BREAK_DELAY_DROP_TOOL = "vanilla.break_delay_drop_tool";
    public static final String VANILLA_CROSSBOW_OFFHAND = "vanilla.crossbow_offhand";
    public static final String VANILLA_DRAG_STACK_INVISIBLE = "vanilla.drag_stack_invisible";
    public static final String VANILLA_MOUSE_INVENTORY = "vanilla.mouse_inventory";
    public static final String VANILLA_LEATHER_SKELETON_STRAY = "vanilla.leather_skeleton_stray";
    public static final String VANILLA_ZOMBVILLAGER_JOCKEY = "vanilla.zombvillager_jockey";
    public static final String VANILLA_CREATIVE_LADDER_SLOW = "vanilla.creative_ladder_slow";
    public static final String VANILLA_DOUBLE_SNEAK_ANIM = "vanilla.double_sneak_anim";
    public static final String VANILLA_F3_DOUBLE = "vanilla.f3_double";
    public static final String VANILLA_SPECTATOR_CONSUME = "vanilla.spectator_consume";
    public static final String VANILLA_LIGHTNING_DROPS = "vanilla.lightning_drops";
    public static final String VANILLA_ARMORSTAND_PARTICLES = "vanilla.armorstand_particles";
    public static final String VANILLA_FISHING_KILL_COUNT = "vanilla.fishing_kill_count";
    public static final String VANILLA_PUFFERFISH_DYING = "vanilla.pufferfish_dying";
    public static final String VANILLA_SHIELD_HURT_SOUND = "vanilla.shield_hurt_sound";
    public static final String VANILLA_BOAT_SLIME_HOVER = "vanilla.boat_slime_hover";
    public static final String VANILLA_OFFHAND_ROD_PUNCH = "vanilla.offhand_rod_punch";
    public static final String VANILLA_RIPTIDE_OFFHAND = "vanilla.riptide_offhand";
    public static final String VANILLA_WOLF_HEARTS = "vanilla.wolf_hearts";
    public static final String VANILLA_STRIDER_SADDLE_PEACEFUL = "vanilla.strider_saddle_peaceful";
    public static final String VANILLA_PEACEFUL_SATURATION = "vanilla.peaceful_saturation";
    public static final String VANILLA_CMD_MINECART_NBT = "vanilla.cmd_minecart_nbt";
    public static final String VANILLA_UNKNOWN_PASSENGER = "vanilla.unknown_passenger";
    public static final String VANILLA_CTRL_Q_CRAFT = "vanilla.ctrl_q_craft";
    public static final String VANILLA_XP_BAR_VANISH = "vanilla.xp_bar_vanish";
    public static final String VANILLA_HIGH_SPEED_FLICKER = "vanilla.high_speed_flicker";
    public static final String VANILLA_ARMORSTAND_DARK = "vanilla.armorstand_dark";
    public static final String VANILLA_DRAGON_VOID_PORTAL = "vanilla.dragon_void_portal";
    public static final String VANILLA_DIM_TELEPORT_STATE = "vanilla.dim_teleport_state";
    public static final String VANILLA_GROUP_AI_DEATH = "vanilla.group_ai_death";
    public static final String VANILLA_ENTITY_ANIM_FREEZE = "vanilla.entity_anim_freeze";
    public static final String VANILLA_FISHING_LINE_CROUCH = "vanilla.fishing_line_crouch";
    public static final String VANILLA_POTTABLE_STAT = "vanilla.pottable_stat";
    public static final String VANILLA_SKELETON_LOOK = "vanilla.skeleton_look";
    public static final String VANILLA_ENDROD_CACTUS = "vanilla.endrod_cactus";
    public static final String VANILLA_DROWN_BUBBLES = "vanilla.drown_bubbles";
    /** MC-237493 — force telemetry off (privacy / modded noise). */
    public static final String VANILLA_TELEMETRY_DISABLE = "vanilla.telemetry_disable";
    /** MC-22882 — Mac: drop-all must use physical Ctrl, not Super/Cmd. */
    public static final String VANILLA_MAC_CTRL_Q = "vanilla.mac_ctrl_q";
    /** MC-122477 — Linux: suppress charTyped that re-types open-chat key into ChatScreen. */
    public static final String VANILLA_LINUX_CHAT_T = "vanilla.linux_chat_t";
    /**
     * MC-59810 — macOS: vanilla converts Ctrl+left-click to right-click (GUI context-menu emulation).
     * That breaks sprint(Ctrl)+break. Strip control mod from mouse events while in-world.
     */
    public static final String VANILLA_MAC_SPRINT_BREAK = "vanilla.mac_sprint_break";

    // --- Wave 1 Band A: ModernFix bugfix ports (FU-MF-* catalog ids) ---
    public static final String MF_BUF_LEAK = "FU-MF-BUF-LEAK";
    public static final String MF_CHUNK_DEADLOCK = "FU-MF-CHUNK-DEADLOCK";
    public static final String MF_CONC_REG = "FU-MF-CONC-REG";
    public static final String MF_DRAGON_LEAK = "FU-MF-DRAGON-LEAK";
    public static final String MF_POSE_STACK = "FU-MF-POSE-STACK";
    public static final String MF_EXP_SCREEN = "FU-MF-EXP-SCREEN";
    public static final String MF_VEHICLE_PKT = "FU-MF-VEHICLE-PKT";
    public static final String MF_MISS_BE = "FU-MF-MISS-BE";
    public static final String MF_MODELDATA_CME = "FU-MF-MODELDATA-CME";
    public static final String MF_PAPER_CHUNK = "FU-MF-PAPER-CHUNK";
    public static final String MF_REGOPS_CME = "FU-MF-REGOPS-CME";
    public static final String MF_REMOVED_DIM = "FU-MF-REMOVED-DIM";
    public static final String MF_SHAPE_CACHE = "FU-MF-SHAPE-CACHE";
    public static final String MF_WORLD_LEAK = "FU-MF-WORLD-LEAK";
    public static final String MF_WORLD_SCREEN = "FU-MF-WORLD-SCREEN";
    /** CoFH FlagManager FLAGS map concurrency crash. */
    public static final String MF_COFH_FLAGS = "FU-MF-COFH-FLAGS";
    /** RecipeBookSettings desync when modded RecipeBookType exists on vanilla connection. */
    public static final String MF_RECIPE_BOOK = "FU-MF-RECIPE-BOOK";
    /** Paper-class ChunkMap unload/scheduling patches (beyond SortedArraySet). */
    public static final String MF_PAPER_CHUNKMAP = "FU-MF-PAPER-CHUNKMAP";
    /** CTM ResourceUtil.metadataCache ConcurrentModificationException. */
    public static final String MF_CTM_CME = "FU-MF-CTM-CME";
    /** Client color/property maps + LivingEntityRenderer.addLayer thread safety. */
    public static final String MF_CLIENT_MAP_SAFETY = "FU-MF-CLIENT-MAP-SAFETY";
    /** ChunkAccess blockEntities map concurrency sanitizer (wrong-thread fail-fast). */
    public static final String MF_BE_THREAD = "FU-MF-BE-THREAD";

    // --- Leaks / network ---
    public static final String LEAK_FORGE_FAKEPLAYER = "leak.forge_fakeplayer";
    public static final String LEAK_CREATE_EXTENDO = "leak.create_extendo";
    public static final String LEAK_CURIOS_CLONE = "leak.curios_clone";
    /** GeckoLib Molang memo / GeoArmorRenderer entity retention (ATL-class). */
    public static final String LEAK_GECKOLIB = "leak.geckolib";
    /** JEI RecipeTransferButton player/container after client clone (ATL-class). */
    public static final String LEAK_JEI = "leak.jei";
    /** FTB Library GuiHelper.BLANK_GUI prevScreen (ATL-class). */
    public static final String LEAK_FTB_LIBRARY = "leak.ftb_library";
    /** EMI EmiHistory screen stacks pin ClientLevel after logout/clone. */
    public static final String LEAK_EMI = "leak.emi";
    /** Entity Model Features LivingEntityRenderer emf$heldIteration residual. */
    public static final String LEAK_EMF = "leak.emf";
    /** Entity Texture Features heldEntity / PLAYER_TEXTURE_MAP residual. */
    public static final String LEAK_ETF = "leak.etf";
    /** Create Crafts & Additions EnergyNetworkManager level instances. */
    public static final String LEAK_CREATEADDITION = "leak.createaddition";
    /** Ars Nouveau clone cap hygiene + CasterTomeRegistry level suppliers. */
    public static final String LEAK_ARS_NOUVEAU = "leak.ars_nouveau";
    /** Iron's Spellbooks ClientMagicData / SpellBar / DeadKingMusic residual. */
    public static final String LEAK_IRONS_SPELLBOOKS = "leak.irons_spellbooks";
    /** JourneyMap EntityDistanceComparator player residual. */
    public static final String LEAK_JOURNEYMAP = "leak.journeymap";
    /** Tombstone LivingEntityRenderer entity mixin residual. */
    public static final String LEAK_TOMBSTONE = "leak.tombstone";
    /** Architectury NetworkManagerImpl clientReceivables player-key leak on clone. */
    public static final String LEAK_ARCHITECTURY = "leak.architectury";
    /** Mouse Tweaks openScreen/handler residual on client clone. */
    public static final String LEAK_MOUSE_TWEAKS = "leak.mouse_tweaks";
    /** FindMe lastRenderedStack ItemStack residual. */
    public static final String LEAK_FINDME = "leak.findme";
    /** Corpse renderer players/skeletons CachedMap residual. */
    public static final String LEAK_CORPSE = "leak.corpse";
    /** Easy Villagers item TE / villager entity caches. */
    public static final String LEAK_EASY_VILLAGERS = "leak.easy_villagers";
    /** Tool Belt cap revive/invalidate on clone. */
    public static final String LEAK_TOOLBELT = "leak.toolbelt";
    /** Traveler's Backpack cap hygiene + layer entity residual. */
    public static final String LEAK_TRAVELERSBACKPACK = "leak.travelersbackpack";
    /** Occultism missing invalidateCaps on clone. */
    public static final String LEAK_OCCULTISM = "leak.occultism";
    /** Citadel ModelAnimator entity + server data residual. */
    public static final String LEAK_CITADEL = "leak.citadel";
    /** Moonlight soft client/level cache residual (best-effort). */
    public static final String LEAK_MOONLIGHT = "leak.moonlight";
    /** Aether DroppedItemCapability owner entity residual (ATL-class). */
    public static final String LEAK_AETHER = "leak.aether";
    /** Alex's Mobs BEACHED_CACHALOT / AMWorldData maps on level unload. */
    public static final String LEAK_ALEXSMOBS = "leak.alexsmobs";
    /** Flywheel WorldAttached world map residual on client leave. */
    public static final String LEAK_FLYWHEEL = "leak.flywheel";
    /** MineColonies JEI recipe-category entity caches on client leave. */
    public static final String LEAK_MINECOLONIES = "leak.minecolonies";
    /** PneumaticCraft ArmorMainScreen upgradeOptions + drone event unregister. */
    public static final String LEAK_PNC = "leak.pnc";
    /** Twilight Forest JEI ENTITY_MAP / HydraModel hydra residual. */
    public static final String LEAK_TWILIGHTFOREST = "leak.twilightforest";
    /** BetterF3 LocationModule chunkFuture residual. */
    public static final String LEAK_BETTERF3 = "leak.betterf3";
    /** Beans Backpacks EnderStorage MAP on clone/logout. */
    public static final String LEAK_BEANSBACKPACKS = "leak.beansbackpacks";
    /** Serene Seasons LevelRenderer snow field + SeasonHandler maps. */
    public static final String LEAK_SERENESEASONS = "leak.sereneseasons";
    /** Mowzie's Mobs boss music + MMModelAnimator entity residual. */
    public static final String LEAK_MOWZIES = "leak.mowzies";
    /** AE2 Wireless Terminals CraftingTerminalHandler players + creative tab items. */
    public static final String LEAK_AE2WT = "leak.ae2wt";
    /** badpackets ChannelRegistry handlers residual on disconnect / server stop. */
    public static final String LEAK_BADPACKETS = "leak.badpackets";
    /** Blue Skies lastRidden + dungeon ambient handler residual. */
    public static final String LEAK_BLUESKIES = "leak.blueskies";
    /** Iceberg EntityCollector level map + CustomItemRenderer entity residual. */
    public static final String LEAK_ICEBERG = "leak.iceberg";
    /** Forbidden Arcanus missing invalidateCaps on death clone. */
    public static final String LEAK_FORBIDDEN_ARCANUS = "leak.forbidden_arcanus";
    /** Just Enough Resources MobEntry / villager entry entity residual. */
    public static final String LEAK_JER = "leak.jer";
    /** Mana and Artifice FeyArmorItem.renderEntity residual. */
    public static final String LEAK_MNA = "leak.mna";
    /** NuclearCraft TooltipHandler.processedEvent residual. */
    public static final String LEAK_NUCLEARCRAFT = "leak.nuclearcraft";
    /** Phosphophyllite ConfigManager.players + isTickingMap residual. */
    public static final String LEAK_PHOSPHOPHYLLITE = "leak.phosphophyllite";
    /** Railcraft ChargeProviderImpl.DISTRIBUTION networks level residual. */
    public static final String LEAK_RAILCRAFT = "leak.railcraft";
    /** Small Ships ChunkMap mixin serverPlayer/list residual. */
    public static final String LEAK_SMALLSHIPS = "leak.smallships";
    /** Vanilla residual: EMPTY entityRepresentation, crosshair/hit, last-damage tick. */
    public static final String LEAK_MC_VANILLA = "leak.mc_vanilla";
    /** CyclopsCore DelegatingDynamicItemAndBlockModel.world residual. */
    public static final String LEAK_CYCLOPS = "leak.cyclops";
    /** EMI Loot EntityEmiStack.entity residual / recreate on level ready. */
    public static final String LEAK_EMI_LOOT = "leak.emi_loot";
    /** LDLib ModularUI.entityPlayer residual. */
    public static final String LEAK_LDLIB = "leak.ldlib";
    public static final String NET_LOGIN_TIMEOUT = "net.login_timeout";
    public static final String NET_READ_TIMEOUT = "net.read_timeout";
    /**
     * CompressionDecoder absolute size limit. Vanilla 1.20.1 checks uncompressed at 8&nbsp;MiB
     * and declares a 2&nbsp;MiB compressed field; OmniFix raises the active ceiling to 16&nbsp;MiB.
     */
    public static final String NET_COMPRESSION_SIZE = "net.compression_size";
    /**
     * ServerGamePacketListenerImpl keep-alive / {@code disconnect.timeout}. Vanilla
     * {@code LATENCY_CHECK_INTERVAL} is 15&nbsp;s; raised to 60&nbsp;s for laggy large packs.
     */
    public static final String NET_PLAY_TIMEOUT = "net.play_timeout";
    /**
     * Custom payload / frame size raise for large-pack Forge channel traffic.
     * Vanilla play S2C custom payload 1&nbsp;MiB, C2S 32&nbsp;KiB, login query 1&nbsp;MiB;
     * PacketEncoder 8&nbsp;MiB; 21-bit frame (3 length bytes). OmniFix raises payload/encode to
     * 16&nbsp;MiB and length framing to 5 bytes. Complements Forge {@code VanillaPacketSplitter}
     * (already splits recipes/tags/advancements/login on forge↔forge); does not reimplement it.
     */
    public static final String NET_PAYLOAD_SPLIT = "net.payload_split";

    // --- Wave 5 correctness-adjacent perf ---
    public static final String PERF_HANDSHAKE = "perf.handshake_stall";
    public static final String PERF_SPIN_WAIT = "perf.loop_spin_waiting";
    /** Avoid reallocating WeightedRandomList on PotentialSpawns when unmodified. */
    public static final String PERF_POTENTIAL_SPAWNS = "perf.potential_spawns_alloc";
    /** Silence recipe reload exception stack storms (log location + message only). */
    public static final String PERF_RECIPE_RELOAD_LOG = "perf.recipe_reload_log";
    /** RegistryObject.get without per-call lambda allocation. */
    public static final String PERF_REGISTRY_OBJECT = "perf.registry_object_get";
    /** Lower util worker thread priority (ForkJoin). */
    public static final String PERF_THREAD_PRIORITY = "perf.thread_priority";
    /** Policy: treat all chat as SECURE (no signing trust UI noise). */
    public static final String FEATURE_CHAT_SIGNING_OFF = "feature.chat_signing_off";
    /** Suppress Linux narrator init stack spam. */
    public static final String FEATURE_NARRATOR_LINUX = "feature.narrator_linux_quiet";
    /** Cache TagEntry elementOrTag() TagOrElementLocation. */
    public static final String PERF_TAG_ID_CACHE = "perf.tag_id_cache";
    /** Deduplicate WallBlock VoxelShape maps for vanilla-property walls. */
    public static final String PERF_WALL_SHAPE_DEDUP = "perf.wall_shape_dedup";
    /** Cache SupportType/Direction.Axis enum arrays in BlockState Cache ctor. */
    public static final String PERF_BLOCKSTATE_ENUM_CACHE = "perf.blockstate_enum_cache";
    /** Memoize CreativeModeTab.buildContents across identical ItemDisplayParameters. */
    public static final String PERF_CREATIVE_TAB = "perf.creative_tab_memoize";
    /** Shrink empty oversized PalettedContainer bit storage after network read. */
    public static final String PERF_COMPACT_BIT_STORAGE = "perf.compact_bit_storage";
    /** Skip canCreateStructure when placement already forbids the chunk (MC-249136-class). */
    public static final String PERF_STRUCTURE_LOCATE = "perf.faster_structure_location";
    /** Drop redundant Forge ObjectHolderRef entries + clear holder stacktraces. */
    public static final String PERF_OBJECT_HOLDER = "perf.object_holder_cleanup";
    /** Remove ineffective Biome temperature position cache (Lithium-class). */
    public static final String PERF_BIOME_TEMP = "perf.biome_temperature_cache";
    /** Bat Halloween date cache + chunk structure-ref map view + ChunkHolder Either without Optional. */
    public static final String PERF_TICKING_CHUNK_ALLOC = "perf.ticking_chunk_alloc";
    /** MappedRegistry byId growth power-of-two instead of +1 each register. */
    public static final String PERF_REGISTRY_GROW = "perf.mojang_registry_grow";
    /** Empty StateHolder neighbour tables → ImmutableTable.of when FerriteCore absent. */
    public static final String PERF_STATE_EMPTY_TABLE = "perf.state_empty_neighbours";
    /** LDLib DummyWorld profiler supplier captures Level — rebind to source world's supplier. */
    public static final String LEAK_LDLIB_DUMMYWORLD = "leak.ldlib_dummyworld";
    /**
     * Model load/bake hot path: Property name interning, BooleanProperty equals skip,
     * Transformation hash cache, multipart Selector predicate cache, MultiVariant parent resolve.
     */
    public static final String PERF_MODEL_OPTS = "perf.model_optimizations";
    /** Cache next free BitSet id in ForgeRegistry.add instead of scanning every registration. */
    public static final String PERF_FORGE_REG_BITS = "perf.forge_registry_bits";
    /** Cache MinecraftProfileTexture.getHash during SkinManager.registerTexture. */
    public static final String PERF_PROFILE_TEXTURE = "perf.profile_texture_cache";
    /** Compact AttributeSupplier maps + intern identical AttributeInstance templates at launch. */
    public static final String PERF_ATTRIBUTE_SUPPLIER = "perf.attribute_supplier_compact";
    /** Deduplicate ModelPart.Cube instances from CubeDefinition.bake. */
    public static final String PERF_COMPACT_ENTITY_MODELS = "perf.compact_entity_models";
    /** Disk-cache stronghold ring positions + early radius reject + dedicated pool. */
    public static final String PERF_CACHE_STRONGHOLDS = "perf.cache_strongholds";
    /** Index zip resource packs for O(k) getNamespaces/listResources. */
    public static final String PERF_ZIP_PACK_INDEX = "perf.zip_pack_index";
    /** Dedicated ForkJoin pool for client/server resource reloads. */
    public static final String PERF_DEDICATED_RELOAD = "perf.dedicated_reload_executor";
    /** Fast-path Forge NamespacedWrapper.freeze when all holders are bound. */
    public static final String PERF_FAST_FORGE_DUMMIES = "perf.fast_forge_dummies";
    /** Cut worldgen hot-path allocations (surface rules, material list, NoiseChunk.wrap). */
    public static final String PERF_WORLDGEN_ALLOC = "perf.worldgen_allocation";
    /** Share LevelChunk section/skylight arrays into ImposterProtoChunk. */
    public static final String PERF_COMPACT_IMPOSTER = "perf.compact_imposter_chunks";
    /** Intern Ingredient.ItemValue templates + defensive copy on getItems. */
    public static final String PERF_INGREDIENT_DEDUP = "perf.ingredient_dedup";
    /** Cheaper LivingEntity cap path + AttachCapabilitiesEvent isCancelable constant. */
    public static final String PERF_FORGE_CAP_RETRIEVAL = "perf.forge_cap_retrieval";
    /** Suspend integrated-server full ticks until client join packets applied. */
    public static final String PERF_SUSPEND_INTEGRATED = "perf.suspend_integrated_server";
    /** GUI SimpleBakedModel only renders camera-facing quads. */
    public static final String PERF_FASTER_ITEM_RENDER = "perf.faster_item_rendering";
    /** Compact MappedRegistry lifecycles + memoize VanillaRegistries + intern BlockStateData tags. */
    public static final String PERF_COMPACT_MOJANG_REG = "perf.compact_mojang_registries";
    /** Disk-cache DFU-upgraded structure NBT. */
    public static final String PERF_CACHE_STRUCTURES = "perf.cache_upgraded_structures";
    /** Lazy Mojang DFU construction + cache blaster + skip register-time schema fetch. */
    public static final String PERF_DYNAMIC_DFU = "perf.dynamic_dfu";
    /** Drop permanent world-spawn region tickets; temporary START/PORTAL tickets instead. */
    public static final String PERF_REMOVE_SPAWN_CHUNKS = "perf.remove_spawn_chunks";
    /** Prefetch columns + biome set hoist + fast ChunkBiomeLookup in SurfaceSystem. */
    public static final String PERF_OPTIMIZE_SURFACE = "perf.optimize_surface_rules";
    /** Defer BlockState cache rebuilds until first property access after bake/rebuild. */
    public static final String PERF_REDUCE_BLOCKSTATE_CACHE = "perf.reduce_blockstate_cache_rebuilds";
    /** Directory-tree cache for Forge PathPackResources (mod path packs). */
    public static final String PERF_PATH_PACK_CACHE = "perf.path_pack_cache";
    /** Compact UnihexProvider line storage (bytes/shorts packed into longs). */
    public static final String PERF_COMPRESS_UNIHEX = "perf.compress_unihex_font";
    /** Soft-value structure template repository map. */
    public static final String PERF_DYNAMIC_STRUCTURE = "perf.dynamic_structure_manager";
    /** Faster chunk mesh rebuild iteration + BlockState reuse. */
    public static final String PERF_CHUNK_MESHING = "perf.chunk_meshing";
    /** DebugLevelSource reuses Forge blockstate IdMapper instead of collecting a new list. */
    public static final String PERF_DEBUG_LEVEL_STATES = "perf.debug_level_states";
    /** Soft-reload language values from pack resources under GC pressure. */
    public static final String PERF_DYNAMIC_LANGUAGES = "perf.dynamic_languages";
    /** Faster tag Ingredient.test/stacking + soft-cached getItems expansion. */
    public static final String PERF_FASTER_INGREDIENTS = "perf.faster_ingredients";
    /** Defer SearchRegistry tree build until first non-empty search. */
    public static final String PERF_LAZY_SEARCH = "perf.lazy_search_tree";
    /** OpenHashMap ForgeRegistry delegates + Block/Item hot-path DelegateHolder. */
    public static final String PERF_FORGE_REG_ALLOC = "perf.forge_registry_alloc";
    /** FakeStateMap for StateDefinition when FerriteCore present. */
    public static final String PERF_FAKE_STATE_MAP = "perf.fake_state_map";
    /** Hide live BEs from ImposterProtoChunk during worldgen. */
    public static final String PERF_IMPOSTER_BE_GUARD = "perf.imposter_be_guard";
    /** Suspend idle non-FULL protochunk holders after generation to reclaim memory. */
    public static final String PERF_RELEASE_PROTOCHUNKS = "perf.release_protochunks";
    /** Profiled resource reload naming/sorting + omnifix.debugReloader system property. */
    public static final String PERF_DEBUG_RELOADER = "perf.debug_reloader";
    /** Log bootstrap/game/world-join timings. */
    public static final String PERF_MEASURE_TIME = "perf.measure_time";
    /** Pre-resolve KeyMapping translations on main thread at search-tree build. */
    public static final String PERF_KEYMAP_PREWARM = "perf.keymap_prewarm";
    /** Integrated server hung-tick watchdog (40s) with thread dump. */
    public static final String PERF_INTEGRATED_WATCHDOG = "perf.integrated_watchdog";
    /** Release vanilla MemoryReserve early on client. */
    public static final String PERF_MEMORY_RESERVE = "perf.memory_reserve_release";
    /** Throttle NightConfig FileWatcher loop (1s park). */
    public static final String PERF_NIGHTCONFIG_WATCH = "perf.nightconfig_watch_throttle";
    /**
     * Defer NightConfig watch reloads to /ofc|/ofsrc and lock per-mod config handlers so
     * watcher-thread + Forge main-thread posts cannot race.
     */
    public static final String BUGFIX_NIGHTCONFIG_CRASH = "bugfix.nightconfig_config_crash";
    /** Compact ModFileScanData annotations/types after discovery. */
    public static final String PERF_MOD_SCAN_COMPACT = "perf.mod_scan_compact";
    /** Drop digest-only SecureJar manifest entries after load. */
    public static final String PERF_MANIFEST_COMPACT = "perf.manifest_compact";
    /** Park FML SyncExecutor idle poll + kick splash redraw. */
    public static final String PERF_MOD_WORK_QUEUE = "perf.mod_work_queue";
    /** Force NetworkConstants.init during bootstrap (Forge #9505 race). */
    public static final String PERF_NETWORK_CONSTANTS_INIT = "perf.network_constants_init";
    /** Skip per-table getResource in Forge loot deserialize via pre-scan marker. */
    public static final String PERF_FASTER_LOOT = "perf.faster_loot_loading";
    /** Clear Forge debug overlay chunk cache when leaving a client level. */
    public static final String BUGFIX_DEBUG_OVERLAY_CLEAR = "bugfix.debug_overlay_clear";
    /** STB rectangle pack for large texture atlases (≥100 sprites). */
    public static final String PERF_FASTER_TEXTURE_STITCH = "perf.faster_texture_stitching";
    /** Plug Mixin InjectorGroupInfo.NO_GROUP member list leak. */
    public static final String PERF_MIXIN_INJECTOR_GROUP = "perf.mixin_injector_group_patch";
    /**
     * Force mixin audit + clear ClassInfo cache after launch. Default off — can break late mixins.
     */
    public static final String PERF_CLEAR_MIXIN_CLASSINFO = "perf.clear_mixin_classinfo";
    /** Profile #minecraft:tick mcfunctions; dump with /omnifix mcfunctions. Default off. */
    public static final String FEATURE_MCFUNCTION_PROFILING = "feature.mcfunction_profiling";
    /** Client registry RegisterEvent progress bars + async splash redraw. */
    public static final String FEATURE_REGISTRY_PROGRESS = "feature.registry_event_progress";
    /** Patchouli book template AIR stacks → EMPTY after reload. */
    public static final String PERF_PATCHOULI_BOOKS = "perf.patchouli_book_dedup";

    // --- Parallel wave batch (2026-07-21 research) ---
    /** MC-2025 — persist entity AABB so fence collision survives chunk reload. */
    public static final String VANILLA_MOB_FENCE_ESCAPE = "vanilla.mob_fence_escape";
    /** MC-30391 — no land fall particles for chicken/blaze/wither. */
    public static final String VANILLA_SLOW_FALL_PARTICLES = "vanilla.slow_fall_particles";
    /** MC-224729 — save protochunks / always-accessible holders on saveAllChunks. */
    public static final String VANILLA_PARTIAL_CHUNK_SAVE = "vanilla.partial_chunk_save";
    /** MC-89146 — ordered BE map so piston move order survives reload. */
    public static final String VANILLA_PISTON_RELOAD = "vanilla.piston_reload";
    /** Resourceful Lib highlight geometry interning after apply. */
    public static final String PERF_RESOURCEFULLIB_HIGHLIGHTS = "perf.resourcefullib_highlights";
    /**
     * Hopper entity suck: avoid re-allocating/querying item entities every transfer tick when
     * the below AABB is empty (engine-class, independent of Lithium).
     */
    public static final String PERF_HOPPER_ENTITY_CACHE = "perf.hopper_entity_cache";
    /** OmniFix branding line in F3 / BrandingControl. */
    public static final String FEATURE_OMNIFIX_BRANDING = "feature.omnifix_branding";
    /** Mirror System.out/err into log4j (launch diagnostics). Default off. */
    public static final String FEATURE_STDOUT_LOG = "feature.log_stdout";

    // --- Parallel 8-agent wave (2026-07-21) ---
    /**
     * Client: skip expensive entity–entity collision resolution for non-local entities;
     * server already owns authority (CorgiTaco / Entity Collision FPS class).
     */
    public static final String PERF_CLIENT_ENTITY_COLLISION = "perf.client_entity_collision";
    /** Cache Direction.values() / Axis.values() on block neighbour-update hot paths. */
    public static final String PERF_DIRECTION_VALUES_CACHE = "perf.direction_values_cache";
    /** Same-tick empty cache for ItemEntity merge neighbour scans. */
    public static final String PERF_ITEM_ENTITY_MERGE_CACHE = "perf.item_entity_merge_cache";
    /** Compact advancement datapack apply error logs (location + message only). */
    public static final String PERF_ADVANCEMENT_RELOAD_LOG = "perf.advancement_reload_log";
    /** Dismiss world join loading screen once level + player ready (force-close class). */
    public static final String FEATURE_FORCE_CLOSE_LOADING = "feature.force_close_loading_screen";
    /** Throttle PathNavigation repath when mob has not moved (engine AI hot path). */
    public static final String PERF_PATH_RECALC_THROTTLE = "perf.path_recalc_throttle";
    /** Throttle / compact UUID duplicate entity log spam. */
    public static final String BUGFIX_UUID_LOG_SPAM = "bugfix.uuid_duplicate_log";
    /**
     * Skip LevelChunk random-tick iteration when the chunk has no random-ticking positions
     * (engine class; independent of Lithium).
     */
    public static final String PERF_SKIP_EMPTY_RANDOM_TICK = "perf.skip_empty_random_ticks";
    /** Same-tick empty cache for ExperienceOrb merge scan in scanForEntities. */
    public static final String PERF_XP_ORB_SCAN_CACHE = "perf.xp_orb_scan_cache";
    /**
     * Raise GoalSelector newGoalRate 3→5 so full goal reselection runs less often
     * (AI Improvements-class trade-off; running goals still tick every tick).
     */
    public static final String PERF_GOAL_SELECTOR_RATE = "perf.goal_selector_rate";
    /** Skip ParticleEngine.tick work when no particles / emitters / pending adds. */
    public static final String PERF_PARTICLE_EMPTY_TICK = "perf.particle_empty_tick";
    /** Fast-path Level.tickBlockEntities when no tickers/pending/fresh BEs. */
    public static final String PERF_EMPTY_BE_TICK = "perf.empty_block_entity_tick";
    /** Short-circuit BlockState.getDrops when block loot table is EMPTY. */
    public static final String PERF_EMPTY_BLOCK_DROPS = "perf.empty_block_drops_shortcircuit";
    /**
     * ClassInstanceMultiMap uses OpenHashMap/ObjectArrayList instead of Guava HashMap/ArrayList
     * for entity section by-class indexes.
     */
    public static final String PERF_ENTITY_SECTION_MAP = "perf.entity_section_multimap";
    /** Skip BrewingStand.serverTick when idle (no fuel load, not brewable, brewTime 0). */
    public static final String PERF_BREWING_STAND_IDLE = "perf.brewing_stand_idle";
    /**
     * Raise brain Sensor scan intervals by ~50% (capped) to cut AI perception CPU
     * (AI Improvements-class trade-off).
     */
    public static final String PERF_SENSOR_SCAN_RATE = "perf.sensor_scan_rate";
    /** Skip AbstractArrow full tick work every other tick while stuck in ground (server). */
    public static final String PERF_ARROW_INGROUND = "perf.arrow_inground_throttle";
    /** Skip AbstractFurnace.serverTick when cold and input/fuel empty. */
    public static final String PERF_FURNACE_IDLE = "perf.furnace_idle";
    /** Skip Campfire cookTick/cooldownTick when no items/progress. */
    public static final String PERF_CAMPFIRE_IDLE = "perf.campfire_idle";
    /**
     * Raise NearestAttackableTargetGoal randomInterval ~+50% so findTarget runs less often
     * (AI Improvements-class trade-off).
     */
    public static final String PERF_TARGET_GOAL_INTERVAL = "perf.target_goal_interval";
    /** Exact-bit memo cache for Direction.getNearest(DDD) hot re-queries. */
    public static final String PERF_DIRECTION_GET_NEAREST = "perf.direction_get_nearest_cache";
    /** Skip BeehiveBlockEntity.serverTick when no bees are stored. */
    public static final String PERF_BEEHIVE_EMPTY = "perf.beehive_empty";
    /** Skip Jukebox playRecordTick when not playing and no disc. */
    public static final String PERF_JUKEBOX_IDLE = "perf.jukebox_idle";
    /** Skip LivingEntity.tickEffects body when no effects and clean ambient state. */
    public static final String PERF_EMPTY_EFFECTS_TICK = "perf.empty_effects_tick";
    /** Use Reference2ObjectOpenHashMap for LivingEntity.activeEffects. */
    public static final String PERF_EFFECTS_MAP = "perf.effects_open_hash_map";
    /** Skip BellBlockEntity ticks when not shaking and not resonating. */
    public static final String PERF_BELL_IDLE = "perf.bell_idle";
    /** Skip ShulkerBox animation tick when CLOSED or fully OPENED. */
    public static final String PERF_SHULKER_BOX_IDLE = "perf.shulker_box_idle";
    /** Skip chest lidAnimateTick when lid fully closed and not opening. */
    public static final String PERF_CHEST_LID_IDLE = "perf.chest_lid_idle";
    /** Skip SignBlockEntity.tick when no editor UUID is set. */
    public static final String PERF_SIGN_EDIT_IDLE = "perf.sign_edit_idle";
    /** Throttle enchanting-table book animation when fully closed (client). */
    public static final String PERF_ENCHANT_TABLE_IDLE = "perf.enchantment_table_idle";
    /** Evaluate AvoidEntityGoal.canUse only every 3rd mob tick (entity scan cut). */
    public static final String PERF_AVOID_ENTITY_THROTTLE = "perf.avoid_entity_scan_throttle";
    /** Raise RandomStrollGoal interval +50% (cap 240) to cut path starts. */
    public static final String PERF_RANDOM_STROLL_INTERVAL = "perf.random_stroll_interval";
    /** Cache BaseSpawner.isNearPlayer result within the same game tick. */
    public static final String PERF_SPAWNER_NEAR_CACHE = "perf.spawner_near_cache";
    /** Skip SculkCatalyst.serverTick when spreader has no charge cursors. */
    public static final String PERF_SCULK_CATALYST_IDLE = "perf.sculk_catalyst_idle";
    /** FollowParentGoal.canUse entity scan every 3rd tick for baby animals. */
    public static final String PERF_FOLLOW_PARENT_THROTTLE = "perf.follow_parent_throttle";
    /** TemptGoal.canUse nearest-player query every 2nd tick when not calming. */
    public static final String PERF_TEMPT_GOAL_THROTTLE = "perf.tempt_goal_throttle";
    /** BreedGoal partner scan every 2nd tick while in love. */
    public static final String PERF_BREED_GOAL_THROTTLE = "perf.breed_goal_throttle";
    /** Same-tick empty cache for MinecartHopper.suckInItems entity path. */
    public static final String PERF_MINECART_HOPPER_CACHE = "perf.minecart_hopper_empty_cache";
    /** BegGoal.canUse player scan every 3rd tick (wolves). */
    public static final String PERF_BEG_GOAL_THROTTLE = "perf.beg_goal_throttle";
    /** Inactive conduits only run full serverTick on 40-tick shape refresh boundary. */
    public static final String PERF_CONDUIT_INACTIVE = "perf.conduit_inactive_throttle";
    /** Hanging entities (item frames/paintings) survive-check every 150 ticks instead of 100. */
    public static final String PERF_HANGING_SURVIVE = "perf.hanging_entity_survive_interval";
    /** FollowOwnerGoal path repath delay 10→15 (adjusted) ticks. */
    public static final String PERF_FOLLOW_OWNER_REPATH = "perf.follow_owner_repath";
    /** ContainerOpenersCounter recheck schedule 5→8 ticks while chest open. */
    public static final String PERF_OPENERS_RECHECK = "perf.container_openers_recheck";
    /** MoveThroughVillageGoal canUse POI scan every 3rd mob tick. */
    public static final String PERF_MOVE_VILLAGE_THROTTLE = "perf.move_through_village_throttle";
    /** FleeSunGoal.canUse every 3rd mob tick (day+fire sky checks). */
    public static final String PERF_FLEE_SUN_THROTTLE = "perf.flee_sun_throttle";
    /** RestrictSunGoal.canUse every 3rd mob tick. */
    public static final String PERF_RESTRICT_SUN_THROTTLE = "perf.restrict_sun_throttle";
    /** MoveToBlockGoal nextStartTick base 200→300 (search less often). */
    public static final String PERF_MOVE_TO_BLOCK_INTERVAL = "perf.move_to_block_interval";
    /** StrollThroughVillageGoal interval +50% (cap 240). */
    public static final String PERF_STROLL_VILLAGE_INTERVAL = "perf.stroll_village_interval";
    /** ExperienceOrb player/merge scan every 30 ticks instead of 20. */
    public static final String PERF_XP_ORB_SCAN_PERIOD = "perf.xp_orb_scan_period";
    /** RemoveBlockGoal.canUse every 3rd tick (break-block AI). */
    public static final String PERF_REMOVE_BLOCK_THROTTLE = "perf.remove_block_throttle";
    /** LeapAtTargetGoal canUse random gate less frequent (base chance half). */
    public static final String PERF_LEAP_TARGET_THROTTLE = "perf.leap_at_target_throttle";
    /** End gateway teleportTick entity AABB scan every other tick when idle (not cooling). */
    public static final String PERF_END_GATEWAY_SCAN = "perf.end_gateway_entity_scan_throttle";
    /** FollowMobGoal.canUse entity list scan every 3rd mob tick. */
    public static final String PERF_FOLLOW_MOB_THROTTLE = "perf.follow_mob_throttle";
    /** FollowMobGoal path repath delay 10→15 while following. */
    public static final String PERF_FOLLOW_MOB_REPATH = "perf.follow_mob_repath";
    /** DefendVillageTargetGoal villager/player scans every 3rd golem tick. */
    public static final String PERF_DEFEND_VILLAGE_THROTTLE = "perf.defend_village_throttle";
    /** OfferFlowerGoal villager scan every 3rd golem tick. */
    public static final String PERF_OFFER_FLOWER_THROTTLE = "perf.offer_flower_throttle";
    /** RunAroundLikeCrazyGoal canUse every 3rd tick (untamed horse panic run). */
    public static final String PERF_RUN_CRAZY_THROTTLE = "perf.run_around_crazy_throttle";
    /** LookAtPlayerGoal probability ×0.67 (fewer idle look scans). */
    public static final String PERF_LOOK_AT_PROBABILITY = "perf.look_at_player_probability";
    /** FollowBoatGoal.canUse boat/player scans every 3rd tick. */
    public static final String PERF_FOLLOW_BOAT_THROTTLE = "perf.follow_boat_throttle";
    /** LandOnOwnersShoulderGoal.canUse every 3rd tick (parrots). */
    public static final String PERF_LAND_SHOULDER_THROTTLE = "perf.land_shoulder_throttle";
    /** ResetUniversalAngerTargetGoal.canUse every 3rd tick. */
    public static final String PERF_RESET_ANGER_THROTTLE = "perf.reset_anger_throttle";
    /** CatSitOnBlockGoal.canUse every 3rd tick. */
    public static final String PERF_CAT_SIT_THROTTLE = "perf.cat_sit_throttle";
    /** CatLieOnBedGoal.canUse every 3rd tick. */
    public static final String PERF_CAT_LIE_THROTTLE = "perf.cat_lie_throttle";
    /** PanicGoal.canUse every 2nd tick unless mob is on fire (water-search path kept hot). */
    public static final String PERF_PANIC_THROTTLE = "perf.panic_goal_throttle";
    /** TradeWithPlayerGoal.canUse every 3rd tick. */
    public static final String PERF_TRADE_PLAYER_THROTTLE = "perf.trade_with_player_throttle";
    /** DolphinJumpGoal constructor interval +50% (cap 240). */
    public static final String PERF_DOLPHIN_JUMP_INTERVAL = "perf.dolphin_jump_interval";
    /** EatBlockGoal.canUse every 3rd tick (sheep/goats). */
    public static final String PERF_EAT_BLOCK_THROTTLE = "perf.eat_block_throttle";
    /** ClimbOnTopOfPowderSnowGoal.canUse every 3rd tick. */
    public static final String PERF_CLIMB_POWDER_THROTTLE = "perf.climb_powder_snow_throttle";
    /** MoveTowardsRestrictionGoal random-pos canUse every 3rd tick. */
    public static final String PERF_MOVE_RESTRICTION_THROTTLE = "perf.move_restriction_throttle";
    /** MoveTowardsTargetGoal.canUse every 2nd tick. */
    public static final String PERF_MOVE_TARGET_THROTTLE = "perf.move_towards_target_throttle";
    /** RangedAttackGoal attack interval min/max +50% (cap 80). */
    public static final String PERF_RANGED_ATTACK_INTERVAL = "perf.ranged_attack_interval";
    /** RangedBowAttackGoal attackIntervalMin +50% (cap 60). */
    public static final String PERF_RANGED_BOW_INTERVAL = "perf.ranged_bow_interval";
    /** PathfindToRaidGoal.canUse every 3rd tick. */
    public static final String PERF_PATHFIND_RAID_THROTTLE = "perf.pathfind_to_raid_throttle";
    /** BreakDoorGoal.canUse every 3rd tick. */
    public static final String PERF_BREAK_DOOR_THROTTLE = "perf.break_door_throttle";
    /** FloatGoal.canUse: dry path every 3rd tick; always evaluate when wet/lava. */
    public static final String PERF_FLOAT_GOAL_THROTTLE = "perf.float_goal_throttle";
    /** MeleeAttackGoal canUse cooldown 20→30 game ticks (path create cadence). */
    public static final String PERF_MELEE_CANUSE_COOLDOWN = "perf.melee_canuse_cooldown";
    /** RangedCrossbowAttackGoal post-charge attackDelay base/range +50%. */
    public static final String PERF_RANGED_CROSSBOW_DELAY = "perf.ranged_crossbow_delay";
    /** DoorInteractGoal.canUse (open-door / super path) every 3rd tick. */
    public static final String PERF_DOOR_INTERACT_THROTTLE = "perf.door_interact_throttle";
    /** RandomLookAroundGoal look probability ×2/3. */
    public static final String PERF_RANDOM_LOOK_PROBABILITY = "perf.random_look_probability";
    /** LlamaFollowCaravanGoal entity scan canUse every 3rd tick. */
    public static final String PERF_LLAMA_CARAVAN_THROTTLE = "perf.llama_caravan_throttle";
    /** FollowFlockLeaderGoal repath interval 10→15. */
    public static final String PERF_FOLLOW_FLOCK_REPATH = "perf.follow_flock_repath";
    /** AreaEffectCloud entity-effect scan period 5→8 ticks. */
    public static final String PERF_AEC_SCAN_PERIOD = "perf.aec_scan_period";
    /** TryFindWaterGoal.canUse every 3rd tick (dry-ground water seek). */
    public static final String PERF_TRY_FIND_WATER_THROTTLE = "perf.try_find_water_throttle";
    /** OcelotAttackGoal navigation repath every 2nd tick. */
    public static final String PERF_OCELOT_ATTACK_REPATH = "perf.ocelot_attack_repath";
    /** MoveBackToVillageGoal.canUse every 3rd tick. */
    public static final String PERF_MOVE_BACK_VILLAGE_THROTTLE = "perf.move_back_village_throttle";
    /** RandomStandGoal.canUse every 3rd tick (horse stand anim). */
    public static final String PERF_RANDOM_STAND_THROTTLE = "perf.random_stand_throttle";
    /** UseItemGoal.canUse every 3rd tick. */
    public static final String PERF_USE_ITEM_THROTTLE = "perf.use_item_throttle";
    /** SwellGoal.canUse every 2nd tick unless already swelling. */
    public static final String PERF_SWELL_GOAL_THROTTLE = "perf.swell_goal_throttle";
    /** ItemEntity stationary merge scan 40→60 ticks. */
    public static final String PERF_ITEM_MERGE_PERIOD = "perf.item_merge_period";
    /** BreathAirGoal air-position search every 2nd tick when air not critical. */
    public static final String PERF_BREATH_AIR_PATH = "perf.breath_air_path_throttle";
    /** SitWhenOrderedToGoal.canUse every 3rd tick when not ordered to sit. */
    public static final String PERF_SIT_ORDERED_THROTTLE = "perf.sit_when_ordered_throttle";
    /** NearestItemSensor wanted-item scan range 32→24. */
    public static final String PERF_NEAREST_ITEM_RANGE = "perf.nearest_item_sensor_range";
    /** SecondaryPoiSensor profession block scan radius 4→3. */
    public static final String PERF_SECONDARY_POI_RADIUS = "perf.secondary_poi_radius";
    /** ArmorStand marker: skip pushEntities entity scan. */
    public static final String PERF_ARMOR_STAND_MARKER_PUSH = "perf.armor_stand_marker_push";
    /** Shulker findNewAttachment every 3rd tick. */
    public static final String PERF_SHULKER_ATTACH_THROTTLE = "perf.shulker_attach_throttle";
    /** MeleeAttackGoal path-recalc base delay 4→6. */
    public static final String PERF_MELEE_PATH_RECALC = "perf.melee_path_recalc_base";
    /** ItemEntity still-on-ground physics interval 4→6. */
    public static final String PERF_ITEM_STILL_PHYSICS = "perf.item_still_physics_period";
    /** LivingEntity.pushEntities every 2nd tick (non-players). */
    public static final String PERF_LIVING_PUSH_THROTTLE = "perf.living_push_throttle";
    /** NearestLivingEntitySensor radius XZ/Y 16→12. */
    public static final String PERF_NEAREST_LIVING_RADIUS = "perf.nearest_living_sensor_radius";
    /** PlayerSensor closerThan range 16→12. */
    public static final String PERF_PLAYER_SENSOR_RANGE = "perf.player_sensor_range";
    /** NearestBedSensor POI range 48→36 and batch 5→3. */
    public static final String PERF_NEAREST_BED_SCAN = "perf.nearest_bed_scan";
    /** HurtByTargetGoal alertOthers Y inflate 10→7. */
    public static final String PERF_HURT_ALERT_Y = "perf.hurt_alert_y";
    /** NearestAttackableTargetGoal search AABB Y inflate 4→3. */
    public static final String PERF_TARGET_SEARCH_Y = "perf.target_search_y";
    /** WaterAvoidingRandomFlyingGoal hover radius 8→6. */
    public static final String PERF_FLYING_HOVER_RADIUS = "perf.flying_hover_radius";
    /** AbstractMinecart low-speed push entity scan every 2nd tick. */
    public static final String PERF_MINECART_PUSH_THROTTLE = "perf.minecart_push_throttle";
    /** BeePollinateGoal flower-retry cooldown min/max +50%. */
    public static final String PERF_BEE_POLLINATE_COOLDOWN = "perf.bee_pollinate_cooldown";
    /** TemptingSensor player range 10→8. */
    public static final String PERF_TEMPTING_SENSOR_RANGE = "perf.tempting_sensor_range";
    /** BeeLocateHiveGoal POI range 20→15 and locate cooldown 200→300. */
    public static final String PERF_BEE_HIVE_LOCATE = "perf.bee_hive_locate";
    /** Phantom player target scan interval +50% and XZ AABB 16→12. */
    public static final String PERF_PHANTOM_SCAN = "perf.phantom_player_scan";
    /** OwnerHurtByTargetGoal.canUse every 2nd tick. */
    public static final String PERF_OWNER_HURT_BY_THROTTLE = "perf.owner_hurt_by_throttle";
    /** OwnerHurtTargetGoal.canUse every 2nd tick. */
    public static final String PERF_OWNER_HURT_TARGET_THROTTLE = "perf.owner_hurt_target_throttle";
    /** WaterAvoidingRandomStrollGoal LandRandomPos radii reduced. */
    public static final String PERF_WATER_AVOID_STROLL_RADIUS = "perf.water_avoid_stroll_radius";
    /** NearestHealableRaiderTargetGoal post-start cooldown 200→300. */
    public static final String PERF_HEALABLE_RAIDER_COOLDOWN = "perf.healable_raider_cooldown";
    /** EndermanTakeBlockGoal take attempt interval 20→30. */
    public static final String PERF_ENDERMAN_TAKE_INTERVAL = "perf.enderman_take_interval";
    /** WardenEntitySensor living-entity radius 24→20. */
    public static final String PERF_WARDEN_SENSOR_RADIUS = "perf.warden_sensor_radius";
    /** AxolotlAttackablesSensor isClose distSq 64→36 (8→6 blocks). */
    public static final String PERF_AXOLOTL_ATTACK_RANGE = "perf.axolotl_attack_range";
    /** FrogAttackablesSensor closerThan 10→8. */
    public static final String PERF_FROG_ATTACK_RANGE = "perf.frog_attack_range";
    /** EndermanLeaveBlockGoal place attempt interval 2000→3000. */
    public static final String PERF_ENDERMAN_LEAVE_INTERVAL = "perf.enderman_leave_interval";
    /** Ghast RandomFloatAroundGoal wander offset 16→12. */
    public static final String PERF_GHAST_WANDER_RADIUS = "perf.ghast_wander_radius";
    /** GhastShootFireballGoal charge-to-fire ticks 20→30. */
    public static final String PERF_GHAST_FIREBALL_CHARGE = "perf.ghast_fireball_charge";
    /** SilverfishWakeUpFriendsGoal infest scan box smaller. */
    public static final String PERF_SILVERFISH_WAKE_SCAN = "perf.silverfish_wake_scan";
    /** BeeWanderGoal canUse chance nextInt(10)→nextInt(15). */
    public static final String PERF_BEE_WANDER_CHANCE = "perf.bee_wander_chance";
    /** BlazeAttackGoal charge/volley timers +50%. */
    public static final String PERF_BLAZE_FIRE_INTERVAL = "perf.blaze_fire_interval";
    /** Guardian laser attack duration 80→100. */
    public static final String PERF_GUARDIAN_ATTACK_DURATION = "perf.guardian_attack_duration";
    /** VexRandomMoveGoal nextInt chance 7→10. */
    public static final String PERF_VEX_RANDOM_MOVE = "perf.vex_random_move_chance";
    /** VexChargeAttackGoal nextInt chance 7→10. */
    public static final String PERF_VEX_CHARGE_CHANCE = "perf.vex_charge_chance";
    /** HoglinSpecificSensor repellent findClosestMatch 8,4 → 6,3. */
    public static final String PERF_HOGLIN_REPELLENT_RANGE = "perf.hoglin_repellent_range";
    /** PiglinSpecificSensor repellent findClosestMatch 8,4 → 6,3. */
    public static final String PERF_PIGLIN_REPELLENT_RANGE = "perf.piglin_repellent_range";
    /** Allay passive heal period 10→15 ticks. */
    public static final String PERF_ALLAY_HEAL_PERIOD = "perf.allay_heal_period";
    /** Rabbit RaidGardenGoal block search range 16→12. */
    public static final String PERF_RABBIT_RAID_RANGE = "perf.rabbit_raid_range";
    /** Slime getJumpDelay nextInt(20)+10 → nextInt(30)+15. */
    public static final String PERF_SLIME_JUMP_DELAY = "perf.slime_jump_delay";
    /** ElderGuardian getAttackDuration 60→80. */
    public static final String PERF_ELDER_GUARDIAN_ATTACK = "perf.elder_guardian_attack_duration";
    /** Evoker fang spell casting interval 100→150. */
    public static final String PERF_EVOKER_FANG_INTERVAL = "perf.evoker_fang_interval";
    /** Evoker summon-vex casting interval 340→450. */
    public static final String PERF_EVOKER_SUMMON_INTERVAL = "perf.evoker_summon_interval";
    /** Evoker wololo casting interval 140→200. */
    public static final String PERF_EVOKER_WOLOLO_INTERVAL = "perf.evoker_wololo_interval";
    /** TurtleGoHomeGoal random gate 700→1000. */
    public static final String PERF_TURTLE_GO_HOME_CHANCE = "perf.turtle_go_home_chance";
    /** TurtleLayEggGoal lay duration 200→300. */
    public static final String PERF_TURTLE_LAY_EGG_DURATION = "perf.turtle_lay_egg_duration";
    /** BeeGrowCropGoal crop-tick interval 30→45. */
    public static final String PERF_BEE_GROW_CROP_INTERVAL = "perf.bee_grow_crop_interval";
    /** PandaRollGoal playful/normal roll gates +50%. */
    public static final String PERF_PANDA_ROLL_CHANCE = "perf.panda_roll_chance";
    /** PandaSneezeGoal weak/normal sneeze gates +50%. */
    public static final String PERF_PANDA_SNEEZE_CHANCE = "perf.panda_sneeze_chance";
    /** PolarBearAttackPlayersGoal baby-scan inflate 8,4→6,3. */
    public static final String PERF_POLAR_BEAR_CUB_SCAN = "perf.polar_bear_cub_scan";
    /** DrownedGoToWaterGoal water sample count/radius reduced. */
    public static final String PERF_DROWNED_WATER_SEARCH = "perf.drowned_water_search";
    /** BeeGoToHiveGoal travel timeout 600→800. */
    public static final String PERF_BEE_GO_HIVE_TIMEOUT = "perf.bee_go_hive_timeout";
    /** BeeGoToKnownFlowerGoal travel timeout 600→800. */
    public static final String PERF_BEE_GO_FLOWER_TIMEOUT = "perf.bee_go_flower_timeout";
    /** FishingHook open-water area scan ±2→±1. */
    public static final String PERF_FISHING_OPEN_WATER_SCAN = "perf.fishing_open_water_scan";
    /** PandaSitGoal item-entity scan inflate 6/8→4/6. */
    public static final String PERF_PANDA_SIT_ITEM_SCAN = "perf.panda_sit_item_scan";

    private static boolean registered;

    private FeatureUnits() {}

    public static synchronized void registerBuiltins() {
        if (registered) {
            return;
        }
        registered = true;

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_FRUSTUM,
                "VS×IP frustum dead-loop cancel",
                "Cancels VS's redundant Frustum dead-loop mixin when Immersive Portals is present (MixinSquared).",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_PORTAL_CAMERA,
                "VS×IP portal camera bridge",
                "Bypasses VS ship-camera wrap during IP nested portal renders and applies self-calibrating ship bank.",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_PORTAL_FOG,
                "VS×IP portal fog render distance",
                "Decouples Embeddium effective render distance from host fog during IP nested passes.",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS, StackDomain.EMBEDDUM));

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_SHIP_UNLOAD_CCE,
                "VS×IP ship unload CCE guard",
                "Skips VS cast of IP ImmPtlClientChunkMap to ClientChunkCacheDuck on ship unload.",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_SHIP_VIS,
                "VS×IP remote ship visibility",
                "Server-side portal-visible remote ship tracking, force-watch, and vanilla chunk-packet guards.",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_SHIP_TRANSIT,
                "VS×IP ship portal transit",
                "Teleports ships through IP portals with momentum rotation and disarm/re-arm anti-bounce.",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_ENTITY_DRAG,
                "VS×IP portal not ship-draggable",
                "Excludes IP Portal entities from VS EntityDragger so ships do not break portal panes.",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_DIAGNOSTICS,
                "VS×IP ship data probe",
                "Throttled diagnostic logging of client ship metadata by dimension (-Domnifix.diagnostics or this toggle).",
                false,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS, StackDomain.EMBEDDUM));

        FeatureUnitRegistry.register(new FeatureUnit(
                CREATE_IP_TRACKS_A,
                "Create×IP nether track pairing",
                "Routes Create PortalTrackProvider for nether portals through overlapping IP Portal entities.",
                true,
                StackDomain.CREATE, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                CREATE_IP_TRACKS_B,
                "Create×IP block-less portal track pairing",
                "Pairs Create tracks across IP entity portals (wand/command/datapack) with no portal block; keeps portal tracks alive without a pane block.",
                true,
                StackDomain.CREATE, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                CREATE_IP_TRAIN_TRANSIT,
                "Create×IP train entity transit",
                "Stops Immersive Portals from teleporting Create train carriages and seated passengers (Create owns dimensional carriages) and routes portal dismount teleports through IP's ServerTeleportationManager.",
                true,
                StackDomain.CREATE, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                VP_CROSS_PORTAL_INTERACT,
                "VS×IP cross-portal interact (VS #1525)",
                "Feeds IP's remote cross-portal hit into VS originalCrosshairTarget so place/use through portals is not replaced by the local pre-portal raycast.",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.IMMERSIVE_PORTALS));

        FeatureUnitRegistry.register(new FeatureUnit(
                VS_CLOCKWORK_CLIP,
                "Create RaycastHelper ship clip",
                "Routes Create RaycastHelper.rayTraceRange through VS clipIncludeShips so Clockwork wanderwand and other Create ray tools hit ship blocks.",
                true,
                StackDomain.VALKYRIEN_SKIES, StackDomain.CREATE));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SPECTATOR_STUCK,
                "Spectator stuck effects",
                "Clears using-item, freeze, nausea, soul speed, elytra boost, fishing hook, and spectator overlays (MC-215531, MC-217716, MC-215530, MC-193343, MC-206705, MC-119754, MC-69216).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SPECTATOR_BED,
                "Spectator bed occupancy",
                "Prevents spectators from sleeping/occupying beds and wakes them on SPECTATOR switch (MC-119417).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_MENDING_BREAK,
                "Mending break progress",
                "Keeps block-break progress when mending rewrites Damage NBT mid-mine (MC-176559; complements Forge shouldCauseBlockBreakReset).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SAPLING_2X2,
                "2×2 sapling growth free-space",
                "Stops north/west adjacent blocks from falsely blocking mega spruce/jungle/dark-oak growth (MC-8187).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_FULLSCREEN,
                "Fullscreen F11 persist",
                "Saves options.txt when toggling fullscreen with F11 (MC-263865).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SPECTATOR_BREAK,
                "Spectator destroy progress clear",
                "Stops client block-break animation/sound when switching to spectator mid-mine (MC-46766).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SPECTATOR_PROJECTILE,
                "Spectator bow/trident release cancel",
                "Cancels bow/trident release (and other use-item finish) while spectator so projectiles do not fire (MC-81773).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SP_CHAT_SPAM,
                "Singleplayer chat spam kick",
                "Skips chat rate-limit disconnect on integrated / singleplayer servers (MC-14923).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_RAW_COPPER_SOUND,
                "Raw copper block copper sounds",
                "Uses SoundType.COPPER for RAW_COPPER_BLOCK (still defaults to stone in 1.20.1) (MC-223153).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_CREEPER_DEFUSE,
                "Creeper defuse on creative/spectator target",
                "Defuses swelling creepers when their target becomes creative or spectator (MC-179072).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_TITLE_CLEAR,
                "Clear title on world leave",
                "Clears title/subtitle overlay when disconnecting or changing worlds (MC-55347).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_HOTBAR_RESPAWN,
                "Hotbar slot on respawn",
                "Preserves the selected hotbar slot when the client recreates LocalPlayer on respawn (MC-143474).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_USE_SLOW_AFTER_DROP,
                "Use slowdown after drop",
                "Releases the active use-item after dropping the held stack so movement slowdown does not stick (MC-231097; complements Forge stopUsingItem).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_BREAK_DELAY_DROP_TOOL,
                "Break delay after drop tool",
                "Stops client block-destroy when dropping the selected stack so the next mine is not delayed (MC-165381).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_CROSSBOW_OFFHAND,
                "Crossbow offhand arm pose",
                "Does not apply charged CROSSBOW_HOLD arm pose to the offhand in first person so main-hand render stays intact (MC-227169).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_DRAG_STACK_INVISIBLE,
                "Drag-stack slot visibility",
                "Keeps inventory slots visible when a quick-craft drag set still has only one slot (MC-80859).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_MOUSE_INVENTORY,
                "Mouse inventory keybinds",
                "Honours remapped mouse bindings for inventory close and drop inside container screens (MC-577).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_LEATHER_SKELETON_STRAY,
                "Leather blocks skeleton stray conversion",
                "Skeletons wearing leather armor no longer convert to strays in powder snow (MC-214147).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_ZOMBVILLAGER_JOCKEY,
                "Cured baby zombie villager jockey",
                "Dismounts cured zombie villagers so baby jockeys do not stay riding (MC-200418).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_CREATIVE_LADDER_SLOW,
                "Creative flight ladder slowdown",
                "Flying players are not treated as climbing ladders/vines/scaffolding (MC-12829).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_DOUBLE_SNEAK_ANIM,
                "Double sneak animation",
                "Strips remote DATA_POSE updates for the local player so quick sneak does not bob twice (MC-159163).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_F3_DOUBLE,
                "F3 after F3+F4",
                "F3+F4 gamemode switcher does not consume the debug-key flag so F3 toggles once (MC-183776).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SPECTATOR_CONSUME,
                "Spectator still consumes",
                "Stops active item use on SPECTATOR switch and cancels completeUsingItem while spectator (MC-129909).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_LIGHTNING_DROPS,
                "Lightning kill drops vanish",
                "Fresh ItemEntity drops (tickCount <= 8) are immune to thunderHit so lightning kills keep loot (MC-206922).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_ARMORSTAND_PARTICLES,
                "Armor stand death particles",
                "Shows breaking plank particles when armor stands die to explosion or fire (MC-132878).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_FISHING_KILL_COUNT,
                "Fishing rod kill credit",
                "Records combat/hurt-by on FishingHook.pullEntity so rod pulls credit player kills (MC-100991).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_PUFFERFISH_DYING,
                "Dying pufferfish sting",
                "Pufferfish playerTouch sting/poison only applies while the fish is still alive (MC-155509).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SHIELD_HURT_SOUND,
                "Shield block hurt sound",
                "Skips LivingEntity hurt sound when a hit is fully blocked by a shield (MC-105068).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_BOAT_SLIME_HOVER,
                "Boat slime hover",
                "Always runs client boat float/move physics so boats do not hover on slime when not locally controlled (MC-108948).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_OFFHAND_ROD_PUNCH,
                "Offhand cast rod punch line",
                "Zeros fishing-line attack-anim offset when the cast rod is in the off-hand (MC-116379).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_RIPTIDE_OFFHAND,
                "Riptide offhand pose",
                "Applies first-person riptide spin pose only to the hand holding a trident (MC-127970).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_WOLF_HEARTS,
                "Wild wolf hearts",
                "Untamed wolves cannot fall in love so meat no longer shows hearts without breeding (MC-93018).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_STRIDER_SADDLE_PEACEFUL,
                "Strider saddle on Peaceful",
                "Skips zombified-piglin saddle jockey rolls for striders on Peaceful (MC-232869).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_PEACEFUL_SATURATION,
                "Peaceful saturation drain",
                "causeFoodExhaustion no-ops on Peaceful so saturation is not drained (MC-31819).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_CMD_MINECART_NBT,
                "Command minecart cooldown NBT",
                "Saves/loads MinecartCommandBlock lastActivated as LastExecuted (MC-121903).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_UNKNOWN_PASSENGER,
                "Unknown passenger log spam",
                "Suppresses ClientPacketListener warn when passenger packets reference an unloaded vehicle (MC-90683).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_CTRL_Q_CRAFT,
                "Ctrl+Q crafting result",
                "Ctrl+Q on ResultSlot repeatedly crafts-and-throws until ingredients run out (MC-135971).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_XP_BAR_VANISH,
                "XP bar at extreme levels",
                "Clamps getXpNeededForNextLevel used by the XP bar so int overflow cannot hide the bar (MC-79545).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_HIGH_SPEED_FLICKER,
                "High-speed elytra player flicker",
                "Clamps the elytra bank acos argument to 1 so NaN rotations do not flicker the player (MC-111516).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_ARMORSTAND_DARK,
                "Armor stand dark in solid block",
                "Samples light at neighboring Y offsets so armor stands are not black when their head is in a solid block (MC-197260).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_DRAGON_VOID_PORTAL,
                "Dragon void when exit portal destroyed",
                "Uses Y=65 when the End podium heightmap is 0 so the dragon does not dive into the void (MC-88371).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_DIM_TELEPORT_STATE,
                "Dimension teleport client state",
                "Re-sends effects, health/food, and abilities after cross-dimension teleportTo (MC-124177).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_GROUP_AI_DEATH,
                "Group AI dead target clear",
                "Clears a mob's target when the target is dead so pack AI does not stall (MC-183990).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_ENTITY_ANIM_FREEZE,
                "Long-lived entity animation freeze",
                "Reduces Mth.sin/cos arguments modulo TWO_PI so float precision does not freeze wing animations (MC-199467).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_FISHING_LINE_CROUCH,
                "Fishing line crouch third-person",
                "Lowers third-person crouch fishing-line attachment so the line meets the rod (MC-4490).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_POTTABLE_STAT,
                "Pottable plant used stat",
                "Awards ITEM_USED when potting a plant so minecraft.used increments (MC-231743).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SKELETON_LOOK,
                "Skeleton look while strafing",
                "Keeps LookControl aimed at the target while RangedBowAttackGoal strafes (MC-121706).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_ENDROD_CACTUS,
                "Cactus survive next to moving end rod",
                "Uses the moving-piston TE moved state for cactus neighbor solidity checks (MC-160095).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_DROWN_BUBBLES,
                "Drown bubbles at eye height",
                "Offsets drown bubble particles by eye height so they spawn at the head (MC-93384).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_TELEMETRY_DISABLE,
                "Disable client telemetry",
                "Forces Minecraft.allowsTelemetry false and ClientTelemetryManager sender DISABLED (MC-237493 / modded privacy).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_MAC_CTRL_Q,
                "Mac Ctrl+Q drop-all",
                "On OSX, player.drop entire-stack uses physical Ctrl instead of Super/Cmd so Cmd+Q is not required (MC-22882).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_LINUX_CHAT_T,
                "Linux chat open char suppress",
                "Cancels the first charTyped after opening chat so Linux DE split key/char polls do not type t or / into ChatScreen (MC-122477).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_MAC_SPRINT_BREAK,
                "Mac sprint+break left click",
                "While no screen is open, strips GLFW control modifier from mouse events so OSX Ctrl+left is not remapped to right-click (MC-59810).",
                true));

        // --- Wave 1 Band A ModernFix bugfix (vanilla/Forge; empty domain = always eligible) ---
        FeatureUnitRegistry.register(new FeatureUnit(
                MF_BUF_LEAK,
                "BufferBuilder render-type put leak",
                "Cancels RenderBuffers.put when the RenderType key already exists so prior BufferBuilders are not leaked.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_CHUNK_DEADLOCK,
                "Chunk load currentlyLoading + mount deadlock",
                "Short-circuits ServerChunkCache.getChunk via ChunkHolder.currentlyLoading and skips ENTITY_MOUNT when the chunk is unloaded.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_CONC_REG,
                "Registry/tag/reload concurrency",
                "Correct double-checked locking for MappedRegistry/NamespacedWrapper/ForgeRegistryTagManager and defers off-thread client reload listeners.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_DRAGON_LEAK,
                "Ender dragon model entity leak",
                "Nulls EnderDragonRenderer.DragonModel.entity after render so prior ClientLevel cannot leak.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_POSE_STACK,
                "Living/player pose stack balance",
                "Pops PoseStack poses if a mod cancels RenderLiving/RenderPlayer after pushing (Forge #9118).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_EXP_SCREEN,
                "Experimental world create flag",
                "Marks experimental PrimaryLevelData as confirmed on create so the dialog is not re-shown incorrectly.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_VEHICLE_PKT,
                "Forge vehicle move packet spam",
                "Uses positionRider instead of absMoveTo for vehicle passengers to avoid chunk-packet floods.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_MISS_BE,
                "Missing client block entities",
                "Recreates default client block entities when multiplayer chunk packets omit BE data.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_MODELDATA_CME,
                "ModelDataManager concurrency",
                "Uses ConcurrentHashMap key-sets and only refreshes model data on the main client thread.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_PAPER_CHUNK,
                "SortedArraySet removeIf (Paper)",
                "O(n) removeIf for SortedArraySet; disabled when Moonrise is present.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_REGOPS_CME,
                "RegistryOps memo ConcurrentHashMap",
                "Replaces RegistryOps memoized lookup map with ConcurrentHashMap.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_REMOVED_DIM,
                "Removed dimension world load",
                "Allows partial dimension DataResult decoding so worlds load after a dimension mod is removed.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_SHAPE_CACHE,
                "Mod shape cache thread-safety",
                "Swaps Refined Storage / Cyclic shape caches to ConcurrentHashMap when those mods are present.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_WORLD_LEAK,
                "Client world clear mitigation",
                "Clears ClientChunkCache storage, light engine, and blockEntityTickers on clearLevel to shrink leaked worlds.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_WORLD_SCREEN,
                "World list create-screen skip",
                "Prevents WorldSelectionList from closing CreateWorldScreen after delete-world when create is active.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_COFH_FLAGS,
                "CoFH FlagManager sync",
                "Synchronizes FlagManager.FLAGS map access to prevent concurrent getOrCreateFlag crashes under CoFH Core.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_RECIPE_BOOK,
                "Recipe book type desync guard",
                "Stops RecipeBookSettings from reading past the buffer for modded RecipeBookType ordinals when the packet is short (vanilla/mod mismatch).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_PAPER_CHUNKMAP,
                "Paper ChunkMap scheduling",
                "Uses main-thread executor for accessible-chunk prep and avoids premature chunk generation scheduling when parent futures are incomplete.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_CTM_CME,
                "CTM ResourceUtil cache sync",
                "Wraps CTM ResourceUtil.metadataCache in Collections.synchronizedMap to prevent concurrent metadata CMEs.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_CLIENT_MAP_SAFETY,
                "Client color/property map thread-safety",
                "Locks BlockColors/ItemColors.register, concurrent ItemProperties maps, and defers off-thread LivingEntityRenderer.addLayer to the client thread.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                MF_BE_THREAD,
                "Block entity map thread sanitizer",
                "Wraps ChunkAccess.blockEntities in a concurrency-sanitizing map so wrong-thread access fails fast.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_FORGE_FAKEPLAYER,
                "Forge FakePlayer factory clear",
                "Clears FakePlayerFactory static maps on server stop so FakePlayers do not leak across restarts.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_CREATE_EXTENDO,
                "Create ExtendoGrip damage-source leak",
                "Clears ExtendoGrip static last-active damage sources on player restoreFrom.",
                true,
                StackDomain.CREATE));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_CURIOS_CLONE,
                "Curios capability clone hygiene",
                "Invalidates and revives player caps on restoreFrom when Curios is present.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_GECKOLIB,
                "GeckoLib Molang / armor entity leak",
                "Resets GeckoLib Molang memoized entity suppliers and clears GeoArmorRenderer entity refs on client logout/clone.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_JEI,
                "JEI RecipeTransferButton clone leak",
                "Updates or clears JEI RecipeTransferButton player/container refs on client player clone and logout.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_FTB_LIBRARY,
                "FTB Library BLANK_GUI prevScreen",
                "Nulls GuiHelper.BLANK_GUI prevScreen on client clone/logout so prior screens cannot pin a ClientLevel.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_EMI,
                "EMI EmiHistory clear",
                "Clears EmiHistory screen stacks on client logout/clone so prior inventory screens cannot pin a ClientLevel.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_EMF,
                "EMF heldIteration clear",
                "Nulls LivingEntityRenderer emf$heldIteration residual fields on client level leave when Entity Model Features is present.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_ETF,
                "ETF heldEntity / player texture map",
                "Nulls LivingEntityRenderer etf$heldEntity and clears ETFManager.PLAYER_TEXTURE_MAP on client level leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_CREATEADDITION,
                "Create Addition EnergyNetworkManager",
                "Removes level keys from EnergyNetworkManager.instances on level unload / server stop.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_ARS_NOUVEAU,
                "Ars Nouveau clone caps + tome registry",
                "Invalidates/revives player caps on clone and clears CasterTomeRegistry static lists that capture RegistryAccess/level.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_IRONS_SPELLBOOKS,
                "Iron's Spellbooks client magic residual",
                "Clears ClientMagicData spell selection, SpellBarOverlay lastSelection, and DeadKingMusicManager on client logout/clone.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_JOURNEYMAP,
                "JourneyMap distance comparator player",
                "Nulls static player refs on JourneyMap EntityDistanceComparator-style classes on client logout.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_TOMBSTONE,
                "Tombstone LivingEntityRenderer entity",
                "Nulls Tombstone mixin residual LivingEntityRenderer.entity field on client level leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_ARCHITECTURY,
                "Architectury clientReceivables clone",
                "Re-keys NetworkManagerImpl.clientReceivables from the old player to the new player on PlayerEvent.Clone.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_MOUSE_TWEAKS,
                "Mouse Tweaks open screen residual",
                "Nulls Main.openScreen/handler/oldSelectedSlot on client clone/logout so prior screens cannot pin a ClientLevel.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_FINDME,
                "FindMe lastRenderedStack",
                "Resets FindMeModClient.lastRenderedStack on client logout/clone so tooltip stacks cannot pin a ClientLevel.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_CORPSE,
                "Corpse renderer cache clear",
                "Clears CorpseRenderer players/skeletons CachedMaps on client level leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_EASY_VILLAGERS,
                "Easy Villagers item caches",
                "Clears ItemTileEntityCache.CACHE and VillagerItem.cachedVillagers on client level leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_TOOLBELT,
                "Tool Belt clone cap hygiene",
                "Uses invalidateCaps/reviveCaps on player clone when Tool Belt is present (replaces revive()).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_TRAVELERSBACKPACK,
                "Traveler's Backpack clone + layer",
                "Cap hygiene on clone and clears backpack layer/model entity residuals on client leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_OCCULTISM,
                "Occultism clone cap hygiene",
                "Adds missing invalidateCaps/reviveCaps on player clone when Occultism is present.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_CITADEL,
                "Citadel animator + server data",
                "Nulls ModelAnimator entity residuals on client leave and soft-clears CitadelServerData statics on server stop.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_MOONLIGHT,
                "Moonlight soft level caches",
                "Best-effort clear of Moonlight TextureCache / client level-ish statics on client leave (soft).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_AETHER,
                "Aether DroppedItemCapability owner",
                "Nulls DroppedItemCapability owner entity refs on level unload / entity leave so owners cannot pin a prior Level.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_ALEXSMOBS,
                "Alex's Mobs world data maps",
                "Removes unloaded levels from ServerEvents.BEACHED_CACHALOT_WHALE_SPAWNER_MAP and AMWorldData.dataMap.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_FLYWHEEL,
                "Flywheel WorldAttached clear",
                "Clears Flywheel WorldAttached world maps on client level leave so prior ClientLevels cannot stick in the attached list.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_MINECOLONIES,
                "MineColonies recipe entity caches",
                "Nulls JobBasedRecipeCategory / GenericRecipe static and JEI-held citizen/entity caches on client level leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_PNC,
                "PneumaticCraft armor UI + drones",
                "Clears ArmorMainScreen.upgradeOptions on client clone/logout and unregisters drone entities from the Forge bus on leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_TWILIGHTFOREST,
                "Twilight Forest ENTITY_MAP / Hydra",
                "Clears TF JEI EntityRenderer.ENTITY_MAP and nulls HydraModel.hydra on client level leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_BETTERF3,
                "BetterF3 LocationModule chunk",
                "Nulls LocationModule chunkFuture / chunk fields on client level leave so chunks cannot pin a ClientLevel.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_BEANSBACKPACKS,
                "Beans Backpacks EnderStorage MAP",
                "Clears EnderStorage.MAP entries on client clone/logout so ender backpack data cannot pin a prior player/level.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_SERENESEASONS,
                "Serene Seasons snow + season maps",
                "Nulls LevelRenderer snow/rain level mixin field on client leave; removes levels from SeasonHandler.updateTicks/lastDayTimes on unload.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_MOWZIES,
                "Mowzie's boss music + animator",
                "Stops boss music / clears sunblock sounds and nulls MMModelAnimator entity residuals on client level leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_AE2WT,
                "AE2WT players + creative tab",
                "Removes cloned/logged-out players from CraftingTerminalHandler.players and clears AE2WTLibCreativeTab.items on client leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_BADPACKETS,
                "badpackets ChannelRegistry handlers",
                "Clears ChannelRegistry C2S/S2C handler sets on server stop and client leave so packet handlers cannot pin the prior session.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_BLUESKIES,
                "Blue Skies lastRidden + ambient",
                "Nulls blue_skies lastRidden entity and dungeonAmbientSoundHandler residual on client leave/clone.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_ICEBERG,
                "Iceberg EntityCollector + item renderer",
                "Removes client levels from EntityCollector.wrappedLevelsMap and nulls CustomItemRenderer entity/BE caches on leave/unload.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_FORBIDDEN_ARCANUS,
                "Forbidden Arcanus clone caps",
                "Calls invalidateCaps on the original player after death clone when Forbidden Arcanus is present (missing in mod clone handler).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_JER,
                "JER MobEntry entity residual",
                "Nulls entity residuals on JER MobEntry / villager entry static caches and soft-clears builder maps on client leave.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_MNA,
                "MNA FeyArmor renderEntity",
                "Nulls FeyArmorItem.renderEntity on client leave so armor render cannot pin a prior player/level.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_NUCLEARCRAFT,
                "NuclearCraft tooltip event",
                "Nulls TooltipHandler.processedEvent on client clone/leave so tooltip events cannot pin a prior player.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_PHOSPHOPHYLLITE,
                "Phosphophyllite players + ticking map",
                "Re-keys ConfigManager.players on clone and clears IIsTickingTracker isTickingMap on server stop.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_RAILCRAFT,
                "Railcraft charge networks",
                "Removes unloaded levels from ChargeProviderImpl.DISTRIBUTION.networks so charge nets cannot pin ServerLevel.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_SMALLSHIPS,
                "Small Ships ChunkMap fields",
                "Nulls smallships-injected ChunkMap.serverPlayer and clears list residual on level unload / server stop.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_MC_VANILLA,
                "Vanilla residual soft clear",
                "Nulls Minecraft crosshair/hitResult and ItemStack.EMPTY entityRepresentation on leave; pokes lastDamageSource on tick to drop removed entities.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_CYCLOPS,
                "CyclopsCore model world residual",
                "Tracks DelegatingDynamicItemAndBlockModel instances and nulls/rebinds world on client leave/login.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_EMI_LOOT,
                "EMI Loot EntityEmiStack residual",
                "Tracks EntityEmiStack instances; nulls entity on leave and recreates in the new ClientLevel on login.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_LDLIB,
                "LDLib ModularUI player residual",
                "Tracks ModularUI instances and nulls/rebinds entityPlayer on client leave/login.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                NET_LOGIN_TIMEOUT,
                "Login timeout 120s",
                "Raises ServerLoginPacketListenerImpl login tick budget from 30s to 120s for large packs.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                NET_READ_TIMEOUT,
                "Read timeout floor 120s",
                "Floors Netty ReadTimeoutHandler budgets to at least 120 seconds.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                NET_COMPRESSION_SIZE,
                "Compression decoder 16MiB",
                "Raises CompressionDecoder absolute size limits (vanilla 2MiB field / 8MiB uncompressed check) to 16MiB for large-pack payloads.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                NET_PLAY_TIMEOUT,
                "Play keep-alive timeout 60s",
                "Raises ServerGamePacketListenerImpl LATENCY_CHECK_INTERVAL from 15s to 60s to reduce disconnect.timeout under pack lag.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                NET_PAYLOAD_SPLIT,
                "Custom payload / frame size 16MiB",
                "Raises vanilla custom-payload ceilings (play S2C 1MiB, C2S 32KiB, login query 1MiB), PacketEncoder 8MiB→16MiB, and Varint21 frame length bytes 3→5 so large Forge channel payloads join without Payload may not be larger than… disconnects.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_HANDSHAKE,
                "Forge handshake re-tick",
                "Drains login payloads faster than one-per-tick and synchronizes sentMessages.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SPIN_WAIT,
                "Server tick park instead of spin",
                "Parks the server thread until nextTickTime instead of busy-spinning managedBlock.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_POTENTIAL_SPAWNS,
                "PotentialSpawns list reuse",
                "Avoids allocating fresh WeightedRandomList/ArrayLists for PotentialSpawns when spawn lists are unmodified.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RECIPE_RELOAD_LOG,
                "Recipe reload log storm",
                "Logs recipe apply failures as a single-line message instead of full exception dumps that flood reload logs.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_REGISTRY_OBJECT,
                "RegistryObject get without lambda",
                "Replaces RegistryObject.get() implementation to avoid per-call Optional/lambda allocation.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_THREAD_PRIORITY,
                "Background / integrated server thread priority",
                "Sets Minecraft util ForkJoin workers and the integrated server thread to priority 4 so client render/input stay responsive.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                FEATURE_CHAT_SIGNING_OFF,
                "Chat signing trust noise off",
                "Forces ChatTrustLevel.SECURE so unsigned/modded chat is not treated as untrusted UI noise.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                FEATURE_NARRATOR_LINUX,
                "Linux narrator quiet init",
                "Catches NarratorLinux init failures and returns EMPTY without dumping a full stack trace.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TAG_ID_CACHE,
                "TagEntry id location cache",
                "Caches TagEntry.elementOrTag() TagOrElementLocation to avoid per-call allocations during tag resolve.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_WALL_SHAPE_DEDUP,
                "WallBlock shape map dedup",
                "Reuses VoxelShape maps across vanilla WallBlocks that share default shape dimensions.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BLOCKSTATE_ENUM_CACHE,
                "BlockState cache enum arrays",
                "Reuses SupportType.values() and Direction.Axis.values() arrays when building BlockStateBase.Cache.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CREATIVE_TAB,
                "Creative tab contents memoize",
                "Skips CreativeModeTab.buildContents when ItemDisplayParameters are unchanged; invalidates non-category tabs when categories rebuild.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_COMPACT_BIT_STORAGE,
                "Compact empty PalettedContainer storage",
                "After network read, shrinks single-value chunk sections that allocated oversized empty bit storage.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_STRUCTURE_LOCATE,
                "Faster structure start check",
                "Short-circuits StructureCheck when ChunkGeneratorStructureState placements already forbid the chunk (MC-249136-class).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_OBJECT_HOLDER,
                "Forge ObjectHolder cleanup",
                "Removes redundant ObjectHolderRef entries after register events and replaces per-holder exception stacks with a shared stub.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BIOME_TEMP,
                "Biome temperature no cache",
                "Bypasses Biome temperature ThreadLocal cache that is ineffective on 1.20.1 and only adds overhead.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TICKING_CHUNK_ALLOC,
                "Ticking path allocation cuts",
                "Caches Bat Halloween LocalDate, reuses ChunkAccess structure-reference map views, and avoids Optional on ChunkHolder getTicking/FullChunk.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_REGISTRY_GROW,
                "MappedRegistry byId grow power-of-two",
                "Grows MappedRegistry byId ObjectArrayList by powers of two instead of size+1 on every registration.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_STATE_EMPTY_TABLE,
                "Empty StateHolder neighbour table",
                "Replaces empty neighbour tables with ImmutableTable.of after populateNeighbours when FerriteCore is not present.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                LEAK_LDLIB_DUMMYWORLD,
                "LDLib DummyWorld profiler supplier",
                "Rebinds DummyWorld Level profiler supplier to the source Level's supplier so DummyWorld cannot pin a dead ClientLevel via a capturing lambda.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MODEL_OPTS,
                "Model load/bake optimizations",
                "Interns Property names and ref-equals; skips BooleanProperty set equality; caches Transformation hashCode; caches multipart Selector predicates; stream-free MultiVariant parent resolve.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FORGE_REG_BITS,
                "ForgeRegistry free-id BitSet cache",
                "Caches nextClearBit during ForgeRegistry.add and invalidates on sync/clear/block; skips noisy per-registration trace logs.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PROFILE_TEXTURE,
                "Profile texture hash cache",
                "Caches MinecraftProfileTexture.getHash during SkinManager.registerTexture to avoid re-digesting profile texture URLs.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ATTRIBUTE_SUPPLIER,
                "AttributeSupplier compact + template intern",
                "Stores AttributeSupplier instances in OpenHashMap and interns identical AttributeInstance templates while entity suppliers are built at launch.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_COMPACT_ENTITY_MODELS,
                "Compact entity model cubes",
                "Deduplicates ModelPart.Cube instances produced by CubeDefinition.bake across shared UV/origin/size keys.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CACHE_STRONGHOLDS,
                "Stronghold ring position cache",
                "Disk-caches concentric-ring structure positions per dimension, computes them on a dedicated pool, and early-rejects isPlacementChunk outside ring radius.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ZIP_PACK_INDEX,
                "Zip resource pack index",
                "Builds a central-directory tree for FilePackResources so getNamespaces/listResources do not rescan the whole zip.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DEDICATED_RELOAD,
                "Dedicated resource reload executor",
                "Runs client/server resource and world-load reloads on a dedicated ForkJoin pool so they do not starve Util.backgroundExecutor.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FAST_FORGE_DUMMIES,
                "Fast Forge registry freeze dummy check",
                "Skips stream allocation in NamespacedWrapper.freeze when every holder is already bound.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_WORLDGEN_ALLOC,
                "Worldgen allocation cuts",
                "Index-loops MaterialRuleList/SequenceRule, OpenHashMap NoiseChunk.wrap without lambdas, reuses surface-rule biome supplier, and skips useless LazyCondition caches.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_COMPACT_IMPOSTER,
                "Compact ImposterProtoChunk",
                "Shares wrapped LevelChunk section arrays and sky-light sources into ImposterProtoChunk instead of duplicating them.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_INGREDIENT_DEDUP,
                "Ingredient ItemValue deduplication",
                "Interns identical Ingredient.ItemValue templates across recipe graphs and defensive-copies stacks on getItems.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FORGE_CAP_RETRIEVAL,
                "Faster Forge capability retrieval",
                "Checks ITEM_HANDLER capability identity before LivingEntity.isAlive, and forces AttachCapabilitiesEvent.isCancelable to a constant false.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SUSPEND_INTEGRATED,
                "Suspend integrated server during client load",
                "Holds full integrated-server world ticks until join packets finish applying (sentinel payload), and sleeps instead of yield-spinning in doWorldLoad.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FASTER_ITEM_RENDER,
                "Faster GUI item rendering",
                "Renders only camera-facing quads for vanilla SimpleBakedModel items/blocks in GUI contexts.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_COMPACT_MOJANG_REG,
                "Compact Mojang registries",
                "Stores only non-stable MappedRegistry lifecycles, memoizes VanillaRegistries.createLookup, and interns BlockStateData NBT constants.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CACHE_STRUCTURES,
                "Cache upgraded structures",
                "Disk-caches DFU-upgraded structure NBT under gameDir/omnifix/structureCacheV1 so outdated structure files do not re-run DFU every load.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DYNAMIC_DFU,
                "Lazy dynamic DFU",
                "Defers full Mojang DFU construction until first upgrade, skips register-time schema fetch, and clears DFU rewrite caches after idle.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_REMOVE_SPAWN_CHUNKS,
                "Remove permanent spawn chunks",
                "Stops permanent world-spawn region tickets; uses temporary START ticket at player for integrated load and PORTAL tickets for end-portal arrivals.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_OPTIMIZE_SURFACE,
                "Optimize surface rules",
                "Hoists biome-condition evaluation from chunk biome sets, uses ChunkBiomeLookup for Voronoi, and prefetches block columns during SurfaceSystem.buildSurface.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_REDUCE_BLOCKSTATE_CACHE,
                "Reduce BlockState cache rebuilds",
                "Skips eager initCache on Blocks.rebuildCache/GameData bake; regenerates shape/solid/tick cache on first access.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PATH_PACK_CACHE,
                "Path pack resource cache",
                "Indexes Forge PathPackResources trees for namespaces/list/exists and prevents double Forge pack-finder injection.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_COMPRESS_UNIHEX,
                "Compress unihex font storage",
                "Stores UnihexProvider byte/short line data in packed longs instead of full arrays.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DYNAMIC_STRUCTURE,
                "Soft structure template cache",
                "Replaces StructureTemplateManager's hard map with a soft-values Guava cache so templates can be GC'd under pressure.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CHUNK_MESHING,
                "Faster chunk mesh rebuild",
                "Uses SectionBlockPosIterator instead of Guava betweenClosed and reuses the already-fetched BlockState in RebuildTask.compile.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DEBUG_LEVEL_STATES,
                "Debug world state list reuse",
                "DebugLevelSource.initValidStates reuses Forge's blockstate IdMapper instead of stream-collecting a new ArrayList.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DYNAMIC_LANGUAGES,
                "Dynamic language map",
                "Stores language values as pack Resource refs where possible and soft-reloads JSON under memory pressure.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FASTER_INGREDIENTS,
                "Faster ingredients",
                "Tag ingredients test/stack without expanding stacks; soft-caches getItems; tracks server reloads so tag binds stay correct for KubeJS/CT.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_LAZY_SEARCH,
                "Lazy search tree build",
                "Defers SearchRegistry tree construction until the first non-empty search query.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FORGE_REG_ALLOC,
                "ForgeRegistry delegate hot-path",
                "Uses OpenHashMap for ForgeRegistry delegates and caches ACTIVE-stage holders on Block/Item via DelegateHolder.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FAKE_STATE_MAP,
                "FakeStateMap state definition (FerriteCore)",
                "Builds StateDefinition states into FakeStateMap arrays when FerriteCore is present for faster neighbour iteration.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_IMPOSTER_BE_GUARD,
                "ImposterProtoChunk live BE guard",
                "Hides level-attached BlockEntities from worldgen through ImposterProtoChunk when writes are disallowed.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RELEASE_PROTOCHUNKS,
                "Release idle protochunk holders",
                "Suspends non-FULL ChunkHolders after generation work finishes, clearing proto futures and saving so memory can be reclaimed.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DEBUG_RELOADER,
                "Profiled resource reloader",
                "Enables profiled reloads via -Domnifix.debugReloader=true, names listeners with FQCN, and sorts timing output by cost.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MEASURE_TIME,
                "Launch and world-join timing logs",
                "Logs vanilla bootstrap ms, game start seconds, and menu-to-ingame join time after recipes/tags settle.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_KEYMAP_PREWARM,
                "KeyMapping translation prewarm",
                "Resolves all KeyMapping translated components on the main thread during createSearchTrees to avoid off-thread GLFW/lazy crashes.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_INTEGRATED_WATCHDOG,
                "Integrated server tick watchdog",
                "Watches integrated server ticks; if one exceeds 40s, logs a full thread dump to diagnose hangs.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MEMORY_RESERVE,
                "Release MemoryReserve early",
                "Calls MemoryReserve.release() on client init so the unused vanilla reserve buffer is freed.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_NIGHTCONFIG_WATCH,
                "NightConfig watcher throttle",
                "Parks NightConfig FileWatcher 1s per loop to cut CPU/alloc from unthrottled values().iterator().",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                BUGFIX_NIGHTCONFIG_CRASH,
                "NightConfig config race fix",
                "Queues watch-thread reloads for /ofc|/ofsrc and locks per-mod config handlers against concurrent Forge posts.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MOD_SCAN_COMPACT,
                "ModFileScanData compact",
                "Drops unused annotation noise and interns Types across ModFileScanData after discovery.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MANIFEST_COMPACT,
                "Jar manifest digest compact",
                "Removes digest-only SecureJar manifest entries that are never re-read after verification.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MOD_WORK_QUEUE,
                "ModWorkManager park queue",
                "Replaces FML SyncExecutor task deque with a park+dummy-task queue so idle work does not spin.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_NETWORK_CONSTANTS_INIT,
                "NetworkConstants bootstrap init",
                "Calls NetworkConstants.init during Bootstrap to avoid Forge #9505 concurrent first-touch races.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FASTER_LOOT,
                "Faster loot table loading",
                "Pre-marks builtin loot JSON so ForgeHooks skips a ResourceManager.getResource() probe per table.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                BUGFIX_DEBUG_OVERLAY_CLEAR,
                "Debug overlay clear on leave",
                "Clears ForgeGui DebugScreenOverlay chunk cache when the client level is set to null.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FASTER_TEXTURE_STITCH,
                "Faster texture stitching (STB)",
                "Uses STB rect pack for Stitcher atlases with ≥100 sprites; small atlases stay vanilla.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MIXIN_INJECTOR_GROUP,
                "Mixin injector group leak patch",
                "Replaces InjectorGroupInfo.NO_GROUP members with a no-op list (Mixin PR #99 class).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CLEAR_MIXIN_CLASSINFO,
                "Clear Mixin ClassInfo after launch",
                "Audits then clears Mixin ClassInfo/ClassNode maps to reclaim memory. Default off for compatibility.",
                false));

        FeatureUnitRegistry.register(new FeatureUnit(
                FEATURE_MCFUNCTION_PROFILING,
                "mcfunction tick profiling",
                "Times #minecraft:tick functions; dump with /omnifix mcfunctions. Default off.",
                false));

        FeatureUnitRegistry.register(new FeatureUnit(
                FEATURE_REGISTRY_PROGRESS,
                "Registry event progress + splash",
                "Client: progress bars per RegisterEvent and async splash redraw during postRegisterEvents.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PATCHOULI_BOOKS,
                "Patchouli book AIR stack dedup",
                "After Patchouli reload, replaces AIR ItemStacks on templates with EMPTY to drop NBT waste.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_MOB_FENCE_ESCAPE,
                "Mob fence escape (AABB persist)",
                "MC-2025: save/load entity AABB so collision against fences survives chunk unload/reload.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_SLOW_FALL_PARTICLES,
                "Flying entity land particles",
                "MC-30391: skip checkFallDamage land particles for chicken, blaze, and wither.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_PARTIAL_CHUNK_SAVE,
                "Partial protochunk save",
                "MC-224729: include protochunks and always-accessible holders in saveAllChunks flushes.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                VANILLA_PISTON_RELOAD,
                "Piston BE order on reload",
                "MC-89146: use linked BE map so piston/move update order is preserved across reload.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RESOURCEFULLIB_HIGHLIGHTS,
                "ResourcefulLib highlight dedup",
                "Interns highlight points/lines after ResourcefulLib HighlightHandler.apply.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_HOPPER_ENTITY_CACHE,
                "Hopper item-entity suck cache",
                "Caches empty below-hopper entity query for the current game tick to cut getEntities pressure.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                FEATURE_OMNIFIX_BRANDING,
                "OmniFix F3 branding",
                "Adds OmniFix version to BrandingControl / F3 brand list.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                FEATURE_STDOUT_LOG,
                "Mirror stdout/stderr to log",
                "Redirects System.out/err through log4j for crash diagnosis. Default off.",
                false));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CLIENT_ENTITY_COLLISION,
                "Client entity collision skip",
                "On client, skips entity–entity collision work for non-controlled entities; server is authoritative.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DIRECTION_VALUES_CACHE,
                "Direction.values() hot-path cache",
                "Redirects Direction.values() / Axis.values() on neighbour-update hot paths to shared arrays.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ITEM_ENTITY_MERGE_CACHE,
                "ItemEntity merge empty cache",
                "Same-tick empty cache for ItemEntity neighbour merge scans to cut getEntities under item dumps.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ADVANCEMENT_RELOAD_LOG,
                "Advancement reload log compact",
                "Logs advancement parse failures as location + message only (no full stack storms).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                FEATURE_FORCE_CLOSE_LOADING,
                "Force-close world loading screen",
                "Dismisses join/loading screens once client level and local player are ready.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PATH_RECALC_THROTTLE,
                "PathNavigation repath throttle",
                "Avoids full path recompute every tick for stationary mobs when the goal target is unchanged.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                BUGFIX_UUID_LOG_SPAM,
                "UUID duplicate entity log throttle",
                "Rate-limits or compact-logs repeated 'UUID of entity already exists' warnings.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SKIP_EMPTY_RANDOM_TICK,
                "Skip empty chunk random ticks",
                "Skips LevelChunk random-tick work when no sections need random ticks this cycle.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_XP_ORB_SCAN_CACHE,
                "ExperienceOrb merge scan cache",
                "Same-tick empty cache for ExperienceOrb.scanForEntities getEntities under XP dumps.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_GOAL_SELECTOR_RATE,
                "GoalSelector new-goal rate 5",
                "Sets GoalSelector newGoalRate from 3 to 5 after construct; running goals still tick every tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PARTICLE_EMPTY_TICK,
                "Skip empty ParticleEngine tick",
                "Cancels ParticleEngine.tick when particles, emitters, and particlesToAdd are all empty.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_EMPTY_BE_TICK,
                "Skip empty block-entity tick",
                "Cancels Level.tickBlockEntities when ticker lists and fresh BE queues are all empty.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_EMPTY_BLOCK_DROPS,
                "Empty block drops short-circuit",
                "Returns empty list from BlockState.getDrops when the block's loot table is EMPTY.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ENTITY_SECTION_MAP,
                "Entity section ClassInstanceMultiMap",
                "Uses fastutil OpenHashMap/ObjectArrayList for ClassInstanceMultiMap by-class entity indexes.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BREWING_STAND_IDLE,
                "Brewing stand idle skip",
                "Cancels BrewingStand.serverTick when not brewing, not brewable, and not loading blaze fuel.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SENSOR_SCAN_RATE,
                "Brain sensor scan rate +50%",
                "Increases Sensor scanRate by half (capped at 80) so doTick runs less often; mild AI trade-off.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ARROW_INGROUND,
                "In-ground arrow tick throttle",
                "Server-side: every other tick skip AbstractArrow.tick while inGround (pickup/despawn still advance).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FURNACE_IDLE,
                "Furnace/smoker/blast idle skip",
                "Cancels AbstractFurnace.serverTick when not lit and both input and fuel slots are empty.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CAMPFIRE_IDLE,
                "Campfire idle skip",
                "Cancels Campfire cookTick when all slots empty; cooldownTick when no cooking progress.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TARGET_GOAL_INTERVAL,
                "NearestAttackableTarget interval +50%",
                "Increases target-search randomInterval by half (cap 40) so findTarget is less frequent.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DIRECTION_GET_NEAREST,
                "Direction.getNearest exact cache",
                "Memoizes Direction.getNearest for identical double-bit vectors (correctness-safe).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BEEHIVE_EMPTY,
                "Empty beehive tick skip",
                "Cancels BeehiveBlockEntity.serverTick when stored bee list is empty.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_JUKEBOX_IDLE,
                "Idle jukebox tick skip",
                "Cancels Jukebox playRecordTick when not playing and disc slot is empty.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_EMPTY_EFFECTS_TICK,
                "Empty potion-effects tick skip",
                "Cancels LivingEntity.tickEffects when activeEffects is empty and effectsDirty is false.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_EFFECTS_MAP,
                "activeEffects OpenHashMap",
                "Replaces LivingEntity.activeEffects HashMap with Reference2ObjectOpenHashMap after construct.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BELL_IDLE,
                "Bell idle skip",
                "Cancels BellBlockEntity client/server ticks when not shaking and not resonating.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SHULKER_BOX_IDLE,
                "Shulker box idle skip",
                "Cancels ShulkerBoxBlockEntity.tick when animation is CLOSED or fully OPENED.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CHEST_LID_IDLE,
                "Chest lid idle skip",
                "Cancels ChestBlockEntity.lidAnimateTick when lid openness is 0 and shouldBeOpen is false.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SIGN_EDIT_IDLE,
                "Sign edit-idle skip",
                "Cancels SignBlockEntity.tick when no playerWhoMayEdit UUID is set (vast majority of signs).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ENCHANT_TABLE_IDLE,
                "Enchanting table closed throttle",
                "Client: skip bookAnimationTick 3/4 of the time when open/oOpen are 0 (still re-opens within 4 ticks).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_AVOID_ENTITY_THROTTLE,
                "AvoidEntityGoal scan throttle",
                "AvoidEntityGoal.canUse only runs entity AABB scans every 3rd mob tick (mild flee latency trade-off).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RANDOM_STROLL_INTERVAL,
                "RandomStrollGoal interval +50%",
                "Increases RandomStrollGoal interval by half (cap 240) so pathfinding starts less often.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SPAWNER_NEAR_CACHE,
                "Spawner isNearPlayer same-tick cache",
                "Memoizes BaseSpawner.isNearPlayer for the same spawner within one Level gameTime tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SCULK_CATALYST_IDLE,
                "Sculk catalyst idle skip",
                "Cancels SculkCatalystBlockEntity.serverTick when the sculk spreader cursor list is empty.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FOLLOW_PARENT_THROTTLE,
                "FollowParentGoal scan throttle",
                "FollowParentGoal.canUse only scans for parents every 3rd animal tick (mild baby follow latency).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TEMPT_GOAL_THROTTLE,
                "TemptGoal player-scan throttle",
                "TemptGoal.canUse nearest-player query runs every 2nd mob tick when not in calmDown.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BREED_GOAL_THROTTLE,
                "BreedGoal partner-scan throttle",
                "BreedGoal.canUse partner entity scan runs every 2nd tick while the animal is in love.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MINECART_HOPPER_CACHE,
                "Hopper minecart empty suction cache",
                "Same-tick empty memo for MinecartHopper.suckInItems entity AABB path (independent of BE hoppers).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BEG_GOAL_THROTTLE,
                "BegGoal player-scan throttle",
                "BegGoal.canUse (wolves begging) only scans players every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CONDUIT_INACTIVE,
                "Inactive conduit tick throttle",
                "Inactive conduits only run full serverTick on the 40-tick shape-refresh boundary (tickCount still advances).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_HANGING_SURVIVE,
                "Hanging entity survive-check interval",
                "Item frames/paintings run survives() collision check every 150 ticks instead of 100.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FOLLOW_OWNER_REPATH,
                "FollowOwnerGoal repath delay +50%",
                "Pets repath to owner every 15 adjusted ticks instead of 10 while following.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_OPENERS_RECHECK,
                "Container openers recheck delay",
                "While a chest/barrel is open, opener recheck block ticks schedule at 8 instead of 5.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MOVE_VILLAGE_THROTTLE,
                "MoveThroughVillageGoal POI throttle",
                "MoveThroughVillageGoal.canUse village POI scans only every 3rd mob tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FLEE_SUN_THROTTLE,
                "FleeSunGoal scan throttle",
                "FleeSunGoal.canUse (day/fire/sky checks + path) only every 3rd mob tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RESTRICT_SUN_THROTTLE,
                "RestrictSunGoal scan throttle",
                "RestrictSunGoal.canUse only every 3rd mob tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MOVE_TO_BLOCK_INTERVAL,
                "MoveToBlockGoal search interval +50%",
                "MoveToBlockGoal.nextStartTick uses 300+random instead of 200+random (capped search rate).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_STROLL_VILLAGE_INTERVAL,
                "StrollThroughVillageGoal interval +50%",
                "Increases StrollThroughVillageGoal interval by half (cap 240) before village section scans.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_XP_ORB_SCAN_PERIOD,
                "XP orb entity scan period 20→30",
                "ExperienceOrb.scanForEntities runs every 30 ticks instead of 20 (player follow + merge).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_REMOVE_BLOCK_THROTTLE,
                "RemoveBlockGoal scan throttle",
                "RemoveBlockGoal.canUse only every 3rd mob tick (enderman/silverfish style break AI).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_LEAP_TARGET_THROTTLE,
                "LeapAtTargetGoal chance throttle",
                "LeapAtTargetGoal.canUse succeeds half as often (random gate tightened).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_END_GATEWAY_SCAN,
                "End gateway entity scan throttle",
                "TheEndGateway teleportTick entity AABB scan runs every other tick when not cooling down.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FOLLOW_MOB_THROTTLE,
                "FollowMobGoal scan throttle",
                "FollowMobGoal.canUse inflated AABB mob scans only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FOLLOW_MOB_REPATH,
                "FollowMobGoal repath delay +50%",
                "While following, FollowMobGoal repaths every 15 adjusted ticks instead of 10.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DEFEND_VILLAGE_THROTTLE,
                "DefendVillageTargetGoal scan throttle",
                "Iron golem village-defend target scans only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_OFFER_FLOWER_THROTTLE,
                "OfferFlowerGoal scan throttle",
                "Iron golem offer-flower villager scans only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RUN_CRAZY_THROTTLE,
                "RunAroundLikeCrazyGoal throttle",
                "RunAroundLikeCrazyGoal.canUse only every 3rd tick for untamed horses.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_LOOK_AT_PROBABILITY,
                "LookAtPlayerGoal probability ×2/3",
                "Reduces LookAtPlayerGoal look probability by one third to cut idle nearest-entity looks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FOLLOW_BOAT_THROTTLE,
                "FollowBoatGoal scan throttle",
                "FollowBoatGoal.canUse boat/player entity scans only every 3rd mob tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_LAND_SHOULDER_THROTTLE,
                "LandOnOwnersShoulderGoal throttle",
                "Parrot land-on-shoulder canUse only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RESET_ANGER_THROTTLE,
                "ResetUniversalAngerTargetGoal throttle",
                "Universal anger reset canUse only every 3rd tick (alerts still fire when goal starts).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CAT_SIT_THROTTLE,
                "CatSitOnBlockGoal throttle",
                "Cat sit-on-block canUse only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CAT_LIE_THROTTLE,
                "CatLieOnBedGoal throttle",
                "Cat lie-on-bed canUse only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PANIC_THROTTLE,
                "PanicGoal throttle (safe when not on fire)",
                "PanicGoal.canUse every other tick unless mob is on fire (preserves urgent water search).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TRADE_PLAYER_THROTTLE,
                "TradeWithPlayerGoal throttle",
                "Villager trade-with-player canUse only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DOLPHIN_JUMP_INTERVAL,
                "DolphinJumpGoal interval +50%",
                "Increases DolphinJumpGoal interval by half (cap 240) before jump attempts.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_EAT_BLOCK_THROTTLE,
                "EatBlockGoal scan throttle",
                "EatBlockGoal.canUse (grass eat) only every 3rd mob tick after vanilla random gate.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_CLIMB_POWDER_THROTTLE,
                "Climb powder-snow goal throttle",
                "ClimbOnTopOfPowderSnowGoal.canUse only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MOVE_RESTRICTION_THROTTLE,
                "MoveTowardsRestrictionGoal throttle",
                "Restriction-return random-pos path search only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MOVE_TARGET_THROTTLE,
                "MoveTowardsTargetGoal throttle",
                "MoveTowardsTargetGoal.canUse only every 2nd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RANGED_ATTACK_INTERVAL,
                "RangedAttackGoal interval +50%",
                "Raises RangedAttackGoal attackIntervalMin/Max by half (cap 80) for slower volleys.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RANGED_BOW_INTERVAL,
                "RangedBowAttackGoal interval +50%",
                "Raises RangedBowAttackGoal attackIntervalMin by half (cap 60).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PATHFIND_RAID_THROTTLE,
                "PathfindToRaidGoal throttle",
                "PathfindToRaidGoal.canUse only every 3rd raider tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BREAK_DOOR_THROTTLE,
                "BreakDoorGoal throttle",
                "BreakDoorGoal.canUse only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FLOAT_GOAL_THROTTLE,
                "FloatGoal dry-path throttle",
                "FloatGoal.canUse skips fluid-height work every 2 of 3 ticks when not already in water/lava; wet path stays hot.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MELEE_CANUSE_COOLDOWN,
                "MeleeAttackGoal canUse cooldown +50%",
                "Raises MeleeAttackGoal canUse re-check cooldown from 20 to 30 game ticks to cut createPath storms.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RANGED_CROSSBOW_DELAY,
                "RangedCrossbowAttackGoal delay +50%",
                "Raises post-charge attackDelay base and random range by half (20→30) for pillagers/crossbow users.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DOOR_INTERACT_THROTTLE,
                "DoorInteractGoal throttle",
                "DoorInteractGoal.canUse (path door probe / OpenDoorGoal) only every 3rd tick when colliding.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RANDOM_LOOK_PROBABILITY,
                "RandomLookAroundGoal probability x2/3",
                "Reduces RandomLookAroundGoal idle look probability by one third across most mobs.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_LLAMA_CARAVAN_THROTTLE,
                "LlamaFollowCaravanGoal throttle",
                "Llama caravan join scans only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FOLLOW_FLOCK_REPATH,
                "FollowFlockLeaderGoal repath +50%",
                "Schooling fish repath-to-leader interval 10→15 ticks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_AEC_SCAN_PERIOD,
                "AreaEffectCloud scan period +60%",
                "Raises AreaEffectCloud living-entity potion scan from every 5 to every 8 ticks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TRY_FIND_WATER_THROTTLE,
                "TryFindWaterGoal throttle",
                "TryFindWaterGoal.canUse only every 3rd tick (water-block seek for turtles/frogs).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_OCELOT_ATTACK_REPATH,
                "OcelotAttackGoal repath every 2nd tick",
                "Ocelot/cat attack moveTo pathing runs every other tick; attack damage timing unchanged.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MOVE_BACK_VILLAGE_THROTTLE,
                "MoveBackToVillageGoal throttle",
                "MoveBackToVillageGoal.canUse (village section + stroll) only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RANDOM_STAND_THROTTLE,
                "RandomStandGoal throttle",
                "Horse RandomStandGoal.canUse only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_USE_ITEM_THROTTLE,
                "UseItemGoal throttle",
                "UseItemGoal.canUse (predicate item-use goals) only every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SWELL_GOAL_THROTTLE,
                "SwellGoal throttle (safe when not swelling)",
                "Creeper SwellGoal.canUse every other tick unless already swelling (getSwellDir>0).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ITEM_MERGE_PERIOD,
                "ItemEntity merge period +50% when still",
                "Stationary ItemEntity merge neighbour scan interval 40→60 ticks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BREATH_AIR_PATH,
                "BreathAirGoal path search throttle",
                "BreathAirGoal air-position search every 2nd tick unless air supply is critical (<60).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SIT_ORDERED_THROTTLE,
                "SitWhenOrderedToGoal throttle",
                "SitWhenOrderedToGoal.canUse every 3rd tick when not ordered; ordered sits stay full-rate.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_NEAREST_ITEM_RANGE,
                "NearestItemSensor range 32→24",
                "Brain NearestItemSensor wanted-item AABB inflate XZ/Y and max distance 32→24.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SECONDARY_POI_RADIUS,
                "SecondaryPoiSensor radius 4→3",
                "Villager secondary job-site block scan radius 4→3 (Y span unchanged).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ARMOR_STAND_MARKER_PUSH,
                "ArmorStand marker push skip",
                "Marker armor stands skip pushEntities minecart entity scans.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SHULKER_ATTACH_THROTTLE,
                "Shulker attach recheck throttle",
                "Shulker findNewAttachment / teleport attach work every 3rd tick.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MELEE_PATH_RECALC,
                "MeleeAttackGoal path recalc base +50%",
                "Raises MeleeAttackGoal ticksUntilNextPathRecalculation base from 4 to 6.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ITEM_STILL_PHYSICS,
                "ItemEntity still physics period +50%",
                "Stationary ItemEntity physics/impulse interval 4→6 ticks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_LIVING_PUSH_THROTTLE,
                "LivingEntity pushEntities every 2nd tick",
                "Non-player LivingEntity.pushEntities runs every other tick (players unthrottled).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_NEAREST_LIVING_RADIUS,
                "NearestLivingEntitySensor radius 16→12",
                "Brain nearest-living scan AABB radius XZ/Y reduced from 16 to 12.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PLAYER_SENSOR_RANGE,
                "PlayerSensor range 16→12",
                "Brain PlayerSensor closerThan player scan range 16→12 blocks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_NEAREST_BED_SCAN,
                "NearestBedSensor cheaper baby bed search",
                "NearestBedSensor HOME POI radius 48→36 and batch size 5→3.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_HURT_ALERT_Y,
                "HurtByTargetGoal alert Y 10→7",
                "Pack alertOthers vertical inflate reduced from 10 to 7 blocks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TARGET_SEARCH_Y,
                "NearestAttackableTargetGoal search Y 4→3",
                "Target search AABB vertical inflate 4→3 blocks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FLYING_HOVER_RADIUS,
                "Flying random hover radius 8→6",
                "WaterAvoidingRandomFlyingGoal HoverRandomPos/AirAndWater radius 8→6.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_MINECART_PUSH_THROTTLE,
                "Minecart low-speed push scan throttle",
                "AbstractMinecart entity push/ride scans every other tick when nearly still.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BEE_POLLINATE_COOLDOWN,
                "Bee flower-retry cooldown +50%",
                "BeePollinateGoal failed flower search cooldown 20–60 → 30–90 ticks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TEMPTING_SENSOR_RANGE,
                "TemptingSensor range 10→8",
                "Brain TemptingSensor player temptation range reduced from 10 to 8 blocks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BEE_HIVE_LOCATE,
                "Bee hive locate cheaper",
                "BeeLocateHiveGoal BEE_HOME POI range 20→15 and post-locate cooldown 200→300.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PHANTOM_SCAN,
                "Phantom player scan cheaper",
                "PhantomAttackPlayerTargetGoal rescan interval 60→90 and XZ inflate 16→12.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_OWNER_HURT_BY_THROTTLE,
                "OwnerHurtByTargetGoal throttle",
                "OwnerHurtByTargetGoal.canUse only every 2nd tick (pet revenge on owner attacker).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_OWNER_HURT_TARGET_THROTTLE,
                "OwnerHurtTargetGoal throttle",
                "OwnerHurtTargetGoal.canUse only every 2nd tick (pet assist owner attacks).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_WATER_AVOID_STROLL_RADIUS,
                "WaterAvoidingRandomStroll radii -20%",
                "WaterAvoidingRandomStrollGoal LandRandomPos sample radii 15→12 (wet) and 10→8 (dry).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_HEALABLE_RAIDER_COOLDOWN,
                "Healable raider target cooldown +50%",
                "NearestHealableRaiderTargetGoal start cooldown 200→300 ticks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ENDERMAN_TAKE_INTERVAL,
                "Enderman take-block interval +50%",
                "EndermanTakeBlockGoal random take attempt interval 20→30 (reducedTickDelay base).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_WARDEN_SENSOR_RADIUS,
                "WardenEntitySensor radius 24→20",
                "Warden nearest-living sensor radius XZ/Y reduced from 24 to 20.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_AXOLOTL_ATTACK_RANGE,
                "Axolotl attackable range 8→6",
                "AxolotlAttackablesSensor isClose distance squared 64→36 (8→6 blocks).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FROG_ATTACK_RANGE,
                "Frog attackable range 10→8",
                "FrogAttackablesSensor closerThan hunt range 10→8 blocks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ENDERMAN_LEAVE_INTERVAL,
                "Enderman leave-block interval +50%",
                "EndermanLeaveBlockGoal place attempt interval 2000→3000.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_GHAST_WANDER_RADIUS,
                "Ghast wander radius 16→12",
                "Ghast RandomFloatAroundGoal wanted-position offset 16→12.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_GHAST_FIREBALL_CHARGE,
                "Ghast fireball charge 20→30",
                "GhastShootFireballGoal charge-to-fire threshold 20→30 ticks.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SILVERFISH_WAKE_SCAN,
                "Silverfish wake-friends scan smaller",
                "SilverfishWakeUpFriendsGoal infest-block scan XZ 10→8 and Y 5→4.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BEE_WANDER_CHANCE,
                "Bee wander chance lower",
                "BeeWanderGoal canUse nextInt gate 10→15 (fewer wander path starts).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BLAZE_FIRE_INTERVAL,
                "Blaze fire volley interval +50%",
                "BlazeAttackGoal charge-up 60→90 and post-volley cooldown 100→130.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_GUARDIAN_ATTACK_DURATION,
                "Guardian laser duration +25%",
                "Guardian.getAttackDuration 80→100 (slower laser complete; elder has own duration).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_VEX_RANDOM_MOVE,
                "Vex random-move chance lower",
                "VexRandomMoveGoal canUse nextInt gate 7→10.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_VEX_CHARGE_CHANCE,
                "Vex charge chance lower",
                "VexChargeAttackGoal canUse nextInt gate 7→10.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_HOGLIN_REPELLENT_RANGE,
                "Hoglin repellent scan 8,4→6,3",
                "HoglinSpecificSensor findNearestRepellent BlockPos search 8/4 → 6/3.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PIGLIN_REPELLENT_RANGE,
                "Piglin repellent scan 8,4→6,3",
                "PiglinSpecificSensor findNearestRepellent BlockPos search 8/4 → 6/3.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ALLAY_HEAL_PERIOD,
                "Allay heal period +50%",
                "Allay aiStep passive heal gate tickCount%10 → %15.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_RABBIT_RAID_RANGE,
                "Rabbit garden raid search 16→12",
                "Rabbit RaidGardenGoal MoveToBlockGoal search range 16→12.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_SLIME_JUMP_DELAY,
                "Slime jump delay +50%",
                "Slime.getJumpDelay nextInt base 20→30 and offset 10→15 (magma multiplies further).",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_ELDER_GUARDIAN_ATTACK,
                "Elder guardian laser duration +33%",
                "ElderGuardian.getAttackDuration 60→80.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_EVOKER_FANG_INTERVAL,
                "Evoker fang spell interval +50%",
                "EvokerAttackSpellGoal getCastingInterval 100→150.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_EVOKER_SUMMON_INTERVAL,
                "Evoker summon-vex interval +32%",
                "EvokerSummonSpellGoal getCastingInterval 340→450.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_EVOKER_WOLOLO_INTERVAL,
                "Evoker wololo interval +43%",
                "EvokerWololoSpellGoal getCastingInterval 140→200.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TURTLE_GO_HOME_CHANCE,
                "Turtle go-home chance lower",
                "TurtleGoHomeGoal random gate reducedTickDelay(700)→1000.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_TURTLE_LAY_EGG_DURATION,
                "Turtle lay-egg duration +50%",
                "TurtleLayEggGoal lay counter threshold 200→300.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BEE_GROW_CROP_INTERVAL,
                "Bee grow-crop interval +50%",
                "BeeGrowCropGoal crop-tick nextInt gate 30→45.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PANDA_ROLL_CHANCE,
                "Panda roll chance lower",
                "PandaRollGoal playful 60→90 and normal 500→750 reducedTickDelay gates.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PANDA_SNEEZE_CHANCE,
                "Panda sneeze chance lower",
                "PandaSneezeGoal weak 500→750 and normal 6000→8000 reducedTickDelay gates.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_POLAR_BEAR_CUB_SCAN,
                "Polar bear cub-scan smaller",
                "PolarBearAttackPlayersGoal baby-bear inflate 8,4 → 6,3.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_DROWNED_WATER_SEARCH,
                "Drowned water-search cheaper",
                "DrownedGoToWaterGoal sample count 10→7 and horizontal offset span 20→16.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BEE_GO_HIVE_TIMEOUT,
                "Bee go-hive travel timeout +33%",
                "BeeGoToHiveGoal travellingTicks fail threshold 600→800.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_BEE_GO_FLOWER_TIMEOUT,
                "Bee go-flower travel timeout +33%",
                "BeeGoToKnownFlowerGoal travellingTicks fail threshold 600→800.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_FISHING_OPEN_WATER_SCAN,
                "Fishing open-water scan smaller",
                "FishingHook.calculateOpenWater horizontal area ±2→±1 per layer.",
                true));

        FeatureUnitRegistry.register(new FeatureUnit(
                PERF_PANDA_SIT_ITEM_SCAN,
                "Panda sit item-scan smaller",
                "PandaSitGoal ItemEntity inflate canUse 6→4 and start 8→6.",
                true));
    }

    static {
        registerBuiltins();
    }
}
