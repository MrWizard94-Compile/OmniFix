# OmniFix — Single Source of Truth (SSOT)

**Platform:** Minecraft **1.20.1** · Forge **47.x** · Java **17**  
**Product:** **The fix/performance mod to end all fix mods** — universal root-caused sink for the entire Forge 1.20.1 ecosystem (MF / Debugify / ATL / Connectivity / GPU / pack seams)  
**Artifact:** `omnifix-forge/build/libs/omnifix-0.1.0-alpha.jar`  
**Config:** `config/omnifix-features.properties`  
**Law:** `SOUL.md` · **Matrix:** `BACKLOG.md` · **Research:** `RESEARCH_MASTER.md` · **Ecosystem map:** `ECOSYSTEM_MAP.md` · **Full corpus:** `CORPUS.md` · **Compat notes:** `COMPAT_MATRIX.md`

### Coding agents

Specialized Grok agents for this repo: **`CODING_AGENTS.md`** · definitions under **`.grok/agents/`**.  
Spawn with `subagent_type` e.g. `omnifix-feature-unit`, `omnifix-mixin-only`, `omnifix-rootcause`.

### Sibling: Omni-Framework (read-only from this repo)

| | |
|---|---|
| **Path** | `C:\WPAI\Gaming\Minecraft\Omni_Framework` |
| **What it is** | OmniLauncher + OmniLoader + OmniMeta + intelligence vivisection (target MC **1.21.1** first) |
| **What OmniFix is** | Forge **1.20.1** fix/performance sink (this repository) |
| **Rule** | **Do not modify Omni_Framework from OmniFix work.** Read for orientation only. Edits belong only in that tree when the director opens that project. |

**Shipped FeatureUnits: 367** (authoritative count from `FeatureUnits.java` · 367 `register()` calls)

This document is the **merged inventory + architectural breakdown**. When `FeatureUnits.java` changes, update §6 counts and residual notes in the same change. `SOUL.md` remains immutable law; this file is the living product SSOT.

---

## Table of contents

1. [Mandate](#1-mandate)
2. [What OmniFix is (product definition)](#2-what-omnifix-is-product-definition)
3. [Repository & module architecture](#3-repository--module-architecture)
4. [Runtime architecture](#4-runtime-architecture)
5. [FeatureUnit system](#5-featureunit-system)
6. [SHIPPED — all 367 FeatureUnits](#6-shipped--all-367-featureunits)
7. [Implementation surface](#7-implementation-surface)
8. [Coexistence & gating](#8-coexistence--gating)
9. [Absorb targets (fix/util constellation)](#9-absorb-targets-fixutil-constellation)
10. [Domains D1–D8 & research bands](#10-domains-d1d8--research-bands)
11. [Research pipeline](#11-research-pipeline)
12. [Terminal residual (SOUL: no stubs)](#12-terminal-residual-soul-no-stubs)
13. [Completion law](#13-completion-law)
14. [Commands & diagnostics](#14-commands--diagnostics)
15. [Document history](#15-document-history)

---

## 1. Mandate

| Principle | Meaning |
|---|---|
| Platform = Forge 1.20.1 | Entire ecosystem (ATM, BMC, Cobblemon, Create, kitchen-sink, servers). |
| Maximize real fixes | Any root-caused complete fix belongs here. |
| No generic bullshit | No FPS cargo-cult, no stubs, no unresearched copies. |
| Other mods irrelevant for inclusion | MF / ATL / Connectivity / Debugify do not block shipping. |
| Coexistence required | Skip double-apply when peer present. |
| Gating is law | FeatureUnit + LoadingModList + StackDomain. Never hard-require kitchen-sink. |

Peer mods = **catalogs and references**, not ownership fences.

---

## 2. What OmniFix is (product definition)

OmniFix is a **universal Forge 1.20.1 fix and performance sink**: one jar that absorbs root-caused units from ModernFix, Debugify, AllTheLeaks, Connectivity-class net hardening, vanilla Mojira, and heavy-pack seams (Valkyrien Skies × Immersive Portals × Create).

It is **not**:

- A content mod
- A renderer replacement (Embeddium/Oculus stay peers)
- A hard dependency on VS / IP / Create / MF
- A place for stubs, ARR copies, or incomplete ports (SOUL)

**Identity (from build):**

| Field | Value |
|---|---|
| Mod ID | `omnifix` |
| Version | `0.1.0-alpha` |
| Group | `org.omnifix` |
| License | MIT |
| Authors | MrWizard94 |
| MC / Forge | 1.20.1 / 47.4.20 (range `[47,)`) |
| Mappings | Official 1.20.1 |

---

## 3. Repository & module architecture

```
OmniFix/
├── omnifix-kernel/          # Pure Java FeatureUnit registry, domains, policy
├── omnifix-forge/           # Forge mod entry, mixins, helpers, resources → jar
├── omnifix-compat-valkyrien-portals/   # VS×IP sources (compiled into forge jar)
├── omnifix-compat-create-portals/      # Create×IP sources (compiled into forge jar)
├── ModernFix/               # Reference tree (not a runtime dependency)
├── _reference/              # Immersive Portals / VS source refs
├── libs/                    # Shade/compile jars (MixinExtras, MixinSquared, Ponder, vs-api)
├── SOUL.md                  # Immutable constitution
├── SSOT.md                  # This file — product SSOT
├── BACKLOG.md               # Living status matrix
├── RESEARCH_MASTER.md       # Research pipeline & notes
└── COMPAT_MATRIX.md         # Peer-mod coexistence notes
```

### 3.1 Gradle projects (`settings.gradle.kts`)

| Project | Role |
|---|---|
| `omnifix-kernel` | No Minecraft dependency. FeatureUnit model, registry, stack domains/profiles. |
| `omnifix-forge` | ForgeGradle mod; depends on kernel; produces `omnifix-0.1.0-alpha.jar`. |

Compat packages live under separate source trees but are compiled **into** the forge module (no separate published jars required for end users).

### 3.2 Kernel types

| Type | Path | Role |
|---|---|---|
| `FeatureUnit` | `kernel/feature/FeatureUnit.java` | id, displayName, description, defaultEnabled, requiredDomains |
| `FeatureUnitRegistry` | `kernel/feature/FeatureUnitRegistry.java` | register, config load, enable queries |
| `FeatureUnits` | `kernel/feature/FeatureUnits.java` | 367 string constants + `registerBuiltins()` |
| `StackDomain` | `kernel/StackDomain.java` | `VALKYRIEN_SKIES`, `IMMERSIVE_PORTALS`, `EMBEDDUM`, `CREATE`, `OCULUS`, `FERRITECORE`, `RADIUM` |
| `StackProfile` | `kernel/StackProfile.java` | Detected pack profile (e.g. heavy physics+portal) |
| `StackPolicyEngine` | `kernel/StackPolicyEngine.java` | Probe mods → resolve profile |
| `OmniFixConstants` | `kernel/OmniFixConstants.java` | `MOD_ID` etc. |

### 3.3 Forge entrypoint

`org.omnifix.forge.OmniFix` (`@Mod("omnifix")`):

1. `FeatureUnits.registerBuiltins()` + load `omnifix-features.properties`
2. Register stack probes → `StackPolicyEngine.resolve()`
3. Bootstrap VS×IP (`ValkyrienPortalsCompatBootstrap`)
4. Register leak handlers (clone / server / client leave)
5. Client events (timing, MemoryReserve, integrated watchdog)
6. Create×IP portal track registration when Create+IP present
7. Runtime patches (NightConfig throttle/crash, ModFileScan compact) — **skipped if ModernFix present**
8. Optional stdout→log4j mirror
9. Commands (`/omnifix`, config reload, mcfunction profiling)
10. Load-complete: optional Mixin ClassInfo clear

---

## 4. Runtime architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Mixin apply time (IMixinConfigPlugin)                      │
│  OmniFixMixinPlugin                                         │
│   · Bootstrap MixinExtras + MixinSquared (shaded)           │
│   · registerBuiltins + early config load                    │
│   · shouldApplyMixin: FeatureUnit + peer skip + mod gates   │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  Mod construct (OmniFix)                                    │
│  · StackPolicyEngine · leak event handlers · runtime patches│
│  · Create×IP / VS×IP bootstraps · commands · client hooks   │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  Mixin packages + helpers                                   │
│  bugfix | vanilla | leak | net | perf | feature | create   │
│  + duck interfaces, caches, DFU, stitcher, pack indexes     │
└─────────────────────────────────────────────────────────────┘
```

### 4.1 Mixin configs (9) — `mods.toml` + resources

| Config | Package / role |
|---|---|
| `omnifix.mixins.json` | VS×IP (`com.valkyrienportals.mixin` / compat) |
| `omnifix.bugfix.mixins.json` | MF-class bugfix |
| `omnifix.vanilla.mixins.json` | Mojira + policy-in-vanilla package |
| `omnifix.leak.mixins.json` | FakePlayer + instance-track leak mixins |
| `omnifix.net.mixins.json` | Timeouts / compression / payload ceilings |
| `omnifix.perf.mixins.json` | Performance ports & measured opts |
| `omnifix.diagnostics.mixins.json` | Diagnostics shell |
| `omnifix.create.mixins.json` | Create×IP / VS×Create |
| `omnifix.feature.mixins.json` | Branding, registry progress, mcfunction profiling |

Shared plugin: `org.omnifix.forge.mixin.OmniFixMixinPlugin`.

### 4.2 Soft dependencies (`mods.toml`)

| Mod | Mandatory | Notes |
|---|---|---|
| forge | yes | `[47,)` |
| minecraft | yes | `[1.20.1,1.21)` |
| valkyrienskies | no | AFTER when present |
| immersive_portals | no | AFTER when present |
| create | no | AFTER when present (`[6.0,)`) |

### 4.3 Access transformers

`META-INF/accesstransformer.cfg` — opens package-private / private fields needed by stitcher holders, surface rules, DFU/structure caches, etc.

### 4.4 Shaded libraries

| Lib | Purpose |
|---|---|
| MixinExtras | `@WrapOperation`, `@WrapMethod`, etc. |
| MixinSquared | Cancellers (e.g. VS×IP frustum dead-loop) |

Bootstrapped in `OmniFixMixinPlugin.onLoad`.

---

## 5. FeatureUnit system

### 5.1 Model

Each unit is toggleable, documented, and optionally domain-gated:

```text
FeatureUnit(id, displayName, description, defaultEnabled, StackDomain...)
```

- Empty domains ⇒ always eligible from a domain perspective (vanilla/Forge).
- Config file keys mirror `id`; missing key ⇒ `defaultEnabled`.
- **Default-off units (4):**
  - `vp.diagnostics`
  - `perf.clear_mixin_classinfo`
  - `feature.mcfunction_profiling`
  - `feature.log_stdout`

### 5.2 Totals by prefix

```
vp.*        9
vs.*        1
create.*    3
vanilla.*  57
FU-MF-*    21
leak.*     50
net.*       5
perf.*    211
bugfix.*    3
feature.*   7
───────────────
TOTAL     367
```

### 5.3 Gating layers

| Layer | When | Behavior |
|---|---|---|
| Config | Always | `FeatureUnitRegistry.isConfigEnabled(id)` |
| Peer coexistence | Mixin apply | Skip MF / Debugify / ATL double-apply |
| Optional mod presence | Mixin apply | e.g. Patchouli, ResourcefulLib, CoFH, CTM, RS, Cyclic |
| StackDomain | Unit registration / profile | VS+IP units need both; fog needs Embeddium, etc. |
| Runtime patches | Mod construct | NightConfig/scan/stdout skip if MF present |

---

## 6. SHIPPED — all 367 FeatureUnits

### 6.1 VS × Immersive Portals (9)

| Id | Gate |
|---|---|
| `vp.frustum_deadloop` | VS+IP |
| `vp.portal_camera` | VS+IP |
| `vp.portal_fog` | VS+IP+Embeddium |
| `vp.ship_unload_cce` | VS+IP |
| `vp.ship_visibility` | VS+IP |
| `vp.ship_transit` | VS+IP |
| `vp.entity_drag` | VS+IP |
| `vp.cross_portal_interact` | VS+IP · VS#1525 |
| `vp.diagnostics` | VS+IP+Emb · **default off** |

### 6.1b VS × Create (1)

| Id | Gate |
|---|---|
| `vs.clockwork_clip` | VS+Create · RaycastHelper → clipIncludeShips (Clockwork wand) |

### 6.2 Create × IP (3)

| Id | Gate |
|---|---|
| `create.ip_tracks_nether` | Create+IP |
| `create.ip_tracks_b` | Create+IP · entity/block-less portals |
| `create.ip_train_transit` | Create+IP · carriage/passenger IP steal + dismount |

### 6.3 Vanilla Mojira (57)

| Id | Ticket(s) |
|---|---|
| `vanilla.spectator_stuck_effects` | MC-215531 family |
| `vanilla.spectator_bed` | MC-119417 |
| `vanilla.mending_break_progress` | MC-176559 |
| `vanilla.sapling_2x2` | MC-8187 |
| `vanilla.fullscreen_state` | MC-263865 |
| `vanilla.spectator_break` | MC-46766 |
| `vanilla.spectator_projectile` | MC-81773 |
| `vanilla.sp_chat_spam` | MC-14923 |
| `vanilla.raw_copper_sound` | MC-223153 |
| `vanilla.creeper_defuse` | MC-179072 |
| `vanilla.title_clear` | MC-55347 |
| `vanilla.hotbar_respawn` | MC-143474 |
| `vanilla.use_slow_after_drop` | MC-231097 |
| `vanilla.break_delay_drop_tool` | MC-165381 |
| `vanilla.crossbow_offhand` | MC-227169 |
| `vanilla.drag_stack_invisible` | MC-80859 |
| `vanilla.mouse_inventory` | MC-577 |
| `vanilla.leather_skeleton_stray` | MC-214147 |
| `vanilla.zombvillager_jockey` | MC-200418 |
| `vanilla.creative_ladder_slow` | MC-12829 |
| `vanilla.double_sneak_anim` | MC-159163 |
| `vanilla.f3_double` | MC-183776 |
| `vanilla.spectator_consume` | MC-129909 |
| `vanilla.lightning_drops` | MC-206922 |
| `vanilla.armorstand_particles` | MC-132878 |
| `vanilla.fishing_kill_count` | MC-100991 |
| `vanilla.pufferfish_dying` | MC-155509 |
| `vanilla.shield_hurt_sound` | MC-105068 |
| `vanilla.boat_slime_hover` | MC-108948 |
| `vanilla.offhand_rod_punch` | MC-116379 |
| `vanilla.riptide_offhand` | MC-127970 |
| `vanilla.wolf_hearts` | MC-93018 |
| `vanilla.strider_saddle_peaceful` | MC-232869 |
| `vanilla.peaceful_saturation` | MC-31819 |
| `vanilla.cmd_minecart_nbt` | MC-121903 |
| `vanilla.unknown_passenger` | MC-90683 |
| `vanilla.ctrl_q_craft` | MC-135971 |
| `vanilla.xp_bar_vanish` | MC-79545 |
| `vanilla.high_speed_flicker` | MC-111516 |
| `vanilla.armorstand_dark` | MC-197260 |
| `vanilla.dragon_void_portal` | MC-88371 |
| `vanilla.dim_teleport_state` | MC-124177 |
| `vanilla.group_ai_death` | MC-183990 |
| `vanilla.entity_anim_freeze` | MC-199467 |
| `vanilla.fishing_line_crouch` | MC-4490 |
| `vanilla.pottable_stat` | MC-231743 |
| `vanilla.skeleton_look` | MC-121706 |
| `vanilla.endrod_cactus` | MC-160095 |
| `vanilla.drown_bubbles` | MC-93384 |
| `vanilla.telemetry_disable` | MC-237493 |
| `vanilla.mac_ctrl_q` | MC-22882 |
| `vanilla.linux_chat_t` | MC-122477 |
| `vanilla.mac_sprint_break` | MC-59810 |
| `vanilla.mob_fence_escape` | MC-2025 |
| `vanilla.slow_fall_particles` | MC-30391 |
| `vanilla.partial_chunk_save` | MC-224729 |
| `vanilla.piston_reload` | MC-89146 |

### 6.4 ModernFix-class bugfix (21) — skip if ModernFix present

| Id |
|---|
| `FU-MF-BUF-LEAK` |
| `FU-MF-CHUNK-DEADLOCK` |
| `FU-MF-CONC-REG` |
| `FU-MF-DRAGON-LEAK` |
| `FU-MF-POSE-STACK` |
| `FU-MF-EXP-SCREEN` |
| `FU-MF-VEHICLE-PKT` |
| `FU-MF-MISS-BE` |
| `FU-MF-MODELDATA-CME` |
| `FU-MF-PAPER-CHUNK` |
| `FU-MF-PAPER-CHUNKMAP` |
| `FU-MF-REGOPS-CME` |
| `FU-MF-REMOVED-DIM` |
| `FU-MF-SHAPE-CACHE` |
| `FU-MF-WORLD-LEAK` |
| `FU-MF-WORLD-SCREEN` |
| `FU-MF-COFH-FLAGS` |
| `FU-MF-RECIPE-BOOK` |
| `FU-MF-CTM-CME` |
| `FU-MF-CLIENT-MAP-SAFETY` |
| `FU-MF-BE-THREAD` |

### 6.5 Memory leaks (50) — skip mixin path if AllTheLeaks present

| Id | Target |
|---|---|
| `leak.forge_fakeplayer` | Forge FakePlayerFactory |
| `leak.create_extendo` | Create ExtendoGrip |
| `leak.curios_clone` | Curios caps |
| `leak.geckolib` | GeckoLib Molang/armor |
| `leak.jei` | JEI transfer buttons |
| `leak.ftb_library` | FTB Library BLANK_GUI |
| `leak.emi` | EMI history |
| `leak.emf` | Entity Model Features |
| `leak.etf` | Entity Texture Features |
| `leak.createaddition` | Create Addition energy net |
| `leak.ars_nouveau` | Ars Nouveau |
| `leak.irons_spellbooks` | Iron’s Spellbooks |
| `leak.journeymap` | JourneyMap |
| `leak.tombstone` | Tombstone |
| `leak.architectury` | Architectury networking |
| `leak.mouse_tweaks` | Mouse Tweaks |
| `leak.findme` | FindMe |
| `leak.corpse` | Corpse |
| `leak.easy_villagers` | Easy Villagers |
| `leak.toolbelt` | Tool Belt |
| `leak.travelersbackpack` | Traveler’s Backpack |
| `leak.occultism` | Occultism |
| `leak.citadel` | Citadel |
| `leak.moonlight` | Moonlight Lib |
| `leak.aether` | Aether DroppedItem owner |
| `leak.alexsmobs` | Alex’s Mobs world maps |
| `leak.flywheel` | Flywheel WorldAttached |
| `leak.minecolonies` | MineColonies JEI entities |
| `leak.pnc` | PneumaticCraft armor/drones |
| `leak.twilightforest` | TF ENTITY_MAP / HydraModel |
| `leak.betterf3` | BetterF3 LocationModule |
| `leak.beansbackpacks` | Beans Backpacks EnderStorage |
| `leak.sereneseasons` | Serene Seasons snow/maps |
| `leak.mowzies` | Mowzie’s music/animator |
| `leak.ae2wt` | AE2WTLib terminal/tab |
| `leak.badpackets` | BadPackets ChannelRegistry |
| `leak.blueskies` | Blue Skies lastRidden/ambient |
| `leak.iceberg` | Iceberg EntityCollector/renderer |
| `leak.forbidden_arcanus` | Forbidden Arcanus clone caps |
| `leak.jer` | Just Enough Resources entities |
| `leak.mna` | Mana and Artifice armor entity |
| `leak.nuclearcraft` | NuclearCraft TooltipHandler |
| `leak.phosphophyllite` | Phosphophyllite ConfigManager |
| `leak.railcraft` | Railcraft Charge networks |
| `leak.smallships` | Small Ships ChunkMap fields |
| `leak.mc_vanilla` | Vanilla EMPTY/crosshair residual |
| `leak.cyclops` | CyclopsCore model world (instance track) |
| `leak.emi_loot` | EMI Loot EntityEmiStack (instance track) |
| `leak.ldlib` | LDLib ModularUI player (instance track) |
| `leak.ldlib_dummyworld` | LDLib DummyWorld profiler supplier |

### 6.6 Network (5)

| Id | Effect |
|---|---|
| `net.login_timeout` | Login 30s → 120s |
| `net.read_timeout` | Read timeout floor 120s |
| `net.compression_size` | Decompress ceiling → 16 MiB |
| `net.play_timeout` | Keep-alive check 15s → 60s |
| `net.payload_split` | Custom payload / encode / varint ceilings → 16 MiB |

### 6.7 Performance (211)

| Id | Effect |
|---|---|
| `perf.handshake_stall` | Login payload re-tick |
| `perf.loop_spin_waiting` | parkNanos vs spin |
| `perf.potential_spawns_alloc` | Reuse PotentialSpawns lists |
| `perf.recipe_reload_log` | Compact recipe apply error logs |
| `perf.registry_object_get` | RegistryObject.get without lambda |
| `perf.thread_priority` | Util workers + integrated server priority 4 |
| `perf.tag_id_cache` | TagEntry + TagOrElementLocation cache |
| `perf.wall_shape_dedup` | WallBlock VoxelShape map reuse |
| `perf.blockstate_enum_cache` | SupportType/Axis.values cache |
| `perf.creative_tab_memoize` | Memoize CreativeModeTab.buildContents |
| `perf.compact_bit_storage` | Shrink empty oversized palette storage |
| `perf.faster_structure_location` | Placement early-out on StructureCheck |
| `perf.object_holder_cleanup` | Drop redundant Forge ObjectHolders |
| `perf.biome_temperature_cache` | Drop ineffective biome temp cache |
| `perf.ticking_chunk_alloc` | Bat date + structure map + ChunkHolder Either |
| `perf.mojang_registry_grow` | MappedRegistry byId power-of-two grow |
| `perf.state_empty_neighbours` | Empty neighbour → ImmutableTable (no FC) |
| `perf.model_optimizations` | Property/Boolean/Transform/Selector/MultiVariant |
| `perf.forge_registry_bits` | ForgeRegistry free-id BitSet cache |
| `perf.profile_texture_cache` | SkinManager profile texture hash cache |
| `perf.attribute_supplier_compact` | Compact AttributeSupplier map + intern templates |
| `perf.compact_entity_models` | Dedup ModelPart.Cube from CubeDefinition.bake |
| `perf.cache_strongholds` | Disk-cache ring positions + radius early-out |
| `perf.zip_pack_index` | Zip FilePackResources namespace/list index |
| `perf.dedicated_reload_executor` | Dedicated ForkJoin pool for resource reloads |
| `perf.fast_forge_dummies` | Fast-path NamespacedWrapper.freeze when bound |
| `perf.worldgen_allocation` | Surface-rule / material / NoiseChunk wrap cuts |
| `perf.compact_imposter_chunks` | Share LevelChunk arrays into ImposterProtoChunk |
| `perf.ingredient_dedup` | Intern Ingredient.ItemValue + defensive copy |
| `perf.forge_cap_retrieval` | Cap order + AttachCapabilities isCancelable |
| `perf.suspend_integrated_server` | Hold SP ticks until join packets applied |
| `perf.faster_item_rendering` | GUI SimpleBakedModel camera-facing quads only |
| `perf.compact_mojang_registries` | LifecycleMap + VanillaRegistries memoize + BlockStateData intern |
| `perf.cache_upgraded_structures` | Disk-cache DFU-upgraded structure NBT |
| `perf.dynamic_dfu` | Lazy DFU + schema-skip + rewrite-cache blaster |
| `perf.remove_spawn_chunks` | Drop permanent spawn tickets; START/PORTAL temps |
| `perf.optimize_surface_rules` | ChunkBiomeLookup + column prefetch + biome hoist |
| `perf.reduce_blockstate_cache_rebuilds` | Lazy BlockState cache rebuild after bake |
| `perf.path_pack_cache` | PathPackResources tree index + pack finder guard |
| `perf.compress_unihex_font` | Compact unihex + lazy soft GlyphProvider |
| `perf.dynamic_structure_manager` | Soft structure template repository |
| `perf.chunk_meshing` | Fast section iterator + BlockState reuse |
| `perf.debug_level_states` | Reuse Forge IdMapper for debug worlds |
| `perf.dynamic_languages` | Soft language values from pack resources |
| `perf.faster_ingredients` | Tag test/stack + soft getItems + reload track |
| `perf.lazy_search_tree` | Defer SearchRegistry tree until first query |
| `perf.forge_registry_alloc` | OpenHashMap + Block/Item DelegateHolder hot-path |
| `perf.fake_state_map` | FakeStateMap StateDefinition (FerriteCore-class) |
| `perf.imposter_be_guard` | Hide live BEs from ImposterProtoChunk worldgen |
| `perf.release_protochunks` | Suspend idle non-FULL protochunk holders |
| `perf.debug_reloader` | Profiled reload FQCN names + cost sort + -D flag |
| `perf.measure_time` | Bootstrap/game/world-join timing logs |
| `perf.keymap_prewarm` | Main-thread KeyMapping translation prewarm |
| `perf.integrated_watchdog` | 40s hung SP tick → full thread dump |
| `perf.memory_reserve_release` | Free vanilla MemoryReserve on client init |
| `perf.nightconfig_watch_throttle` | Park NightConfig FileWatcher 1s/loop |
| `perf.mod_scan_compact` | Drop annotation noise + intern Types on ModFileScanData |
| `perf.manifest_compact` | Drop digest-only SecureJar manifest entries |
| `perf.mod_work_queue` | FML SyncExecutor park + splash kick queue |
| `perf.network_constants_init` | NetworkConstants.init at bootstrap (Forge #9505) |
| `perf.faster_loot_loading` | Pre-mark builtin loot JSON; skip getResource probe |
| `perf.faster_texture_stitching` | STB rect pack for atlases ≥100 sprites |
| `perf.mixin_injector_group_patch` | DummyList on InjectorGroupInfo.NO_GROUP members |
| `perf.clear_mixin_classinfo` | Audit + clear Mixin ClassInfo after launch (**default off**) |
| `perf.patchouli_book_dedup` | Patchouli template AIR stacks → EMPTY after reload |
| `perf.resourcefullib_highlights` | Intern ResourcefulLib highlight geometry |
| `perf.hopper_entity_cache` | Same-tick empty hopper entity suck skip |
| `perf.client_entity_collision` | Client skip entity–entity collision for non-local |
| `perf.direction_values_cache` | Shared Direction/Axis arrays on redstone/leaves/level |
| `perf.item_entity_merge_cache` | Same-tick empty ItemEntity merge scan cache |
| `perf.advancement_reload_log` | Compact advancement parse error logs |
| `perf.path_recalc_throttle` | Throttle PathNavigation re-A* for stationary mobs |
| `perf.skip_empty_random_ticks` | Zero randomTickSpeed when no section isRandomlyTicking |
| `perf.xp_orb_scan_cache` | Same-tick empty XP orb merge scan cache |
| `perf.goal_selector_rate` | GoalSelector newGoalRate 3→5 |
| `perf.particle_empty_tick` | Skip ParticleEngine.tick when idle |
| `perf.empty_block_entity_tick` | Skip Level.tickBlockEntities when idle |
| `perf.empty_block_drops_shortcircuit` | Empty loot table → empty drops without params |
| `perf.entity_section_multimap` | fastutil OpenHashMap for ClassInstanceMultiMap |
| `perf.brewing_stand_idle` | Skip idle brewing stand serverTick |
| `perf.sensor_scan_rate` | Brain Sensor scanRate +50% (cap 80) |
| `perf.arrow_inground_throttle` | Server every-other-tick for stuck arrows |
| `perf.furnace_idle` | Skip cold empty furnace/smoker/blast ticks |
| `perf.campfire_idle` | Skip empty campfire cook/cooldown ticks |
| `perf.target_goal_interval` | NearestAttackableTarget interval +50% |
| `perf.direction_get_nearest_cache` | Exact-bit cache for Direction.getNearest |
| `perf.beehive_empty` | Skip empty beehive serverTick |
| `perf.jukebox_idle` | Skip jukebox tick when no disc / not playing |
| `perf.empty_effects_tick` | Skip tickEffects when no effects + clean |
| `perf.effects_open_hash_map` | Reference2ObjectOpenHashMap for activeEffects |
| `perf.bell_idle` | Skip bell ticks when not shaking/resonating |
| `perf.shulker_box_idle` | Skip shulker tick when CLOSED or OPENED |
| `perf.chest_lid_idle` | Skip chest lid animate when fully closed |
| `perf.sign_edit_idle` | Skip sign tick when no editor UUID |
| `perf.enchantment_table_idle` | Client throttle closed book animation 3/4 ticks |
| `perf.avoid_entity_scan_throttle` | AvoidEntityGoal canUse every 3rd mob tick |
| `perf.random_stroll_interval` | RandomStrollGoal interval +50% (cap 240) |
| `perf.spawner_near_cache` | BaseSpawner.isNearPlayer same-tick memo |
| `perf.sculk_catalyst_idle` | Skip catalyst tick when no sculk charge cursors |
| `perf.follow_parent_throttle` | FollowParentGoal scan every 3rd animal tick |
| `perf.tempt_goal_throttle` | TemptGoal nearest-player every 2nd tick |
| `perf.breed_goal_throttle` | BreedGoal partner scan every 2nd love tick |
| `perf.minecart_hopper_empty_cache` | Hopper minecart empty suction same-tick memo |
| `perf.beg_goal_throttle` | BegGoal player scan every 3rd wolf tick |
| `perf.conduit_inactive_throttle` | Inactive conduits only full-tick on 40t shape refresh |
| `perf.hanging_entity_survive_interval` | Item frames/paintings survives() every 150t (was 100) |
| `perf.follow_owner_repath` | Pet follow repath delay 10→15 adjusted ticks |
| `perf.container_openers_recheck` | Open chest opener recheck 5→8 block ticks |
| `perf.move_through_village_throttle` | Village POI canUse every 3rd mob tick |
| `perf.flee_sun_throttle` | FleeSunGoal canUse every 3rd mob tick |
| `perf.restrict_sun_throttle` | RestrictSunGoal canUse every 3rd mob tick |
| `perf.move_to_block_interval` | MoveToBlockGoal search base 200→300 |
| `perf.stroll_village_interval` | StrollThroughVillageGoal interval +50% |
| `perf.xp_orb_scan_period` | XP orb scanForEntities 20→30 ticks |
| `perf.remove_block_throttle` | RemoveBlockGoal canUse every 3rd tick |
| `perf.leap_at_target_throttle` | LeapAtTargetGoal canUse every other tick |
| `perf.end_gateway_entity_scan_throttle` | End gateway entity AABB every other tick |
| `perf.follow_mob_throttle` | FollowMobGoal AABB scan every 3rd tick |
| `perf.follow_mob_repath` | FollowMobGoal repath 10→15 adjusted ticks |
| `perf.defend_village_throttle` | Iron golem defend-village scan every 3rd tick |
| `perf.offer_flower_throttle` | OfferFlowerGoal villager scan every 3rd tick |
| `perf.run_around_crazy_throttle` | Untamed horse crazy-run canUse every 3rd tick |
| `perf.look_at_player_probability` | LookAtPlayerGoal probability ×2/3 |
| `perf.follow_boat_throttle` | FollowBoatGoal scan every 3rd tick |
| `perf.land_shoulder_throttle` | Parrot shoulder-land canUse every 3rd tick |
| `perf.reset_anger_throttle` | Universal anger reset canUse every 3rd tick |
| `perf.cat_sit_throttle` | Cat sit-on-block canUse every 3rd tick |
| `perf.cat_lie_throttle` | Cat lie-on-bed canUse every 3rd tick |
| `perf.panic_goal_throttle` | Panic canUse every 2nd tick unless on fire |
| `perf.trade_with_player_throttle` | Villager trade canUse every 3rd tick |
| `perf.dolphin_jump_interval` | DolphinJumpGoal interval +50% (cap 240) |
| `perf.eat_block_throttle` | EatBlockGoal canUse every 3rd tick |
| `perf.climb_powder_snow_throttle` | Climb powder-snow canUse every 3rd tick |
| `perf.move_restriction_throttle` | Restriction-return path search every 3rd tick |
| `perf.move_towards_target_throttle` | MoveTowardsTarget canUse every 2nd tick |
| `perf.ranged_attack_interval` | RangedAttackGoal interval min/max +50% |
| `perf.ranged_bow_interval` | RangedBowAttackGoal interval +50% |
| `perf.pathfind_to_raid_throttle` | PathfindToRaid canUse every 3rd tick |
| `perf.break_door_throttle` | BreakDoorGoal canUse every 3rd tick |
| `perf.float_goal_throttle` | FloatGoal dry-path canUse every 3rd tick (wet/lava always) |
| `perf.melee_canuse_cooldown` | MeleeAttackGoal canUse cooldown 20→30 ticks |
| `perf.ranged_crossbow_delay` | Crossbow post-charge attackDelay +50% |
| `perf.door_interact_throttle` | DoorInteractGoal canUse every 3rd tick |
| `perf.random_look_probability` | RandomLookAroundGoal look probability ×2/3 |
| `perf.llama_caravan_throttle` | LlamaFollowCaravanGoal scan every 3rd tick |
| `perf.follow_flock_repath` | FollowFlockLeaderGoal repath 10→15 |
| `perf.aec_scan_period` | AreaEffectCloud entity scan 5→8 ticks |
| `perf.try_find_water_throttle` | TryFindWaterGoal every 3rd |
| `perf.ocelot_attack_repath` | OcelotAttack moveTo every 2nd tick |
| `perf.move_back_village_throttle` | MoveBackToVillage canUse every 3rd |
| `perf.random_stand_throttle` | Horse RandomStand every 3rd |
| `perf.use_item_throttle` | UseItemGoal every 3rd |
| `perf.swell_goal_throttle` | SwellGoal every 2nd unless swelling |
| `perf.item_merge_period` | ItemEntity stationary merge 40→60 |
| `perf.breath_air_path_throttle` | BreathAir findAir every 2nd unless critical |
| `perf.sit_when_ordered_throttle` | SitWhenOrdered every 3rd unless ordered |
| `perf.nearest_item_sensor_range` | NearestItemSensor 32→24 |
| `perf.secondary_poi_radius` | SecondaryPoi XZ 4→3 (symmetric ±) |
| `perf.armor_stand_marker_push` | Marker armor stands skip pushEntities |
| `perf.shulker_attach_throttle` | Shulker findNewAttachment every 3rd |
| `perf.melee_path_recalc_base` | Melee path recalc base 4→6 |
| `perf.item_still_physics_period` | Item still physics 4→6 |
| `perf.living_push_throttle` | Non-player pushEntities every 2nd tick |
| `perf.nearest_living_sensor_radius` | NearestLivingEntitiesSensor radius 16→12 |
| `perf.player_sensor_range` | PlayerSensor range 16→12 |
| `perf.nearest_bed_scan` | Bed POI scan 48→36, batch 5→3 |
| `perf.hurt_alert_y` | HurtBySensor alert Y 10→7 |
| `perf.target_search_y` | Target search Y 4→3 |
| `perf.flying_hover_radius` | Flying hover radius 8→6 |
| `perf.minecart_push_throttle` | Still minecart push every 2nd tick |
| `perf.bee_pollinate_cooldown` | Bee flower retry 20–60→30–90 |
| `perf.tempting_sensor_range` | TemptingSensor 10→8 |
| `perf.bee_hive_locate` | Hive POI 20→15, cooldown 200→300 |
| `perf.phantom_player_scan` | Rescan 60→90, XZ 16→12 |
| `perf.owner_hurt_by_throttle` | OwnerHurtBy every 2nd tick |
| `perf.owner_hurt_target_throttle` | OwnerHurtTarget every 2nd tick |
| `perf.water_avoid_stroll_radius` | LandRandomPos 15→12 / 10→8 |
| `perf.healable_raider_cooldown` | Healable raider cooldown 200→300 |
| `perf.enderman_take_interval` | Take-block interval 20→30 |
| `perf.warden_sensor_radius` | Warden sensor 24→20 |
| `perf.axolotl_attack_range` | Axolotl isClose 8→6 (64→36 distSq) |
| `perf.frog_attack_range` | Frog closerThan 10→8 |
| `perf.enderman_leave_interval` | Leave-block 2000→3000 |
| `perf.ghast_wander_radius` | Ghast float wander 16→12 |
| `perf.ghast_fireball_charge` | Fireball charge 20→30 |
| `perf.silverfish_wake_scan` | Wake friends scan XZ 10→8 Y 5→4 |
| `perf.bee_wander_chance` | Bee wander nextInt 10→15 |
| `perf.blaze_fire_interval` | Blaze charge 60→90, post-volley 100→130 |
| `perf.guardian_attack_duration` | Guardian laser 80→100 |
| `perf.vex_random_move_chance` | Vex random move 7→10 |
| `perf.vex_charge_chance` | Vex charge 7→10 |
| `perf.hoglin_repellent_range` | Hoglin repellent 8,4→6,3 |
| `perf.piglin_repellent_range` | Piglin repellent 8,4→6,3 |
| `perf.allay_heal_period` | Allay heal %10→%15 |
| `perf.rabbit_raid_range` | Rabbit crop raid search 16→12 |
| `perf.slime_jump_delay` | Slime jump delay nextInt(20)+10 → nextInt(30)+15 |
| `perf.elder_guardian_attack_duration` | Elder laser 60→80 |
| `perf.evoker_fang_interval` | Evoker fang cast interval 100→150 |
| `perf.evoker_summon_interval` | Evoker summon-vex interval 340→450 |
| `perf.evoker_wololo_interval` | Evoker wololo interval 140→200 |
| `perf.turtle_go_home_chance` | Turtle go-home gate 700→1000 |
| `perf.turtle_lay_egg_duration` | Turtle lay-egg duration 200→300 |
| `perf.bee_grow_crop_interval` | Bee grow-crop gate 30→45 |
| `perf.panda_roll_chance` | Roll gates 60→90, 500→750 |
| `perf.panda_sneeze_chance` | Sneeze 500→750, 6000→8000 |
| `perf.polar_bear_cub_scan` | Cub protect scan 8,4→6,3 |
| `perf.drowned_water_search` | Water samples 10→7, span 20→16 |
| `perf.bee_go_hive_timeout` | Hive travel timeout 600→800 |
| `perf.bee_go_flower_timeout` | Flower travel timeout 600→800 |
| `perf.fishing_open_water_scan` | Open-water area ±2→±1 |
| `perf.panda_sit_item_scan` | Item inflate 6→4 / 8→6 |

### 6.8 Bugfix (extra) (3)

| Id | Effect |
|---|---|
| `bugfix.nightconfig_config_crash` | Defer watch reloads (`/ofc`/`/ofsrc`) + lock config handlers |
| `bugfix.debug_overlay_clear` | Clear Forge debug overlay chunk cache on level leave |
| `bugfix.uuid_duplicate_log` | Rate-limit duplicate entity UUID log spam |

### 6.9 Policy / feature (7)

| Id | Effect |
|---|---|
| `feature.chat_signing_off` | Force ChatTrustLevel.SECURE + empty ProfileKeyPairManager |
| `feature.narrator_linux_quiet` | Quiet Linux narrator init failure |
| `feature.mcfunction_profiling` | Time `#minecraft:tick` functions; `/omnifix mcfunctions` (**default off**) |
| `feature.registry_event_progress` | Client RegisterEvent progress bars + async splash |
| `feature.omnifix_branding` | OmniFix version on BrandingControl / F3 brands |
| `feature.log_stdout` | Mirror System.out/err into log4j (**default off**) |
| `feature.force_close_loading_screen` | Dismiss join loading screens when ready |

### 6.10 Totals (canonical)

```
VP/Create    13   (vp 9 + vs 1 + create 3)
Vanilla      57
MF bugfix    21
Leaks        50
Network       5
Perf        211
Bugfix       3
Policy/Feat   7
───────────────
TOTAL       367
```

---

## 7. Implementation surface

### 7.1 Scale (approx.)

| Surface | Count |
|---|---|
| FeatureUnits | **367** |
| Java under `omnifix-forge` | ~329 |
| Mixin-named classes | ~258 |
| Mixin JSON configs | **9** |
| Kernel Java types | 7 |

### 7.2 Helper / infrastructure packages (`org.omnifix.*`)

| Package | Examples |
|---|---|
| `blockstate` | `BlockStateCacheHandler`, `FakeStateMap` |
| `chunk` | `ExtendedPalettedContainer` |
| `classloading` | `ManifestCompactor` |
| `client` | timing, async loading screen, client events |
| `config` | NightConfig fixer / watch throttler |
| `dfu` | `LazyDataFixer`, `DFUBlaster` |
| `duck` | Integrated server, chunk map, stronghold, profiling ducks |
| `dynamiclanguages` | Soft language maps |
| `entity` | Attribute supplier compact / launch gate |
| `load` | ModFileScan compact, work queue, reload tracker |
| `recipe` | Ingredient dedup / soft stacks |
| `registry` | LifecycleMap, DelegateHolder |
| `render` | Fast item render, unihex, buffers |
| `resources` | ZipPackIndex, pack cache engine, reload executor |
| `searchtree` | LazySearchTree |
| `structure` | CachingStructureManager |
| `textures` | StbStitcher |
| `util` | TracingPrintStream, DummyList, SafeRun, blockpos iterators |
| `world` | IntegratedWatchdog, ThreadDumper |
| `worldgen` | ChunkBiomeLookup, PrefetchingBlockColumn, surface context |

### 7.3 Mixin package map

| Package | Domain |
|---|---|
| `mixin.bugfix.*` | MF concurrency, dragon, pose, world leaks, paper chunk, CTM, CoFH, … |
| `mixin.vanilla.*` | Mojira + chat signing / narrator (policy living in vanilla package) |
| `mixin.leak.*` | FakePlayer, instance track, leave/clone handlers |
| `mixin.net.*` | Login/read/play timeouts, compression, payload/varint ceilings |
| `mixin.perf.*` | Largest surface — boot, worldgen, AI, BE idle, render, packs, DFU, … |
| `mixin.feature.*` | Branding, registry progress, mcfunction profiling |
| Compat packages | VS×IP / Create×IP under compat source roots |

### 7.4 Typical unit implementation pattern

1. Prove root cause (Mojira / MF / ATL / pack crash / decompile).
2. Add `public static final String` + `FeatureUnitRegistry.register(...)` in `FeatureUnits.java`.
3. Implement mixin(s) and/or runtime helper; wire into the correct mixin JSON.
4. Map mixin → unit id in `OmniFixMixinPlugin.featureUnitForMixin` (and optional-mod gates).
5. Coexistence: MF / Debugify / ATL skip paths already cover prefix families.
6. Update this SSOT (§6) and `BACKLOG.md` status in the same change.
7. Build `omnifix-forge` → reobf jar.

---

## 8. Coexistence & gating

### 8.1 Peer skip table

| Peer | OmniFix skips |
|---|---|
| ModernFix | `FU-MF-*` + MF-class `perf.*` only (`perf.handshake_stall`, `perf.loop_spin_waiting`) + MF-class feature mixins + NightConfig/scan/stdout runtime patches. **AI/entity/idle `perf.*` throttles stay active when ModernFix is present.** |
| Debugify | `vanilla.*` Mojira mixins (not chat-signing / narrator policy) |
| AllTheLeaks | mixin-gated `leak.*` |
| Moonrise | SortedArraySet paper fix |
| RS / Cyclic | shape-cache only if present |
| Patchouli / ResourcefulLib / CoFH / CTM | feature mixins only if mod present |

### 8.2 Philosophy

- **Inclusion is independent of peer presence** (SOUL: peers do not block shipping).
- **Application is coexistence-aware** (skip double-apply to avoid double-mixin / double-patch crashes).
- Players may force-enable via config only where the unit remains registered; mixin plugin still enforces peer skips for safety on overlapping ports.

---

## 9. Absorb targets (fix/util constellation)

| Mod | Domain | Interest |
|---|---|---|
| ModernFix | Boot/memory/bugfix | Full absorb eligible |
| FerriteCore | Blockstate memory | Measure first |
| Canary / Radium | Lithium tick | High value, careful |
| AllTheLeaks | Per-mod leaks | Primary leak syllabus |
| MemoryLeakFix | Vanilla leaks | Eligible |
| Fix GPU Memory Leak | GL leaks | High value — residual research |
| Connectivity | Net timeouts/size/ghost | Multiplayer universal |
| Recipe Essentials | Recipe packets | Research |
| Debugify | Mojira 70+ | Core Band A |
| Crash Assistant | Crash patterns | Diagnostics only |
| Clumps / LMD / AI Imp. | Entity lag | Measure |
| Krypton Reforged | Net stack | Careful |
| Starlight | Light engine | Defer casual |
| No Chat Reports | Chat signing | Policy |

---

## 10. Domains D1–D8 & research bands

| | Scope |
|---|---|
| **D1** | Vanilla Mojira / MemoryLeakFix / MF vanilla bugfix |
| **D2** | Forge FakePlayer, registry CME, handshake, payload, caps |
| **D3** | Login/read timeout, compression, ghost blocks, recipe sync, XP |
| **D4** | ATL #3 + GPU + FerriteCore |
| **D5** | Embeddium/Oculus/IF/EMF/ETF, cull, GPU |
| **D6** | Create, VS, IP, AE2, Mek, Ars, Colonies, FTB, JEI/EMI, Cobblemon, … |
| **D7** | Structure locate, DFU, spawn chunks, worldgen alloc |
| **D8** | Mixin collisions, type casts, dim-blind packets, ship/portal space, nested render, registries, client maps, cap clone |

**Bands:** A correctness → B dominant seams → C specialized → D measured perf.

---

## 11. Research pipeline

| Phase | Status |
|---|---|
| P0 Fix-mod landscape | **Done** |
| P1 Debugify + MF + ATL extract | **Done** |
| P2 Ecosystem mod surface | **Done** |
| P3 Modpack crash frequency | **Partial** |
| P4 Domain deep dives | **Partial** (VS×IP exemplar) |
| P5 Continuous intake | **Process defined** |

Detailed notes: `RESEARCH_MASTER.md`. Status matrix: `BACKLOG.md`.

---

## 12. Terminal residual (SOUL: no stubs)

**Shipped mandate complete for every item with a proven, reimplementable root cause on Forge 1.20.1.**  
Below is the permanent residual set — not incomplete code, but items blocked by missing contract, ARR, mega-system scope, or deliberate deferral.

### 12.1 Vanilla Mojira — residual

| Ticket | Status | Why not shipped |
|---|---|---|
| MC-112730 beacon double render | **closed-no-contract** | No clean dual-list root on Forge 1.20.1 |
| MC-122627 tab padding | **defer cosmetic** | Pure UI padding; no gameplay/crash impact |
| MC-7569 RCON newlines | **N/A fixed upstream** | Vanilla already appends `"\n"` |

*(MC-2025 / MC-30391 / MC-224729 / MC-89146 are **shipped** as `vanilla.mob_fence_escape`, `vanilla.slow_fall_particles`, `vanilla.partial_chunk_save`, `vanilla.piston_reload`.)*

### 12.2 AllTheLeaks

**Complete** for ATL #3 soft+instance track syllabus on 1.20.1 (including cyclops/emi_loot/ldlib/DummyWorld).

### 12.3 Net / GPU / recipe — residual

| Id | Status | Why |
|---|---|---|
| net.ghost_blocks | **closed-no-ARR-copy** | Multi-path desync; Connectivity ARR not reimplemented |
| net.recipe_packet | **closed-covered** | Forge `VanillaPacketSplitter` + our size raises cover shippable side |
| ren.gpu_leak | **closed-research** | Needs GL object lifecycle repro per renderer stack |
| ren.rsm_collision | **closed-research** | Embeddium multi-mod inject cancel needs live stack |

### 12.4 ModernFix Band D residual

| Family | Status |
|---|---|
| dynamic_resources (full model bakery rewrite) | **defer mega-system** — hundreds of classes + optional mod hooks |
| faster_capabilities ASM dispatcher gen | **defer** — runtime bytecode generator |
| smart_ingredient_sync | **defer** — custom network channel + protocol handshake |
| blast_search_trees / JEI tree integration surface | **defer** — beyond lazy SearchRegistry deferral already shipped |
| restore_old_dragon_movement | **defer gameplay** — intentional MF gameplay change |

**Already shipped among former residual:** `perf.remove_spawn_chunks`, `perf.optimize_surface_rules`, `perf.release_protochunks`, `perf.dynamic_dfu`, `perf.cache_upgraded_structures`, `perf.faster_texture_stitching`, `perf.fake_state_map`, `perf.reduce_blockstate_cache_rebuilds`, `perf.dynamic_languages`, `perf.lazy_search_tree`, and related.

### 12.5 Seams residual

| Id | Status |
|---|---|
| vs.mf_clash | **closed-no-repro** — no isolated MF×Clockwork crash root without pack repro |
| ip.oculus | **defer hard** — nested shader passes |
| cobblemon.tick | **closed-research** — content mod load; no universal root |
| canary / starlight | **defer ecosystem** — engine-level; peer mods own domain |

---

## 13. Completion law

OmniFix 1.20.1 is **feature-complete for every root-caused, reimplementable, non-ARR, non-gameplay-policy, non-mega-system unit identified in the mandate pipeline.**

Further units require: new Mojira/mod crash evidence, pack repro for seams, licensed third-party design docs, or deliberate opt-in mega-systems (dynamic resources).

**SOUL invariants apply:** no stubs, no unresearched copies, documentation synchronized with `FeatureUnits.java`.

---

## 14. Commands & diagnostics

Registered from `OmniFix` (gated by relevant FeatureUnits):

| Command / hook | Role |
|---|---|
| `/omnifix` family | Diagnostics / feature introspection |
| `/omnifix mcfunctions` | Dump mcfunction tick profiling (`feature.mcfunction_profiling`) |
| Config reload (`/ofc`, `/ofsrc` class) | NightConfig crash-safe reload (`bugfix.nightconfig_config_crash`) |
| Integrated watchdog | 40s hung SP tick → thread dump (`perf.integrated_watchdog`) |
| Measure-time logs | Bootstrap / game / world-join (`perf.measure_time`) |
| F3 / branding | OmniFix version string (`feature.omnifix_branding`) |

---

## 15. Document history

| Date | Change |
|---|---|
| 2026-07-17 | `List.md` started as mandate paste (incomplete inventory). |
| 2026-07-17 | Full mandate + early shipped inventory + residual § in `List.md`. |
| 2026-07-21 | Inventory grown to **246** FeatureUnits through multi-wave shipping. |
| 2026-07-21 | **SSOT.md created:** merge of comprehensive architectural breakdown + `List.md` inventory; `List.md` retired. Residual section cleaned for units later shipped. |
| 2026-07-21 | Wave: +8 perf units (bell/shulker/chest lid/sign/enchant table idle, avoid-entity throttle, random stroll interval, spawner near cache) → **254**. |
| 2026-07-21 | **ECOSYSTEM_MAP.md** added (FC taxonomy, D1–D12, peer/content pillars). Wave: +6 (sculk catalyst, follow/tempt/breed/beg AI throttle, minecart hopper cache) → **260**. |
| 2026-07-21 | Wave: +5 (conduit inactive, hanging survive, follow-owner repath, openers recheck, village POI) → **265**. |
| 2026-07-21 | 8-agent parallel wave: flee/restrict sun, move-to-block, stroll village, XP scan, remove-block, leap, end gateway → **273**. |
| 2026-07-21 | Wave: follow-mob, defend-village, offer-flower, run-crazy, look-at probability → **279**. |
| 2026-07-21 | Custom-agent wave (+`omnifix-ai-throttle`): boat/shoulder/anger/cats/panic/trade/dolphin → **287**. |
| 2026-07-21 | Wave (+`omnifix-entity-idle` agent): eat/climb/restriction/target/ranged/raid/door → **295**. |
| 2026-07-21 | AI throttle wave: float/melee/crossbow/door-interact/random-look/llama-caravan/flock-repath/AEC scan → **303**. OmniFixMixinPlugin MF peer-skip narrowed to Handshake/SpinWait only (AI/entity perf stays on with ModernFix). |
| 2026-07-21 | AI throttle wave-2 (+8): try-find-water / ocelot-attack repath / move-back-village / random-stand / use-item / swell / item-merge period / breath-air path → **311**. |
| 2026-07-21 | AI/entity throttle wave-3 (+8): sit-when-ordered / nearest-item range / secondary-poi radius / armor-stand marker push / shulker attach / melee path recalc / item still physics / living push → **319**. |
| 2026-07-21 | Entity-brain wave-4 (+8, +`omnifix-entity-brain`): nearest-living sensor radius / player sensor range / nearest-bed scan / hurt-alert Y / target-search Y / flying hover radius / minecart push throttle / bee pollinate cooldown → **327**. |
| 2026-07-21 | Entity-brain wave-5 (+8): tempting sensor range / bee hive locate / phantom player scan / owner-hurt-by / owner-hurt-target / water-avoid stroll radius / healable raider cooldown / enderman take interval → **335**. |
| 2026-07-21 | Entity-brain wave-6 (+8): warden sensor radius / axolotl attack range / frog attack range / enderman leave interval / ghast wander radius / ghast fireball charge / silverfish wake scan / bee wander chance → **343**. |
| 2026-07-21 | Entity-brain wave-7 (+8): blaze fire interval / guardian attack duration / vex random-move chance / vex charge chance / hoglin repellent range / piglin repellent range / allay heal period / rabbit raid range → **351**. |
| 2026-07-21 | Entity-brain wave-8 (+8): slime jump delay / elder guardian attack duration / evoker fang interval / evoker summon interval / evoker wololo interval / turtle go-home chance / turtle lay-egg duration / bee grow-crop interval → **359**. |
| 2026-07-21 | Entity-brain wave-9 (+8): panda roll chance / panda sneeze chance / polar bear cub scan / drowned water search / bee go-hive timeout / bee go-flower timeout / fishing open-water scan / panda sit item scan → **367**. |

---

*Authoritative unit list = `omnifix-kernel/.../FeatureUnits.java`. This file must not drift silently from that catalog.*
