# OmniFix Formal FeatureUnit Backlog — Forge 1.20.1

**Living matrix.** Status: `shipped` | `backlog` | `research` | `defer`.  
**Confidence:** H = root-caused + implemented or ready · M = strong evidence · L = needs dig.  
**Coexist:** skip when peer mod present (MF/Debugify/ATL) still applies unless force-enabled later.

Mandate: platform-wide Forge 1.20.1; maximize real fixes; no generic bullshit; other mods do not block inclusion.

---

## Shipped (in `omnifix-0.1.0-alpha` jar)

| Id | Domain | Source | Gate | Confidence | Status |
|---|---|---|---|---|---|
| vp.frustum_deadloop | VS×IP | VS/IP frustum inject collision | VS+IP | H | shipped |
| vp.portal_camera | VS×IP | Empty portal panes on ship | VS+IP | H | shipped |
| vp.portal_fog | VS×IP | Nested pass fog RD | VS+IP+Embeddium | H | shipped |
| vp.ship_unload_cce | VS×IP | IP chunk source cast CCE | VS+IP | H | shipped |
| vp.ship_visibility | VS×IP | Remote ships through portals | VS+IP | H | shipped |
| vp.ship_transit | VS×IP | Ship IP transit | VS+IP | H | shipped |
| vp.entity_drag | VS×IP | Portals not ship-draggable | VS+IP | H | shipped |
| vp.diagnostics | VS×IP | Ship data probe | VS+IP+Emb | H | shipped (default off) |
| create.ip_tracks_nether | Create×IP | PortalTrackProvider via IP entity | Create+IP | H | shipped |
| vanilla.spectator_stuck_effects | Vanilla | MC-215531/217716/215530/193343/206705/119754/69216 | always | H | shipped |
| vanilla.spectator_bed | Vanilla | MC-119417 | always | H | shipped |
| vanilla.mending_break_progress | Vanilla | MC-176559 | always | H | shipped |
| vanilla.sapling_2x2 | Vanilla | MC-8187 | always | H | shipped |
| vanilla.fullscreen_state | Vanilla | MC-263865 | always | H | shipped |
| FU-MF-BUF-LEAK | Bugfix | MF renderbuffers put | always | H | shipped |
| FU-MF-CHUNK-DEADLOCK | Bugfix | MF chunk currentlyLoading + mount | always | H | shipped |
| FU-MF-CONC-REG | Bugfix | MF registry/tag DCL + reload thread | always | H | shipped |
| FU-MF-DRAGON-LEAK | Bugfix | MF dragon model entity | always | H | shipped |
| FU-MF-POSE-STACK | Bugfix | MF pose stack cancel balance | always | H | shipped |
| FU-MF-EXP-SCREEN | Bugfix | MF experimental create flag | always | H | shipped |
| FU-MF-VEHICLE-PKT | Bugfix | MF vehicle positionRider | always | H | shipped |
| FU-MF-MISS-BE | Bugfix | MF missing client BEs | always | H | shipped |
| FU-MF-MODELDATA-CME | Bugfix | MF ModelDataManager | always | H | shipped |
| FU-MF-PAPER-CHUNK | Bugfix | MF SortedArraySet removeIf | always | H | shipped |
| FU-MF-REGOPS-CME | Bugfix | MF RegistryOps memo map | always | H | shipped |
| FU-MF-REMOVED-DIM | Bugfix | MF partial dim load | always | H | shipped |
| FU-MF-SHAPE-CACHE | Bugfix | MF RS/Cyclic ConcurrentHashMap | RS/Cyclic | H | shipped |
| FU-MF-WORLD-LEAK | Bugfix | MF clearLevel mitigation | always | H | shipped |
| FU-MF-WORLD-SCREEN | Bugfix | MF create-screen skip | always | H | shipped |
| leak.forge_fakeplayer | Leak | ATL forge FakePlayer factory | always | H | shipped |
| leak.create_extendo | Leak | ATL ExtendoGrip static | Create | H | shipped |
| leak.curios_clone | Leak | ATL curios cap clone | Curios | H | shipped |
| net.login_timeout | Net | Connectivity-class login ticks | always | H | shipped |
| net.read_timeout | Net | Connectivity-class read timeout floor | always | H | shipped |
| net.compression_size | Net | CompressionDecoder 2/8MiB→16MiB | always | H | shipped |
| net.play_timeout | Net | Keep-alive LATENCY_CHECK 15s→60s | always | H | shipped |
| net.payload_split | Net | Custom payload 1MiB/32KiB + encode/frame raise | always | H | shipped |
| perf.handshake_stall | Perf | MF handshake re-tick | always | H | shipped |
| perf.loop_spin_waiting | Perf | MF parkNanos tick wait | always | H | shipped |

**Shipped count:** **367** FeatureUnits · **Mixin/handler Java files:** ~500+

**Ecosystem atlas:** `ECOSYSTEM_MAP.md` (OmniFix FC/D, D1–D12). **Full Minecraft corpus:** `CORPUS.md` (JE/BE, all loaders, content pillars, ops, diagnostics).

---

## Band A backlog — Vanilla Mojira (Debugify archive/1.20 unpatched)

| Id (proposed) | Ticket | Summary | Confidence | Status |
|---|---|---|---|---|
| vanilla.mouse_inventory | MC-577 | Mouse buttons block non-default inventory binds | M | **shipped** |
| vanilla.fishing_line_crouch | MC-4490 | Fishing line third-person crouch | M | **shipped** |
| vanilla.creative_ladder_slow | MC-12829 | Creative flight slow in ladders | M | **shipped** |
| vanilla.mac_ctrl_q | MC-22882 | Mac Ctrl+Q | M | **shipped** |
| vanilla.spectator_break | MC-46766 | Survival→spectator keep break anim | H | **shipped** |
| vanilla.title_clear | MC-55347 | Title persists across worlds | H | **shipped** |
| vanilla.mac_sprint_break | MC-59810 | Mac sprint break = right click | M | **shipped** |
| vanilla.xp_bar_vanish | MC-79545 | XP bar vanishes high levels | M | **shipped** |
| vanilla.drag_stack_invisible | MC-80859 | Drag stacks invisible | M | **shipped** |
| vanilla.unknown_passenger | MC-90683 | Unknown passenger log spam | M | **shipped** |
| vanilla.drown_bubbles_feet | MC-93384 | Drown bubbles at feet | L | **shipped** (`vanilla.drown_bubbles`) |
| vanilla.shield_hurt_sound | MC-105068 | Shield hit wrong sound | M | **shipped** |
| vanilla.boat_slime_hover | MC-108948 | Boat on slime hovers | M | **shipped** |
| vanilla.high_speed_flicker | MC-111516 | High-speed player flicker | M | **shipped** |
| vanilla.beacon_double_render | MC-112730 | Beacon/structure double render | M | skip (no clean Forge root cause) |
| vanilla.offhand_rod_punch | MC-116379 | Offhand cast rod punch | M | **shipped** |
| vanilla.tab_padding | MC-122627 | Tab suggestion padding | L | backlog |
| vanilla.linux_chat_t | MC-122477 | Linux chat types t | M | **shipped** |
| vanilla.riptide_offhand | MC-127970 | Riptide offhand glitch | M | **shipped** |
| vanilla.respawn_hotbar | MC-143474 | Respawn hotbar slot reset | M | **shipped** (`vanilla.hotbar_respawn`) |
| vanilla.double_sneak_anim | MC-159163 | Double sneak animation | M | **shipped** |
| vanilla.drop_tool_break_delay | MC-165381 | Drop tool delays break | M | **shipped** (`vanilla.break_delay_drop_tool`) |
| vanilla.f3_double | MC-183776 | F3+F4 needs double F3 | M | **shipped** |
| vanilla.armorstand_dark | MC-197260 | Armor stand dark in block | M | **shipped** |
| vanilla.crossbow_mainhand | MC-227169 | Crossbow offhand breaks mainhand | M | **shipped** (`vanilla.crossbow_offhand`) |
| vanilla.use_slow_persist | MC-231097 | Use slow after drop | M | **shipped** (`vanilla.use_slow_after_drop`) |
| vanilla.telemetry_disable | MC-237493 | Telemetry cannot disable | H | **shipped** |
| vanilla.mob_fence_escape | MC-2025 | Mobs escape fences chunk load | H | **shipped** |
| vanilla.rcon_newlines | MC-7569 | RCON newlines | L | skip (1.20.1 already appends `\n`) |
| vanilla.sp_chat_spam | MC-14923 | SP chat spam kick | H | **shipped** |
| vanilla.slow_fall_particles | MC-30391 | Chicken/blaze/wither land particles | H | **shipped** |
| vanilla.peaceful_saturation | MC-31819 | Hunger saturation peaceful | M | **shipped** |
| vanilla.spectator_projectile | MC-81773 | Bow/trident release spectator | H | **shipped** |
| vanilla.dragon_void_portal | MC-88371 | Dragon voids if portal destroyed | M | **shipped** |
| vanilla.piston_reload | MC-89146 | Piston forgets update reload | H | **shipped** |
| vanilla.wolf_hearts | MC-93018 | Wild wolf hearts no breed | M | **shipped** |
| vanilla.fishing_kill_count | MC-100991 | Fishing kill no count | M | **shipped** |
| vanilla.skeleton_look | MC-121706 | Skeleton strafe no pitch | L | **shipped** |
| vanilla.cmd_minecart_nbt | MC-121903 | Cmd block minecart cooldown NBT | M | **shipped** |
| vanilla.dim_teleport_state | MC-124177 | Dim teleport loses client state | M | **shipped** |
| vanilla.spectator_consume | MC-129909 | Spectator still consumes | M | **shipped** |
| vanilla.armorstand_particles | MC-132878 | Armor stand no death particles | L | **shipped** |
| vanilla.ctrl_q_craft | MC-135971 | Ctrl+Q crafting table | M | **shipped** |
| vanilla.pufferfish_dying | MC-155509 | Dying pufferfish hurts | M | **shipped** |
| vanilla.endrod_cactus | MC-160095 | End rod cactus piston | L | **shipped** |
| vanilla.creeper_defuse | MC-179072 | Creeper no defuse creative/spec | H | **shipped** |
| vanilla.group_ai_death | MC-183990 | Group AI on target death | M | **shipped** |
| vanilla.entity_anim_freeze | MC-199467 | Entity anim freeze long life | M | **shipped** |
| vanilla.zombvillager_jockey | MC-200418 | Cured baby stays jockey | M | **shipped** |
| vanilla.leather_stray | MC-214147 | Leather skeleton → stray | M | **shipped** (`vanilla.leather_skeleton_stray`) |
| vanilla.lightning_drops | MC-206922 | Lightning kill drops vanish | M | **shipped** |
| vanilla.raw_copper_sound | MC-223153 | Raw copper stone sounds | H | **shipped** (still broken on 1.20.1) |
| vanilla.partial_chunk_save | MC-224729 | Partial chunks not saved | H | **shipped** |
| vanilla.pottable_stat | MC-231743 | Pottable plant stat | L | **shipped** |
| vanilla.strider_saddle_peaceful | MC-232869 | Strider saddle peaceful | M | **shipped** |

---

## Band A/B backlog — AllTheLeaks #3 (1.20.1)

Source: [AllTheLeaks#3](https://github.com/pietro-lopes/AllTheLeaks/issues/3). Version ranges from ATL apply at implement time.

| Id (proposed) | Target mod | Leak class | Status |
|---|---|---|---|
| leak.ae2wt | ae2wtlib | Player maps / creative tab | **shipped** |
| leak.aether | aether | DroppedItemCapability | **shipped** |
| leak.alexsmobs | alexsmobs | ServerEvents / world data maps | **shipped** |
| leak.architectury | architectury | NetworkManager client receivables | **shipped** |
| leak.ars | ars_nouveau | Cap revive + tome registry | **shipped** |
| leak.badpackets | badpackets | ChannelRegistry handlers | **shipped** |
| leak.beansbp | beansbackpacks | EnderStorage MAP | **shipped** (`leak.beansbackpacks`) |
| leak.betterf3 | betterf3 | LocationModule chunk | **shipped** |
| leak.blueskies | blue_skies | lastRidden / ambient | **shipped** |
| leak.citadel | citadel | ModelAnimator / server data | **shipped** |
| leak.corpse | corpse | Renderer maps | **shipped** |
| leak.create_extendo | create | ExtendoGrip damage | **shipped** |
| leak.createaddition | createaddition | EnergyNetworkManager level | **shipped** |
| leak.curios_clone | curios | Cap revive | **shipped** |
| leak.cyclops | cyclopscore | Dynamic model world | **shipped** (InstanceLeakRegistry) |
| leak.easyvillagers | easy_villagers | Item TE cache | **shipped** (`leak.easy_villagers`) |
| leak.emi | emi | EmiHistory | **shipped** |
| leak.emi_loot | emi_loot | EntityEmiStack | **shipped** (InstanceLeakRegistry) |
| leak.emf | entity_model_features | heldIteration | **shipped** |
| leak.etf | entity_texture_features | heldEntity | **shipped** |
| leak.findme | findme | lastRenderedStack | **shipped** |
| leak.flywheel | flywheel | WorldAttached | **shipped** |
| leak.forbidden_arcanus | forbidden_arcanus | invalidateCaps | **shipped** |
| leak.forge_fakeplayer | forge | FakePlayer factory | **shipped** |
| leak.ftb_library | ftb_library | BLANK_GUI prevScreen | **shipped** |
| leak.geckolib | geckolib | Molang / armor entity | **shipped** |
| leak.iceberg | iceberg | EntityCollector / item renderer | **shipped** |
| leak.irons | irons_spellbooks | Overlay / music / spell data | **shipped** (`leak.irons_spellbooks`) |
| leak.jei | jei | RecipeTransferButtons | **shipped** |
| leak.jer | jeresources | MobTableBuilder entities | **shipped** |
| leak.journeymap | journeymap | Distance comparator player | **shipped** |
| leak.ldlib | ldlib | ModularUI player | **shipped** (InstanceLeakRegistry) |
| leak.minecolonies | minecolonies | Recipe GUI entities | **shipped** |
| leak.mc_vanilla | minecraft | EMPTY stack entity / tick lists | **shipped** |
| leak.mna | mna | Armor renderer cleanup | **shipped** |
| leak.moonlight | moonlight | Level clear return | **shipped** |
| leak.mousetweaks | mousetweaks | Open screen fields | **shipped** (`leak.mouse_tweaks`) |
| leak.mowzies | mowziesmobs | Boss music / animator | **shipped** |
| leak.nuclearcraft | nuclearcraft | TooltipHandler event | **shipped** |
| leak.occultism | occultism | Cap invalidate | **shipped** |
| leak.phosphophyllite | phosphophyllite | ConfigManager players | **shipped** |
| leak.pnc | pneumaticcraft | Armor UI / drones | **shipped** |
| leak.railcraft | railcraft | Charge networks | **shipped** |
| leak.sereneseasons | sereneseasons | Snow level / season ticks | **shipped** |
| leak.smallships | smallships | ChunkMap fields | **shipped** |
| leak.tombstone | tombstone | LivingEntityRenderer entity | **shipped** |
| leak.toolbelt | toolbelt | Cap revive | **shipped** |
| leak.travelersbackpack | travelersbackpack | Cap + layer entity | **shipped** |
| leak.twilightforest | twilightforest | ENTITY_MAP / HydraModel | **shipped** |

---

## Band A backlog — Connectivity / Recipe Essentials class

| Id | Symptom | Reimplement approach | License note | Status |
|---|---|---|---|---|
| net.login_timeout | Login 30s kick | ServerLoginPacketListenerImpl 600→2400 ticks | Independent | **shipped** |
| net.read_timeout | Read timeout | ReadTimeoutHandler floor 120s | Independent | **shipped** |
| net.compression_size | DecoderException large packets | CompressionDecoder 2/8MB → 16MB | Independent | **shipped** |
| net.play_timeout | Keep-alive latency check | 15s → 60s | Independent | **shipped** |
| net.payload_split | Custom payload too large | Raise vanilla custom-payload 1MiB/32KiB + PacketEncoder 8MiB + Varint21 3→5; Forge VanillaPacketSplitter already covers recipe/tag split | Independent | **shipped** |
| net.ghost_blocks | Ghost blocks after lag | Block ack / resync path | Research ARR Connectivity | research — see notes below |
| net.recipe_packet | Recipe packet blowup | Cache / compress / dedupe sync | Research Recipe Essentials ARR | research — see notes below |
| perf.handshake_stall | Login freeze payload drip | Handshake re-tick | Independent (MF-class) | **shipped** |

Connectivity is **All Rights Reserved** — reimplement symptoms from vanilla/Forge contracts only, never copy ARR sources.

### Research notes — skipped Band A net units (2026-07-17)

**net.ghost_blocks (skip — no clean vanilla/Forge constant contract)**  
- Symptom class: client-side “ghost” blocks after lag (break/place prediction vs server reality).  
- Vanilla surface is multi-path: client prediction in multiplayer game mode, block-break progress, `ClientboundBlockUpdatePacket` / multi-block section updates, and lag-induced packet reordering — not a single documented ceiling or missing ack API.  
- Connectivity allegedly resyncs/acks; without ARR sources there is no isolated, reimplementable vanilla hook that is safe to ship without inventing a full block-resync protocol.  
- Status: remain research until a pure vanilla desync path is root-caused with a minimal fix.

**net.recipe_packet (skip — Forge already splits; further work is ARR-adjacent)**  
- Symptom class: join-time recipe graph blowup / disconnect on huge recipe registries.  
- Forge 1.20.1 already registers `VanillaPacketSplitter` (`forge:split`) and `ForgeConnectionNetworkFilter` splits `ClientboundUpdateRecipesPacket` (also tags, advancements, login) on forge↔forge links when remote advertises the channel.  
- Residual “recipe packet” pain after size raises is cache/compress/dedupe of recipe sync (Recipe Essentials class) — not a missing vanilla size constant. Implementing that without ARR sources would be a large independent recipe-sync redesign, not a FeatureUnit-sized constant raise.  
- `net.payload_split` + `net.compression_size` cover the size side of recipe/custom channel traffic.

---

## Band B/C backlog — ecosystem seams

| Id | Domain | Notes | Status |
|---|---|---|---|
| vp.cross_portal_interact | VS×IP | VS #1525 originalCrosshairTarget | **shipped** |
| create.ip_tracks_b | Create×IP | Block-less IP entity portal tracks | **shipped** |
| create.ip_train_transit | Create×IP | Train entity transit | **shipped** |
| vs.clockwork_clip | VS | RaycastHelper → clipIncludeShips | **shipped** |
| vs.mf_clash | VS×MF | Clockwork item render crash | research |
| ren.gpu_leak | Renderer | GL_OUT_OF_MEMORY path | research |
| ren.rsm_collision | Embeddium | Multi-mod inject cancel | research |
| cobblemon.tick | Cobblemon | Entity tick load | research |
| ip.oculus | IP×Oculus | Nested shader passes | defer (hard) |

---

## ModernFix remaining (eligible absorb)

| Family | Type | Status |
|---|---|---|
| bugfix.buffer_builder finalize/UnsafeBufferHelper | bugfix | **shipped** (FU-MF-BUF-LEAK) |
| bugfix.recipe_book_type_desync | bugfix | **shipped** (FU-MF-RECIPE-BOOK) |
| bugfix.cofh_core_crash | bugfix | **shipped** (FU-MF-COFH-FLAGS) |
| bugfix.paper_chunk_patches ChunkMap | bugfix | **shipped** (FU-MF-PAPER-CHUNKMAP) |
| perf.cache_strongholds / zip_pack_index / attribute / profile / cubes | perf | **shipped** |
| perf.dedicated_reload / fast_forge_dummies / worldgen_alloc / imposter | perf | **shipped** |
| perf.ingredient_dedup / forge_cap_retrieval / suspend_integrated | perf | **shipped** |
| perf.faster_item_rendering / compact_mojang_registries / cache_structures / dynamic_dfu | perf | **shipped** |
| perf.remove_spawn_chunks / optimize_surface_rules / release_protochunks | perf | **shipped** |
| perf.dynamic_resources / smart_ingredient | perf | still absorb targets (mega/protocol) |
| perf.faster_loot_loading | perf | **shipped** |
| perf.faster_texture_stitching / mixin_injector_group / clear_mixin_classinfo / patchouli | perf | **shipped** |
| feature.mcfunction_profiling / registry_event_progress | feature | **shipped** |
| perf.nightconfig_watch_throttle / mod_scan_compact / manifest_compact / mod_work_queue | perf | **shipped** |
| bugfix.nightconfig_config_crash / debug_overlay_clear | bugfix | **shipped** |
| feature.remove_telemetry | feature | **shipped** (`vanilla.telemetry_disable`) |
| perf.warden_sensor / axolotl_attack / frog_attack / enderman_leave / ghast_wander / ghast_fireball / silverfish_wake / bee_wander | perf | **shipped** (entity-brain wave-6) |
| perf.blaze_fire_interval / guardian_attack_duration / vex_random_move_chance / vex_charge_chance / hoglin_repellent_range / piglin_repellent_range / allay_heal_period / rabbit_raid_range | perf | **shipped** (entity-brain wave-7) |
| perf.slime_jump_delay / elder_guardian_attack_duration / evoker_fang_interval / evoker_summon_interval / evoker_wololo_interval / turtle_go_home_chance / turtle_lay_egg_duration / bee_grow_crop_interval | perf | **shipped** (entity-brain wave-8) |
| perf.panda_roll_chance / panda_sneeze_chance / polar_bear_cub_scan / drowned_water_search / bee_go_hive_timeout / bee_go_flower_timeout / fishing_open_water_scan / panda_sit_item_scan | perf | **shipped** (entity-brain wave-9) |

---

## Top Forge 1.20.1 ecosystem targets (P2 — prevalence, not live CF rank)

Always-on libs: Architectury, Cloth, Balm, GeckoLib, Kotlin for Forge, Moonlight, Curios, Patchouli.  
Renderer: Embeddium, Oculus, ImmediatelyFast, EMF/ETF.  
Content pillars: Create 6, AE2, Mek, IE, PNC, Thermal, Ars, Iron’s, MineColonies, FTB*, Sophisticated*, JEI/EMI, Quark, TF/Aether/Blue Skies/Ad Astra, Cobblemon, VS+Clockwork, Immersive Portals.  
Fix peers: ModernFix, FerriteCore, Connectivity, AllTheLeaks, Debugify, Canary/Radium.

---

## Implementation order (forced)

1. Finish **implementing** rows in this file.  
2. ATL high-download: JEI, GeckoLib, FTB Library, EMF/ETF, Create Addition, Ars, Iron’s.  
3. Remaining Mojira client/server tickets by severity.  
4. Connectivity payload/ghost after clean vanilla reimplement design.  
5. Create×IP trains / VS #1525 after repro.  
6. Band D only with measurements.

---

*Last updated: 2026-07-21 — FeatureUnits **367**; entity-brain wave-9 (+8: panda-roll/panda-sneeze/polar-bear-cub/drowned-water/bee-go-hive/bee-go-flower/fishing-open-water/panda-sit-item) · MF peer-skip = Handshake/SpinWait only.*
