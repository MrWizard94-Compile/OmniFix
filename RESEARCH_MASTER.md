# OmniFix Research Master — Forge 1.20.1

**Status:** Multi-pass research complete (P0–P5). Implementation continues under SOUL; living inventory in `SSOT.md` (**367** FeatureUnits as of 2026-07-21).  
**Atlases:** `CORPUS.md` (entire Minecraft + modding ecosystem problem space, crawl v1) · `ECOSYSTEM_MAP.md` (OmniFix FC/D projection on Forge 1.20.1).  
Wave-6 hard slices (GPU lifecycle, Oculus×IP, mega dynamic_resources) remain backlog-only.  
**Survey date:** 2026-07-17 · **Living update:** 2026-07-21  
**Platform:** Minecraft 1.20.1 · Forge 47.x · Java 17  
**Mandate:** OmniFix maps and absorbs the **entire** Forge 1.20.1 fix/performance problem space — not pack-local patches. Peer mods are catalogs. SOUL completeness is law. Full-game corpus is broader than OmniFix ship scope.

**Quality bar (SOUL.md):** symptom → root cause → gated complete FeatureUnit → verify. No stubs.

---

## How to read this document

| Field | Meaning |
|---|---|
| **FU-id** | FeatureUnit identifier for implementation tracking |
| **Band** | A = universal correctness · B = dominant-stack seams · C = specialized platforms · D = measured perf |
| **Gate** | Always / domain probe (mod ids) |
| **Source** | Where the issue was cataloged |
| **Confidence** | High = root cause known or shipped elsewhere with clear patch · Med = strong symptom + likely cause · Low = needs repro |
| **Status** | `shipped` · `pending_verify` · `backlog` · `watch` · `defer` |

**License note before porting:** re-research the exact license of any third-party mixins (MIT/LGPL/ARR). Prefer independent root-cause reimplementation over copy-paste of ARR code (Connectivity, many someaddon mods).

---

# P0 — Forge 1.20.1 fix-mod landscape

Existing mods that define the **issue surface** OmniFix may absorb. They are research catalogs, not ownership fences.

| Mod | License (typical) | Domain | Absorb priority |
|---|---|---|---|
| **ModernFix** | LGPL | Boot, memory, bugfix, perf, safety | **P0 catalog** (local tree in repo) |
| **FerriteCore** | MIT | Blockstate/model memory | Band D after measure |
| **Canary** | LGPL | Lithium-class server tick | Band D; preserve behavior |
| **Radium Reforged** | LGPL | Lithium-class (alt) | Same; VS wiki marks incompatible in some stacks |
| **AllTheLeaks** | MIT | 50+ named mod/vanilla/Forge leaks | **P0 catalog** (issue #3) |
| **MemoryLeakFix** | LGPL | Vanilla client/server leaks | Band A |
| **Fix GPU Memory Leak** | ARR | Client GL resource leaks | Band A (reimplement carefully) |
| **Connectivity** | ARR | Login/packet/ghostblock | Band A (reimplement carefully) |
| **Recipe Essentials** | ARR | Recipe packet / lookup | Band A |
| **Debugify** | (check) | Mojira vanilla bugs (70+) | **P0 catalog** |
| **Clumps** | MIT | XP orb merge | Band B |
| **Let Me Despawn** | LGPL | Pickup-mob despawn | Band B |
| **AI Improvements** | ARR | Mob AI cost | Band D |
| **Krypton Reforged** | MIT | Network stack | Band C — historically fragile on Forge |
| **Starlight (Forge)** | MIT | Light engine rewrite | Band C — high risk |
| **No Chat Reports** | (check) | Chat signing strip | Feature-ish (MF has related) |
| **Crash Assistant** | (check) | Known-crash patterns | Diagnostic source only |
| **ImmediatelyFast** | LGPL | Immediate-mode render | Coexist; fix collisions only |
| **Entity Culling / More Culling** | Custom/LGPL | Cull | Fix false-cull only |
| **Yeetus Experimentus** | (check) | Experimental screen suppress | Related to MF extra_experimental |
| **Chunky / Chunk Pregenerator** | LGPL/ARR | Pregen tools | Not OmniFix core |
| **Dynamic View / Smooth Chunk Save / Fast Async World Save / Chunk Sending / Better Chunk Loading** | various | Server chunk/load | Band B/D individually |

### UsefulMods Performance120 (Forge section) — complete inventory

From [TheUsefulLists/UsefulMods Performance120.md](https://github.com/TheUsefulLists/UsefulMods/blob/main/Performance/Performance120.md) Forge 1.20.X:

AI Improvements, Alternate Current, Better Chunk Loading, Better Fps Render Distance, Canary, Connectivity, Chunk-Pregenerator, Chunky, Client Crafting, Clumps, Chunk Sending, Dynamic View, Embeddium, Entity Culling, Fast Async World Save, Fastload, FerriteCore, Fix GPU Memory Leak, Get It Together Drops, Krypton Reforged, Ksyxis, Leaky, Let Me Despawn, Limited Chunkloading, Log Begone, MemoryLeakFix, ModernFix, Radium Reforged, Recipe Essentials, Rubidium, Smooth Chunk Save, Starlight, Structure Essentials.

**P0 verdict:** OmniFix’s Band A skeleton is Debugify + ModernFix bugfix + AllTheLeaks + Connectivity-class + GPU leak-class.

---

# P1 — Extracted FeatureUnits (high-confidence catalogs)

## P1.1 Debugify / Mojira (vanilla always-on)

Source: Debugify `archive/1.20` PATCHED.md + 1.20.1+2.0 changelog additions (MC-263865, MC-8187).  
Each unpatched Mojira ID is one FeatureUnit. Implement as toggleable `vanilla.mc_#####` units.

### Client (always / client)

| FU-id | Bug | Summary |
|---|---|---|
| FU-VAN-MC577 | MC-577 | Mouse buttons block non-default inventory controls |
| FU-VAN-MC4490 | MC-4490 | Fishing line detach third-person crouch |
| FU-VAN-MC12829 | MC-12829 | Creative flight slowed by ladders/vines/scaffolding |
| FU-VAN-MC22882 | MC-22882 | Ctrl+Q Mac (default off) |
| FU-VAN-MC46766 | MC-46766 | Survival→Spectator keeps break animation |
| FU-VAN-MC55347 | MC-55347 | Long title persists across worlds |
| FU-VAN-MC59810 | MC-59810 | macOS sprint+break = right-click |
| FU-VAN-MC79545 | MC-79545 | XP bar vanishes at high levels |
| FU-VAN-MC80859 | MC-80859 | Dragged stacks invisible until size change |
| FU-VAN-MC90683 | MC-90683 | Unknown passenger log spam |
| FU-VAN-MC93384 | MC-93384 | Drown bubbles at feet |
| FU-VAN-MC105068 | MC-105068 | Shield hit plays wrong hurt sound |
| FU-VAN-MC108948 | MC-108948 | Boat hovers on slime |
| FU-VAN-MC111516 | MC-111516 | Player flicker at high speed |
| FU-VAN-MC112730 | MC-112730 | Beacon/structure block double render |
| FU-VAN-MC116379 | MC-116379 | Offhand cast rod punch detaches line |
| FU-VAN-MC122627 | MC-122627 | Tab suggestion padding |
| FU-VAN-MC122477 | MC-122477 | Linux chat types 't' |
| FU-VAN-MC127970 | MC-127970 | Riptide + offhand visual glitch |
| FU-VAN-MC143474 | MC-143474 | Respawn resets hotbar slot |
| FU-VAN-MC159163 | MC-159163 | Double sneak animation |
| FU-VAN-MC165381 | MC-165381 | Drop tool delays breaking |
| FU-VAN-MC176559 | MC-176559 | Mending resets break progress |
| FU-VAN-MC183776 | MC-183776 | F3+F4 requires double F3 |
| FU-VAN-MC197260 | MC-197260 | Armor stand dark head-in-block |
| FU-VAN-MC215531 | MC-215531 | Pumpkin overlay stuck in spectator |
| FU-VAN-MC217716 | MC-217716 | Nausea overlay stuck in spectator |
| FU-VAN-MC227169 | MC-227169 | Crossbow offhand breaks main hand render |
| FU-VAN-MC231097 | MC-231097 | Use slow persists after drop |
| FU-VAN-MC237493 | MC-237493 | Telemetry cannot be disabled |
| FU-VAN-MC263865 | MC-263865 | Fullscreen state not saved (F11) — 1.20.1+2.0 |

### Server / both

| FU-id | Bug | Summary |
|---|---|---|
| FU-VAN-MC2025 | MC-2025 | Mobs escape fences / suffocate on chunk load |
| FU-VAN-MC7569 | MC-7569 | RCON newlines stripped |
| FU-VAN-MC8187 | MC-8187 | 2×2 spruce/jungle saplings blocked N/W — 1.20.1+2.0 |
| FU-VAN-MC14923 | MC-14923 | SP kick for “spam” |
| FU-VAN-MC30391 | MC-30391 | Slow-fall particles on chicken/blaze/wither |
| FU-VAN-MC31819 | MC-31819 | Hunger saturation on Peaceful |
| FU-VAN-MC69216 | MC-69216 | Fishing rod stays cast in spectator |
| FU-VAN-MC81773 | MC-81773 | Drawn bow/trident releasable in spectator |
| FU-VAN-MC88371 | MC-88371 | Dragon voids if portal destroyed |
| FU-VAN-MC89146 | MC-89146 | Piston forgets update on reload |
| FU-VAN-MC93018 | MC-93018 | Wild wolves hearts no breed |
| FU-VAN-MC100991 | MC-100991 | Fishing-rod kills don’t count |
| FU-VAN-MC119417 | MC-119417 | Spectator can occupy bed |
| FU-VAN-MC119754 | MC-119754 | Elytra firework boost continues spectator |
| FU-VAN-MC121706 | MC-121706 | Skeleton strafe no look pitch |
| FU-VAN-MC121903 | MC-121903 | Command block minecart cooldown not NBT |
| FU-VAN-MC124177 | MC-124177 | Dim teleport loses client state |
| FU-VAN-MC129909 | MC-129909 | Spectator still consumes briefly |
| FU-VAN-MC132878 | MC-132878 | Armor stand no death particles |
| FU-VAN-MC135971 | MC-135971 | Ctrl+Q crafting table |
| FU-VAN-MC155509 | MC-155509 | Dying pufferfish still hurts |
| FU-VAN-MC160095 | MC-160095 | End rod only breaks cactus when pistoned |
| FU-VAN-MC179072 | MC-179072 | Creeper no defuse on creative/spectator |
| FU-VAN-MC183990 | MC-183990 | Group AI breaks on target death |
| FU-VAN-MC193343 | MC-193343 | Soul Speed stuck in spectator |
| FU-VAN-MC199467 | MC-199467 | Entity anim freezes after long life |
| FU-VAN-MC200418 | MC-200418 | Cured baby zomb villager stays jockey |
| FU-VAN-MC214147 | MC-214147 | Leather skeleton → stray in powder snow |
| FU-VAN-MC206705 | MC-206705 | Spyglass stuck in spectator |
| FU-VAN-MC206922 | MC-206922 | Lightning-kill drops vanish |
| FU-VAN-MC215530 | MC-215530 | Freeze effect stuck spectator |
| FU-VAN-MC223153 | MC-223153 | Raw copper wrong sounds |
| FU-VAN-MC224729 | MC-224729 | Partial chunks not saved |
| FU-VAN-MC231743 | MC-231743 | Pottable plant stat missing |
| FU-VAN-MC232869 | MC-232869 | Strider saddle peaceful |

**Band:** A · **Gate:** Always · **Status:** backlog · **Confidence:** High (Debugify has patches; verify still broken on 1.20.1 vanilla before port)

---

## P1.2 ModernFix `bugfix/*` (local tree)

| FU-id | Family | Root cause (from source) | Gate |
|---|---|---|---|
| FU-MF-BUF-LEAK | buffer_builder_leak | BufferBuilder not closed when put() replaces same render type | Always client |
| FU-MF-CHUNK-DEADLOCK | chunk_deadlock | Full-chunk promotion / entity add during worldgen path deadlocks | Always server |
| FU-MF-COFH-FLAG | cofh_core_crash | CoFH FlagManager unsync map access | `cofh_core` |
| FU-MF-CONC-REG | concurrency | Broken double-checked locking registries/tags; late reload listeners | Always |
| FU-MF-CTM-CME | ctm_resourceutil_cme | CTM ResourceUtil CME | `ctm` |
| FU-MF-DRAGON-LEAK | ender_dragon_leak | Client world leak via dragon entity ref | Always client |
| FU-MF-POSE-STACK | entity_pose_stack | Pose stack corruption living/player render | Always client |
| FU-MF-EXP-SCREEN | extra_experimental_screen | Experimental dialog re-shown incorrectly | Always client |
| FU-MF-VEHICLE-PKT | forge_vehicle_packets | Forge vehicle packet desync | Always |
| FU-MF-MISS-BE | missing_block_entities | Chunks missing BE data → invisible BE | Always client |
| FU-MF-MODELDATA-CME | model_data_manager_cme | ModelDataManager concurrency | Always client |
| FU-MF-PAPER-CHUNK | paper_chunk_patches | getNow schedule storm / SortedArraySet | Always server |
| FU-MF-RECIPE-BOOK | recipe_book_type_desync | Recipe book type desync | Always |
| FU-MF-REGOPS-CME | registry_ops_cme | RegistryOps memo CME | Always |
| FU-MF-REMOVED-DIM | removed_dimensions | Removing dim mod breaks worlds | Always |
| FU-MF-DRAGON-MOVE | restore_old_dragon_movement | MC-272431-class dragon vertical regression | Always (toggle) |
| FU-MF-SHAPE-CACHE | unsafe_modded_shape_caches | Non-threadsafe mod shape caches (RS/Cyclic) | Always / gated |
| FU-MF-WORLD-LEAK | world_leaks | Mitigate leaked ClientLevel structures | Always client |
| FU-MF-WORLD-SCREEN | world_screen_skipped | World select list skip edge case | Always client |

### ModernFix `perf/*` that are correctness-adjacent (promote early)

| FU-id | Family | Why not pure fluff |
|---|---|---|
| FU-MF-HANDSHAKE | fix_handshake_stall | Login hang on large packs |
| FU-MF-SPIN-WAIT | fix_loop_spin_waiting | Server busy-spin |
| FU-MF-SMART-ING | smart_ingredient_sync | Network ingredient bloat |
| FU-MF-DATAPACK-EX | datapack_reload_exceptions | Reload exception handling |

Remaining ~50 perf families → Band D (`FU-MF-PERF-*`), implement only with baselines.

### ModernFix feature/safety

| FU-id | Notes |
|---|---|
| FU-MF-TELEMETRY | remove_telemetry (overlaps MC-237493) |
| FU-MF-CHAT-SIGN | remove_chat_signing |
| FU-MF-SAFETY-COLORS | safety Item/BlockColors / ItemProperties / LivingEntityRenderer |

---

## P1.3 AllTheLeaks #3 (1.20.1 leak syllabus)

Source: [pietro-lopes/AllTheLeaks#3](https://github.com/pietro-lopes/AllTheLeaks/issues/3).  
Each row = gated FeatureUnit `FU-ATL-<mod>`. Version ranges from ATL apply.

| FU-id | Target | Leak class |
|---|---|---|
| FU-ATL-AE2WT | ae2wtlib | Player maps / creative tab not cleared |
| FU-ATL-AETHER | aether | DroppedItemCapability entity leak |
| FU-ATL-ALEXS | alexsmobs | ServerEvents / world data maps on unload |
| FU-ATL-ARCH | architectury | NetworkManagerImpl client receivables on clone |
| FU-ATL-ARS | ars_nouveau | Cap revive wrong API; tome registry level leak |
| FU-ATL-BADPKT | badpackets | ChannelRegistry handlers on close/stop |
| FU-ATL-BEANSBP | beansbackpacks | EnderStorage MAP on clone/logout |
| FU-ATL-BETTERF3 | betterf3 | LocationModule chunk leak |
| FU-ATL-BLUESKIES | blue_skies | Client lastRidden / ambient handler |
| FU-ATL-CITADEL | citadel | ModelAnimator entity; server data map |
| FU-ATL-CORPSE | corpse | Renderer players/skeletons maps |
| FU-ATL-CREATE | create | ExtendoGrip lastActiveDamageSource on clone |
| FU-ATL-CREATEADD | createaddition | EnergyNetworkManager level instances |
| FU-ATL-CURIOS | curios | Cap revive/invalidate on clone |
| FU-ATL-CYCLOPS | cyclopscore | Dynamic model world field |
| FU-ATL-DIFFLOCK | difficultylock | Util statics on server stop |
| FU-ATL-DOMEST | domesticationinnovation | Collar tick tracker on unload |
| FU-ATL-EASYVILL | easy_villagers | Item TE / villager caches |
| FU-ATL-EMI | emi | EmiHistory on clone/logout |
| FU-ATL-EMILOOT | emi_loot | EntityEmiStack entities |
| FU-ATL-EMF | entity_model_features | heldIteration on level change |
| FU-ATL-ETF | entity_texture_features | heldEntity / player texture map |
| FU-ATL-FINDME | findme | lastRenderedStack |
| FU-ATL-FLYWHEEL | flywheel | WorldAttached old world list |
| FU-ATL-FA | forbidden_arcanus | Missing invalidateCaps on clone |
| FU-ATL-FORGE | forge | FakePlayer advancement listeners / packet listeners |
| FU-ATL-FTBLIB | ftblibrary | BLANK_GUI prevScreen |
| FU-ATL-GECKO | geckolib | Molang queries strong refs; armor renderer entity |
| FU-ATL-ICEBERG | iceberg | EntityCollector levels; CustomItemRenderer entities |
| FU-ATL-IRONS | irons_spellbooks | Spell bar / music / synced spell data |
| FU-ATL-JEI | jei | RecipeTransferButtons player/container |
| FU-ATL-JER | jeresources | MobTableBuilder level entities |
| FU-ATL-JM | journeymap | Entity distance comparator player |
| FU-ATL-LDLIB | ldlib | ModularUI player; lambda level |
| FU-ATL-COLONIES | minecolonies | Recipe category / generic recipe entities |
| FU-ATL-MC | minecraft | Dragon model; EMPTY ItemStack entity; damage source; tick lists |
| FU-ATL-MNA | mna | FeyArmor / Gecko post-render cleanup |
| FU-ATL-MOONLIGHT | moonlight | Level clear return correctness |
| FU-ATL-MOUSE | mousetweaks | Open screen/handler on clone |
| FU-ATL-MOWZIE | mowziesmobs | Boss music / model animator entity |
| FU-ATL-NC | nuclearcraft | TooltipHandler processed event |
| FU-ATL-OCCULT | occultism | Missing invalidateCaps |
| FU-ATL-PHOS | phosphophyllite | ConfigManager players; ticking map |
| FU-ATL-PNC | pneumaticcraft | Armor UI options; drone event unregister |
| FU-ATL-RAILCRAFT | railcraft | Charge networks level |
| FU-ATL-SEASONS | sereneseasons | LevelRenderer snow level; season ticks |
| FU-ATL-SMALLSHIPS | smallships | ChunkMap mixin fields |
| FU-ATL-TFC | tfcthermaldeposits | worldLevel static |
| FU-ATL-TOMB | tombstone | LivingEntityRenderer entity mixin field |
| FU-ATL-TOOLBELT | toolbelt | Cap revive/invalidate |
| FU-ATL-TRAVELBP | travelersbackpack | Cap + backpack layer entity |
| FU-ATL-TF | twilightforest | ENTITY_MAP / HydraModel |

**Band:** A/B · **Gate:** per mod · **Status:** backlog · **Confidence:** High for “leak exists”; re-verify version ranges on current mod builds before ship.

---

## P1.4 Network / GPU / recipe (Connectivity-class)

Reimplement from first principles (ARR sources — do not copy):

| FU-id | Symptom | Approach |
|---|---|---|
| FU-NET-LOGIN-TIMEOUT | Login timeout large packs | Configurable handshake / play timeouts |
| FU-NET-PACKET-SIZE | DecoderException / packet too large | Raise safe limits / fragment where legal |
| FU-NET-PAYLOAD | Custom payload too large | Same family |
| FU-NET-GHOST | Ghost blocks after lag | Targeted resync path |
| FU-NET-RECIPE-PKT | Recipe packet blowup | Cache + reduce sync (Recipe Essentials class) |
| FU-GPU-LEAK | GL_OUT_OF_MEMORY / incomplete FBO | Release leaked GPU resources on resource reload / dimension change |
| FU-NET-HANDSHAKE-STALL | MF fix_handshake_stall | Already in MF extract |

---

## P1.5 Already in OmniFix (shipped / pending verify)

| FU-id | Layer | Status |
|---|---|---|
| FU-VP-FRUSTUM | VS×IP frustum cancel (MixinSquared) | shipped (standalone verify) |
| FU-VP-PORTAL-CAMERA | Empty panes on ship + bank matrix | shipped (standalone verify) |
| FU-VP-PORTAL-FOG | Embeddium fog RD nested pass | shipped (standalone verify) |
| FU-VP-SHIP-UNLOAD-CCE | VS unload × IP chunk source | pending_verify |
| FU-VP-SHIP-VIS | Remote ship visibility + tracker | pending_verify |
| FU-VP-SHIP-TRANSIT | Ship IP transit + momentum | pending_verify |
| FU-VP-ENTITY-DRAG | Portal not ship-draggable | pending_verify |
| FU-CREATE-IP-TRACKS-A | Nether portal track pairing | pending_verify |
| FU-KERNEL-STACK | StackDomain / profile / mixin plugin | shipped |

---

# P2 — Top Forge 1.20.1 ecosystem mod surface

Not a full CurseForge scrape (API limits); **ecosystem-defining** mods by prevalence in 1.20.1 packs + download class. Each is a domain for ongoing issue mining.

### Libraries / glue (almost every pack)

Architectury, Cloth Config, Balm, Puzzles Lib, Moonlight (Selene), GeckoLib, Kotlin for Forge, Forge Config API / Night Config, Patchouli, Curios, Player Animator, Resourceful Lib, Bookshelf, Zeta.

### Renderer / client

Embeddium, Oculus, ImmediatelyFast, Entity Culling, EMF, ETF, Jade/TOP, AppleSkin, Controlling, Searchables, Mouse Tweaks, Xaero’s (Minimap/World Map), JourneyMap, FTB Chunks map.

### Perf / fix (see P0)

ModernFix, FerriteCore, Connectivity, AllTheLeaks, Clumps, Canary/Radium, Starlight, etc.

### Content pillars (Band B domains)

| Domain id | Mods | Primary failure classes |
|---|---|---|
| `create` | Create 6.x + addons (Connected, Addition, Steam n Rails, Big Cannons, Copycats, Deco, …) | Contraption crashes, recipe load, Flywheel, portals, ships, redstone links |
| `ae2` | AE2 + WT + addons | Terminal memory, network, channels |
| `mek` | Mekanism suite | Multiblock render, radiation, tick |
| `ie` | Immersive Engineering | Multiblock, power |
| `pnc` | PneumaticCraft | Drone events (ATL), pressure |
| `thermal` | Thermal series | TE ticks |
| `ars` | Ars Nouveau | Cap clone, tome registry (ATL) |
| `irons` | Iron’s Spellbooks | Overlay/music leaks (ATL) |
| `botania` | Botania | (watch) |
| `colonies` | MineColonies + Structurize/Domum/BlockUI | Pathing, GUI entity leaks |
| `ftb` | FTB Library/Chunks/Quests/Teams/… | GUI leaks, map×portals |
| `sophisticated` | Core/Backpacks/Storage | Cap/GUI |
| `quark` | Quark + Zeta | Misc content edges |
| `twilight` | Twilight Forest | Dim, ENTITY_MAP leak |
| `aether` | Aether | Cap leak |
| `ad_astra` | Ad Astra | Dim/space |
| `cobblemon` | Cobblemon (+ addons) | Entity tick, join crashes |
| `vs` | Valkyrien Skies + Clockwork/Trackwork/Eureka | Physics×everything |
| `ip` | Immersive Portals | Nested world render×everything |
| `bettercombat` | Better Combat + player animator | Join/world load crashes reported |
| `apotheosis` | Apotheosis | Canary conflict noted in UsefulMods |
| `farmers` | Farmer’s Delight (+ addons) | (watch) |
| `tombstone` | Tombstone | Renderer entity leak |
| `securitycraft` | SecurityCraft | Ship/portal space (watch) |
| `supplementaries` | Supplementaries + Amendments | VS wiki stable/buggy; MixinSquared host often |

### Connector / Fabric-on-Forge

Sinytra Connector: **minefield**. Only add FU when a specific crash is root-caused. Do not “support Connector” as a feature.

---

# P3 — Major 1.20.1 modpack failure patterns

Patterns distilled from ATM9, Better MC Forge BMC4, Cobblemon packs, Create-centric packs, kitchen-sink forums (not single-instance unique).

| Pattern id | Frequency class | Symptom | OmniFix angle |
|---|---|---|---|
| PK-OOM-SESSION | Very high | RAM climbs over hours → crash | ATL + MF world/dragon leaks + GPU |
| PK-LOGIN-TIMEOUT | Very high | Cannot join large servers | Connectivity-class |
| PK-RECIPE-PACKET | High | Disconnect on recipe sync | Recipe Essentials-class |
| PK-MIXIN-COLLISION | High | Boot InvalidInjectionException | MixinSquared cancellers |
| PK-CCE-DIM | High | ClassCast after portal/dim change | Type-assumption breaks (VP ship unload exemplar) |
| PK-AMD-DRIVER | High | GPU driver crash (ATM9 famous) | Document + GPU leak hygiene; not always code-fixable |
| PK-SHADER-PORTAL | Medium | Black/broken portals with Oculus | IP×Oculus hard |
| PK-CONTRAPTION-CRASH | Medium | Create assemble/tick crash | Create-specific FU |
| PK-JEI-MEM | Medium | Memory with JEI open | ATL JEI + search trees |
| PK-CHUNK-DEADLOCK | Medium | Server freeze loading chunks | MF chunk_deadlock |
| PK-GHOST-BLOCKS | Medium | Blocks desync after lag | Connectivity ghost |
| PK-WORLD-LOAD-HANG | Medium | Stuck loading terrain | Ksyxis/Fastload class — safety audit first |
| PK-RESOURCE-RELOAD | Medium | CME / crash on F3+T or pack switch | concurrency + CTM + ModelData |
| PK-MODERNFIX-CLASH | Low–Med | VS/Clockwork render crash with MF | Isolate MF mixin × Clockwork |
| PK-BETTERCOMBAT-JOIN | Medium | Crash joining world with BC | Gate/investigate BC + animator |
| PK-DUPLICATE-JARS | Ops | Double mod jars undefined winner | Not code; pack hygiene docs |
| PK-CONNECTOR-CHAOS | Medium | Fabric+Forge hybrid crashes | Selective only |

**Modpacks used as pattern sources (not exclusive targets):** ATM9 / ATM9 Sky, Better MC BMC4 Forge, Cobblemon Academy–class, Create: Above and Beyond–likes still on 1.20.1, RLCraft-likes / hardcore kitchen sinks, custom 200–400 mod Forge packs.

---

# P4 — Domain deep dives

## P4.1 Create 6.x (Forge 1.20.1)

**Sources:** Create issue tracker, changelogs (6.0.2+ IP mention, fluid contraption crash fixes), ATL create/createaddition, VS Create mixins, OmniFix track provider.

| FU-id | Issue | Confidence | Status |
|---|---|---|---|
| FU-CREATE-IP-TRACKS-A | Nether track pairing via IP entity | High | pending_verify |
| FU-CREATE-IP-TRACKS-B | Block-less IP portals track pair | High | shipped (entity connectToPortal + updateShape keep) |
| FU-CREATE-IP-TRAIN | Train entity transit / far visibility | Med | backlog (research Create-IP third-party) |
| FU-CREATE-CONTRAPTION-IP | Non-train contraptions × portals | Med | backlog |
| FU-CREATE-EXTENDO-LEAK | ExtendoGrip damage source clone leak | High | backlog (ATL) |
| FU-CREATE-ADD-ENERGY | Create Addition energy network level leak | High | backlog (ATL) |
| FU-CREATE-REDSTONE-SHIP | Links / networks on VS ships (residual) | Med | backlog |
| FU-CREATE-FLYWHEEL-COLLIDE | Flywheel × other renderers | Low | watch |
| FU-CREATE-RECIPE-LOAD | Large Create recipe contribution to packet | Med | via FU-NET-RECIPE-PKT |

## P4.2 Valkyrien Skies 2.4.x

**Sources:** VS wiki Compat list + Common Issues, issue #1525, OmniFix VP layer, reference `mod_compat/*`.

| FU-id | Issue | Confidence | Status |
|---|---|---|---|
| FU-VP-* | Existing OmniFix VS×IP set | High | see P1.5 |
| FU-VS-IP-INTERACT | Cross-portal interact broken with VS (#1525) | High | shipped (feed IP remote hit → VS originalCrosshairTarget) |
| FU-VS-MF-CLASH | ModernFix × Clockwork item render crash | Med | backlog |
| FU-VS-WAND-CLIP | Clockwork raycast → clipIncludeShips | High likely | backlog |
| FU-VS-ENTITY-CULL | Entity Culling false-cull ships | Med | backlog |
| FU-VS-FACE-CULL | Embeddium face culling half-ships | Config-heavy | watch / optional force |
| FU-VS-REDSTONE-RESID | Create Connected links residual | Med | backlog |
| FU-VS-PATHFIND | Mobs/colonists on ships | Low | watch (hard) |
| FU-VS-INCOMPAT-RADIUM | Radium incompatibility | Document | watch |
| FU-VS-INCOMPAT-NSNT | No See No Tick | Document | watch |

VS already has internal compat for: Create, CBC, IP (partial), Sodium/Embeddium, FTB Chunks, EMF/ETF, TIS-3D, CC:Tweaked, etc. OmniFix fills **gaps and third-party collisions**, not a full VS rewrite.

## P4.3 Immersive Portals 3.x Forge

| FU-id | Issue | Confidence | Status |
|---|---|---|---|
| FU-IP-OCULUS | Nested pass shader breakage | Med hard | backlog (research-heavy) |
| FU-IP-DH-LOD | Distant Horizons × portals | Med | watch |
| FU-IP-MAP-FTB | FTB/Xaero map across portal views | Low cosmetic | watch |
| FU-IP-CREATE-* | See Create | — | — |
| FU-IP-VS-* | See VS | — | — |

## P4.4 Embeddium / Oculus / render

| FU-id | Issue | Confidence | Status |
|---|---|---|---|
| FU-REN-RSM-COLLISION | Multi-mod inject on RenderSectionManager | High | backlog as cancellers appear |
| FU-REN-GPU-LEAK | See FU-GPU-LEAK | High | backlog |
| FU-REN-BE-CULL | BE invisible (max BE cull distance) | Config / optional force | watch |
| FU-REN-IF-VERSION | ImmediatelyFast multi-version jars | Ops | document |

## P4.5 AE2

| FU-id | Issue | Confidence | Status |
|---|---|---|---|
| FU-AE2-WT-LEAK | ae2wtlib player maps | High | backlog (ATL) |
| FU-AE2-TERMINAL-MEM | Terminal open memory growth (upstream reports) | Med | backlog / verify version |

## P4.6 Mekanism / IE / PNC / Thermal

| FU-id | Issue | Confidence | Status |
|---|---|---|---|
| FU-PNC-DRONE-EVENT | Drone Forge bus unregister (ATL) | High | backlog |
| FU-MEK-VS-MB | Historical multiblock render on ships (older VS) | Low on 2.4.11 | watch |
| FU-IE-* | No high-conf unique 1.20.1 crash cluster this pass | — | watch |

## P4.7 Magic (Ars / Iron’s / Occultism / Botania)

| FU-id | From ATL | Status |
|---|---|---|
| FU-ATL-ARS | Cap + tome | backlog |
| FU-ATL-IRONS | Overlay/music/data | backlog |
| FU-ATL-OCCULT | Cap invalidate | backlog |
| FU-ATL-MNA | Renderer cleanup | backlog |

## P4.8 MineColonies

| FU-id | Issue | Status |
|---|---|---|
| FU-ATL-COLONIES | JEI/recipe GUI entity leaks | backlog |
| FU-COL-VS-PATH | Pathing on ships | watch (hard) |
| FU-COL-CREATE | Create integration (external compat mods exist) | watch |

## P4.9 FTB / maps / QoL

| FU-id | Issue | Status |
|---|---|---|
| FU-ATL-FTBLIB | GUI prevScreen leak | backlog |
| FU-FTB-IP-MAP | Map through portals | watch |
| FU-ATL-JM | JourneyMap comparator player | backlog |

## P4.10 Cobblemon

| FU-id | Issue | Status |
|---|---|---|
| FU-COBBLE-TICK | Entity tick load on servers | Med — specialized optimizer mods exist; verify root cause |
| FU-COBBLE-JOIN | Random join failures (upstream changelogs) | Prefer upstream; OmniFix only if residual |

## P4.11 Inventory / cosmetics

| FU-id | Status |
|---|---|
| FU-ATL-CURIOS | backlog |
| FU-ATL-TRAVELBP | backlog |
| FU-ATL-TOOLBELT | backlog |
| FU-ATL-BEANSBP | backlog |

## P4.12 Dimensions / worldgen

| FU-id | Status |
|---|---|
| FU-MF-REMOVED-DIM | backlog |
| FU-ATL-TF / AETHER / BLUESKIES | backlog |
| FU-WG-STRUCTURE-LOCATE | Structure Essentials-class — Band D/B |
| FU-WG-DFU | Lazy DFU / dynamic DFU — Band D |

---

# P5 — Living intake process + master prioritization

## P5.1 Intake rules (permanent)

1. **Reproduce** on Forge 1.20.1 (minimal set when possible).  
2. **Root-cause** (decompile, bisect, probes).  
3. **Assign FU-id**, Band, Gate.  
4. **Implement complete** FeatureUnit with domain gate + mixin plugin gate.  
5. **Coexistence:** if ModernFix/ATL/Debugify present, prefer no double-apply (detect mod or MixinSquared cancel own conflict).  
6. **Verify** log + in-game.  
7. **Document** in COMPAT_MATRIX or FeatureUnit registry.

## P5.2 Continuous sources (P5 ops)

| Cadence | Source |
|---|---|
| Weekly | Mojira open bugs affecting 1.20.1 still |
| Weekly | ModernFix / AllTheLeaks / Connectivity / Debugify releases |
| Weekly | Create, VS, IP, Embeddium, Oculus issue trackers |
| Weekly | ATM / Better MC / Cobblemon issue labels `Crash` |
| Continuous | Crash Assistant known-pattern lists |
| Continuous | User reports with logs (any pack) |

## P5.3 Implementation order (forced by evidence)

### Wave 0 — Foundation — **SHIPPED**
1. In-game **pending_verify** for VP/Create — still required on a real pack.  
2. Shade MixinSquared + MixinExtras + bootstrap — **done**.  
3. FeatureUnit registry + `config/omnifix-features.properties` — **done**.

### Wave 1 — Band A universal — **SHIPPED (implemented set)**
- ModernFix-class bugfix ports (`FU-MF-*`): concurrency, chunk deadlock, world leak, render buffers, vehicle packets, missing BE, model data CME, removed dims, pose stack, dragon leak, experimental screen, SortedArraySet, RegistryOps, shape caches.  
- Network: login timeout 120s, read-timeout floor 120s.  
- Leak: FakePlayerFactory clear on server stop.  
- **Not shipped this pass (would be incomplete without more infrastructure):** recipe-book vanilla-connection path, BufferBuilder finalize/UnsafeBufferHelper, GPU GL leak (ARR source reimplement), full MC-2025 fence path.

### Wave 2 — high-download leaks — **PARTIAL SHIPPED**
- Create ExtendoGrip static clear + Curios cap hygiene on `PlayerEvent.Clone`.  
- **Remaining ATL rows** (JEI, GeckoLib, EMI, EMF/ETF, FTB, Iron’s, Ars, Colonies, …) stay backlog — each needs version-pinned mixins, not bulk guesswork.

### Wave 3 — Dominant seams — **SHIPPED (existing) + backlog**
- Full VS×IP layer + Create nether tracks (Wave 0).  
- **Shipped this pass:** VS #1525 cross-portal interact, Create tracks leg (b).  
- **Backlog (no stub code):** Clockwork `clipIncludeShips`, MF×Clockwork clash isolation, Create train transit.

### Wave 4 — Mojira / Debugify-class — **SHIPPED (high-value batch)**
- Spectator stuck effects + overlays, spectator bed, mending break progress, fullscreen F11 save, 2×2 sapling free-space (MC-8187).  
- Auto-skipped when Debugify is present.  
- **Remaining Mojira list** from PATCHED.md stays backlog for incremental ports.

### Wave 5 — Band D correctness-adjacent perf — **SHIPPED (two proven)**
- Forge handshake re-tick + sentMessages sync.  
- Server tick parkNanos instead of spin-wait.  
- Auto-skipped when ModernFix present.  
- **Bulk Canary/FerriteCore/MF perf** remains measure-first backlog.

### Wave 6 — Hard graphics / pathfinding — **BACKLOG ONLY (SOUL)**
No incomplete Oculus×IP / full ship pathfinding / Starlight code was added. These require dedicated root-cause spikes before any FeatureUnit lands.

---

## Count summary (this pass)

| Catalog | Approximate FeatureUnits |
|---|---|
| Debugify / Mojira (1.20 archive + extras) | ~65 |
| ModernFix bugfix + correctness-adjacent | ~25 |
| AllTheLeaks named targets | ~50 |
| Network/GPU/recipe | ~7 |
| OmniFix shipped/pending | ~9 |
| Create / VS / IP / render / AE2 / colonies / cobblemon extras | ~40+ |
| **Total tracked this document** | **~200 FeatureUnits** |

Plus Band D perf families (~50) as deferred measured queue.

---

## Explicit non-goals (until proven)

- Bulk-porting every UsefulMods “FPS” mod without root cause  
- Blind Starlight / full Lithium behavior without equivalence tests  
- “Connector support” as a slogan  
- Pack hygiene automation (duplicate jar deletion) as mixin code  
- Claiming Oculus×IP fully fixed without nested shader pipeline proof  

---

## Research program completion checklist

| Pass | Deliverable | Done |
|---|---|---|
| **P0** | Fix-mod landscape + UsefulMods Forge inventory | ✅ |
| **P1** | Debugify PATCHED + MF bugfix + ATL #3 + net/GPU as FeatureUnits | ✅ |
| **P2** | Ecosystem mod domains + failure classes | ✅ |
| **P3** | Cross-pack failure patterns | ✅ |
| **P4** | Domain deep dives Create/VS/IP/Render/AE2/Mek/Magic/Colonies/FTB/Cobblemon | ✅ |
| **P5** | Living intake rules + wave-ordered backlog | ✅ |

---

## Next action after research

Research program **P0–P5 is complete** as an intake artifact.  
**Implementation** begins at Wave 0 (verify shipped VP layer → MixinSquared JiJ → FeatureUnit registry → Wave 1 Band A).

*This file is the living master backlog. Update FU rows when implemented, rejected, or root-cause revised.*
