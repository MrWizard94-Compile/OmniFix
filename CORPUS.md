# CORPUS — Entire Minecraft & Modding Ecosystem Problem Space

**Document type:** Living research corpus (atlas), not an implementation backlog  
**Status:** Bootstrap crawl **v1.0** — 2026-07-21  
**Scope:** Minecraft as a product family + **Java** modding ecosystem (primary) + Bedrock/plugins/edge as contrast  
**Method:** Public web crawl (Mojira signals, UsefulMods Performance120, CrashDetector patterns, Forge/Fabric forums, pack troubleshooting guides, content-mod issue classes, peer fix-mod catalogs)  
**Law:** `SOUL.md` — mapping does **not** authorize stubs. Implementation still requires root cause + complete FeatureUnit.  
**Companions:** `ECOSYSTEM_MAP.md` (OmniFix-oriented FC/D atlas) · `SSOT.md` (shipped) · `BACKLOG.md` · `RESEARCH_MASTER.md` · `COMPAT_MATRIX.md`

### Sibling platform (not this repo)

**Omni-Framework** lives at `C:\WPAI\Gaming\Minecraft\Omni_Framework` (launcher, loader, meta, intelligence corpus). It is the broader platform stack; **OmniFix** is the Forge 1.20.1 fix/perf sink.  
**From this repository: read-only.** Do not edit, reformat, or “sync into” Omni_Framework unless the director explicitly works in that tree.

---

## 0. How to read this corpus

| Symbol | Meaning |
|---|---|
| **PS-###** | Problem-space node (stable id for cross-ref) |
| **FC-*** | Failure class (shared with `ECOSYSTEM_MAP.md`) |
| **Layer** | Where the problem lives (engine / loader / content / ops / social) |
| **Edition** | JE (Java) · BE (Bedrock) · Both |
| **Loader** | Vanilla · Fabric · Quilt · Forge · NeoForge · Paper/Spigot/Bukkit · Hybrid |
| **Absorb?** | Whether a root-caused universal fix *could* live in a sink like OmniFix |
| **Status** | `mapped` · `partial` · `residual` · `out-of-scope` · `watch` |

**Corpus goal:** Make the *entire* problem space enumerable so no major failure mode is invisible.  
**Not a goal:** Claiming every cell is fixed, or shipping incomplete ports.

---

## 1. Product family topology

```
Minecraft
├── Java Edition (JE) ────────────────── primary modding universe
│   ├── Vanilla client/server
│   ├── Data packs / resource packs / transfer functions
│   ├── Mod loaders: Forge · Fabric · Quilt · NeoForge
│   ├── Hybrid bridges: Sinytra Connector, Architectury, MultiLoader
│   ├── Server stacks: Vanilla · Forge · Fabric · Paper/Purpur · Arclight/Mohist hybrids
│   └── Launcher surface: Official · Prism · MultiMC · CurseForge · Modrinth · ATLauncher
├── Bedrock Edition (BE) ─────────────── add-ons, Marketplace, Script API
│   ├── Limited “mods” vs JE depth
│   ├── Cross-play network (Xbox Live)
│   └── Geyser/Floodgate bridges ↔ JE servers
└── Shared brand problems: accounts, Realms, marketplace, ToS, version churn
```

### PS-001 Edition split (JE vs BE)

| Dimension | Java | Bedrock | Corpus implication |
|---|---|---|---|
| Engine | JVM, OpenGL (Vulkan experimental mods) | Native C++ multi-platform | Different crash/leak taxonomies |
| Mod depth | Near-total bytecode rewrite | Add-ons / scripts / Marketplace | **JE owns deep problem space** |
| Redstone / tech | Canonical technical community | Different timing/rules | Porting tech farms fails across editions |
| Performance baseline | Needs mods for modern FPS | “Efficient by default” narrative | JE fix/perf mod constellation exists |
| Cross-play | Limited (Geyser) | Native | Protocol translation = new FC class |

**Absorb?** JE: yes (OmniFix domain). BE: generally **out-of-scope** for OmniFix.  
**Sources:** Mojang edition articles; community comparisons (2025–2026).

---

## 2. Version eras (generation-shift pain)

Minecraft’s problem space is **version-sharded**. A fix valid on 1.12.2 is often wrong on 1.20.1.

| Era | Versions (approx) | Dominant PS nodes |
|---|---|---|
| **Classic–Beta** | pre-1.0 | World format, multiplayer protocols (historical) |
| **Legacy mod** | 1.7.10, 1.12.2 | Forge golden age; huge packs; old ASM/coremod culture |
| **Flattening** | 1.13–1.14 | Block/item IDs → namespaced; world upgrade DFU trauma |
| **Modern data** | 1.15–1.16 | Data packs mature; nether update; Biome/worldgen churn |
| **Caves & Cliffs** | 1.17–1.18 | World height, new gen, heavy DFU, chunk formats |
| **1.19–1.20** | 1.19.x–1.20.x | Deep dark, trails & tales; **peak kitchen-sink packs** (ATM, BMC) |
| **1.21+** | 1.21.x | NeoForge migration; trial chambers; continuous DFU |

### PS-002 Version skew / jar hell

- Wrong-version mods → Mixin target missing → boot crash  
- Soft incompat (loads, then CCE in rare path)  
- “Works on my machine” with different mapping channels (official / parchment / yarn)

### PS-003 DFU / world upgrade

- DataFixerUpper schema cost at boot and chunk load  
- Partial upgrades, missing block entities, legacy NBT  
- Modded world convert across major versions = high data-loss risk  
**Related shipped OmniFix:** `perf.dynamic_dfu`, structure DFU disk cache  

---

## 3. Loader & platform problem space

### PS-010 Forge (legacy + 1.20.1 line)

| Subproblem | FC | Notes |
|---|---|---|
| Mod discovery / scan annotation noise | FC-IO, FC-ALLOC | ModFileScan bloat |
| NightConfig TOML parse/watch | FC-CRASH, FC-IO | Corrupted configs, FileWatcher thrash |
| Registry freeze / ObjectHolders | FC-CRASH, FC-ALLOC | Deferred register races |
| Capability attach/order | FC-TICK, FC-LEAK | Cap retrieval order |
| Mixin + coremod coexistence | FC-SEAM | Priority wars |
| Login handshake payload drip | FC-TIMEOUT | Large packs |
| Config loading on overlay render | FC-CRASH | “Rendering overlay” + ConfigLoadingException |

### PS-011 Fabric

| Subproblem | FC | Notes |
|---|---|---|
| Fabric API / FRAPI / Sodium Indium chain | FC-SEAM | Render API fragmentation |
| Entrypoint failure | FC-CRASH | Missing dependency hard fail |
| Mixin ecosystem maturity | FC-SEAM | Generally cleaner than Forge historical coremods |
| Port of Lithium/Sodium ecosystem | — | Reference optimization surface |

### PS-012 Quilt

- Fabric-adjacent; smaller ecosystem; dual-support burden for mod authors  
- Problem: **orphan versions** when Fabric moves and Quilt lags

### PS-013 NeoForge

- Forge fork/migration (1.20.2+ focus; 1.21 packs)  
- Port pain: mods half-migrated; API renames; pack makers stuck on 1.20.1 Forge longer  

### PS-014 Paper / Spigot / Purpur (plugin servers)

| Subproblem | FC | Notes |
|---|---|---|
| Plugin API vs NMS | FC-SEAM | Version-bound NMS |
| Async chunk / entity schedulers | FC-TICK | Behavior differences vs vanilla |
| Hybrid servers (Mohist, Arclight) | FC-SEAM, FC-CRASH | Mods + plugins double surface |
| Timings/Spark profiling culture | FC-TICK | Ops diagnostics |

### PS-015 Hybrid / connector

- **Sinytra Connector** (Fabric→Forge): incomplete ports, mixin double-apply  
- **Architectury / MultiLoader**: abstraction tax, diverging APIs  
- **Geyser/Floodgate**: protocol translation, auth, inventory desync  

---

## 4. Vanilla Java Edition — engine problem taxonomy

Grouped by Mojira-adjacent classes + technical community consensus (not exhaustive ticket dump).

### PS-100 Correctness / game rules (Band A)

| Class | Examples | FC |
|---|---|---|
| Spectator / gamemode edge | stuck overlays, break anim, projectiles | FC-CRASH soft / correctness |
| Combat / items | shield sounds, crossbow offhand, mending progress | correctness |
| Entity state | fence escape, jockey persist, group AI death | correctness |
| Platform input | Mac Ctrl+Q, Linux chat ‘t’, macOS sprint | correctness |
| UI / inventory | drag stacks invisible, hotbar respawn, tab padding | correctness / cosmetic |
| Telemetry / privacy | cannot disable telemetry | FC-POLICY |

**Debugify** = catalog of Mojira patches. OmniFix absorbs many on 1.20.1.

### PS-110 Performance — client FPS / stutter

| Class | Symptoms | Peer surface (1.20.x) |
|---|---|---|
| Chunk meshing / rebuild | FPS drops while exploring | Sodium/Embeddium/Rubidium |
| Entity/BE render | farms, chests, signs | Entity Culling, EBE, FastChest |
| Immediate-mode UI render | inventory lag | ImmediatelyFast |
| Particles | campfire farms, potions | Particle Core / Blocker |
| Leaves / foliage | forest FPS | Cull Less Leaves, More Culling |
| Shaders | Oculus/Iris stacks | FC-SEAM with portals/cull |
| GPU resource leaks | VRAM climb, GL_OUT_OF_MEMORY | Fix GPU Memory Leak (ARR) |
| Background FPS | unfocused window waste | Dynamic FPS |
| Render distance | load cost vs FPS | Better FPS Render Distance (ARR) |

**Official help center** frames crashes/lag as RAM, drivers, network, installations — necessary but **incomplete** vs modded root causes.

### PS-120 Performance — server TPS / MSPT

| Class | Symptoms | Peer surface |
|---|---|---|
| Entity AI / pathing | dense mobs, villagers | Lithium/Canary/Radium, AI Improvements (ARR) |
| Hoppers / item entities | item funnels, farms | Canary-class, Clumps, Get It Together Drops |
| Redstone dust | clocks, farms | Alternate Current |
| Random ticks / chunk ticks | wide loaded areas | Dynamic View, Limited Chunkloading |
| Mob spawn / despawn | farms, pickup-mobs | Let Me Despawn |
| Block entities idle | furnaces, hoppers, sculk | Lithium-class / OmniFix idle units |
| Light engine | block place lag | Starlight (rewrite risk) |
| Chunk load/unload | travel stutter | C2ME, Better Chunk Loading (ARR) |
| World save spikes | “Saving world…” hitch | Smooth Chunk Save, Fast Async World Save (ARR) |
| Structure locate | `/locate` lag | Structure Essentials (ARR), MF structure opts |

### PS-130 Memory

| Class | Symptoms | Peers |
|---|---|---|
| Blockstate/model memory | huge packs OOM | FerriteCore |
| Soft leaks (static maps) | RAM climb over hours | AllTheLeaks, MemoryLeakFix |
| DFU / structure caches | boot RAM | LazyDFU, MF DFU |
| GPU VRAM | crash / black textures | GPU leak mods |
| Recipe/tag graphs | join RAM | Recipe Essentials (ARR) |

### PS-140 Networking

| Class | Symptoms | Peers |
|---|---|---|
| Login timeout | kitchen-sink join kick | Connectivity-class |
| Read / keep-alive timeout | AFK kick, lag kick | Connectivity-class |
| Packet size / compression | DecoderException | Connectivity, Krypton |
| Recipe/tag/advancement sync | disconnect on join | Recipe Essentials, Forge splitter |
| Ghost blocks | client/server block mismatch | Connectivity (ARR multi-path) |
| High playercount net | bandwidth, tracking | VMP, Raknetify (experimental) |

### PS-150 Worldgen / chunks / IO

| Class | Notes |
|---|---|
| Noise/surface allocation | gen GC pressure |
| Structure placement | ring searches, strongholds |
| Protochunk / ticket thrash | spawn chunks, portal tickets |
| Region file IO | save storms, corruption risk |
| Pregen tooling | Chunky, Chunk-Pregenerator (ops domain) |

### PS-160 Concurrency / threading

| Class | Notes |
|---|---|
| Registry/tag CME | async reload vs main |
| ModelData CME | client chunk threads |
| Level access off-thread | missing BE, wrong thread asserts |
| Watchdog hangs | infinite loops, deadlock |

---

## 5. Mod loader mechanics — meta problem space

### PS-200 Mixin / transform

| Node | FC | Signals |
|---|---|---|
| Target method missing | FC-CRASH | Version skew, intermediary mismatch |
| Inject cancel wars | FC-SEAM | Two mods `@Inject` same point |
| MixinExtras / MixinSquared needs | FC-SEAM | Must bootstrap shaded libs |
| InjectorGroupInfo leaks | FC-ALLOC | MF/OmniFix injector group patch |
| ClassInfo retention | FC-ALLOC | Optional clear after launch |
| Mixin config invalid | FC-CRASH | CrashDetector “Broken SpongeMixin Configs” |

### PS-210 Config systems

| Node | FC | Signals |
|---|---|---|
| NightConfig parse failure | FC-CRASH | `ParsingException`, corrupted TOML |
| FileWatcher thrash | FC-IO, FC-TICK | config reload storms |
| Serverconfig vs config | FC-CRASH | world/serverconfig vs .minecraft/config |
| Forge ConfigLoadingException on overlay | FC-CRASH | “Rendering overlay” |

### PS-220 Dependency / versioning

| Node | Notes |
|---|---|
| Missing library mods | Cloth, Architectury, Kotlin for Forge, GeckoLib |
| Duplicate mods | two Sodium forks, Embeddium+Rubidium |
| SemVer lies | “1.20” jar on 1.20.1 breaking change |
| JPMS / illegal access | modern Java + old mods |

### PS-230 Launch / JVM / native

| Node | Signals (CrashDetector-class) |
|---|---|
| Bad GPU drivers | NVIDIA/AMD/Intel native stacks, GLFW errors |
| OpenGL unsupported | old GPU phrases |
| Invalid GC flags | user JVM args |
| OOM | heap / metaspace / direct buffer |
| Early window issues | LWJGL monitor init |
| Java version wrong | class file major version |

**Ops reality:** RAM allocation is the most common *user-facing* root of lag/crash, but **not** the deepest engineering root in large packs.  

---

## 6. Fix / performance peer constellation (problem-space indexes)

These mods are **problem catalogs** — each exists because a PS node is real.

### PS-300 Universal correctness / boot / memory sinks

| Peer | Loader focus | Primary PS nodes | License note |
|---|---|---|---|
| **ModernFix** | Forge/Fabric | Boot, DFU, memory, bugfix, Band D | LGPL |
| **Debugify** | Multi | Mojira vanilla | Check per version |
| **AllTheLeaks** | Forge | Per-mod static leaks | MIT |
| **MemoryLeakFix** | Multi | Vanilla leaks | LGPL |
| **FerriteCore** | Multi | Blockstate memory | MIT |
| **Neruina** | Multi | Soft-crash entity quarantine | Ops safety |

### PS-310 Tick / AI / physics (Lithium family)

| Peer | Notes |
|---|---|
| **Lithium** (Fabric) | Behavior-preserving multi-system opt |
| **Canary** / **Radium Reforged** | Forge Lithium ports |
| **AI Improvements** | ARR; AI cost |
| **ServerCore** | Fabric multiplayer features + opt |
| **VMP** | High playercount |

### PS-320 Render family

| Peer | Notes |
|---|---|
| **Sodium** / **Embeddium** / **Rubidium** | Renderer replacement |
| **Iris** / **Oculus** | Shaders on Sodium/Embeddium |
| **Indium** | FRAPI on Sodium |
| **Nvidium** | NVIDIA backend (Sodium) |
| **VulkanMod** | Vulkan rewrite (incompat heavy) |
| **ImmediatelyFast** | Immediate-mode batching |
| **Entity Culling** / **More Culling** | Visibility |
| **Canvas** | Alternate Fabric renderer |

### PS-330 Net / join / packets

| Peer | Notes |
|---|---|
| **Connectivity** | ARR; timeouts, size, ghost blocks |
| **Krypton** / **Krypton Reforged** | Net stack |
| **Recipe Essentials** | ARR; recipe packet/cache |
| **Raknetify** | Experimental transport |

### PS-340 Chunk / world ops

| Peer | Notes |
|---|---|
| **C2ME** | Concurrent chunk management |
| **Starlight** | Light engine rewrite |
| **Chunky** / **Chunk-Pregenerator** | Pregen tools |
| **Dynamic View** | TPS ↔ view distance |
| **Smooth/Fast Async World Save** | ARR save spikes |
| **Better Chunk Loading** / **Chunk Sending** | ARR load/send |
| **Ksyxis** / **Fastload** | Join chunk skip (behavior risk) |

### PS-350 UsefulMods Performance120 as PS index

UsefulMods lists for **Fabric/Forge/NeoForge/Quilt 1.20.x** are a **compressed map of the industry’s agreed problem surface**: AI, redstone, chunks, net, memory, render, particles, pregen, recipe sync, GPU leak, entity cull, etc.  
Each row = at least one real PS node + a contested solution + license constraint.

---

## 7. Content-mod pillars (ecosystem “weather systems”)

Content mods create **new** problem weather that vanilla never had.

### PS-400 Create / contraptions

| Weather | FC | Notes |
|---|---|---|
| Contraption entity load | FC-TICK | Trains, elevators |
| Kinetics / stress networks | FC-TICK | Large factories |
| Portal dimension tracks | FC-SEAM | Create×IP |
| ExtendoGrip / statics | FC-LEAK | ATL-class |
| Ponder / client render | FC-ALLOC | Join/UI |

### PS-410 Valkyrien Skies / physics ships

| Weather | FC |
|---|---|
| Physics thread vs game thread | FC-SEAM, FC-CRASH |
| Ship chunk / unload CCE with portals | FC-CRASH |
| Frustum / camera inject collision | FC-SEAM |
| Raycast / clip on ships | correctness + FC-SEAM |
| Clockwork × MF render clash | FC-SEAM residual |

### PS-420 Immersive Portals / multidimensional

| Weather | FC |
|---|---|
| Nested render passes | FC-SEAM, FC-TICK |
| Fog / RD coupling with Embeddium | FC-SEAM |
| Entity drag / transit steal | correctness |
| Oculus/Iris nested shaders | FC-SEAM hard residual |
| Dim-blind packets | FC-DESYNC |

### PS-430 Applied Energistics 2 / logistics

| Weather | FC |
|---|---|
| Grid network ticks | FC-TICK |
| Channel/cable graphs | FC-TICK, FC-ALLOC |
| Terminal / crafting CPU | FC-TICK |
| Wireless terminal leaks | FC-LEAK |

### PS-440 Mekanism / IE / PNC / Thermal (machines)

| Weather | FC |
|---|---|
| Cable/pipe networks | FC-TICK |
| Multiblock validation | FC-TICK, FC-CRASH |
| Chemical/fluid tanks | FC-LEAK, FC-IO |
| Radiation / heat sims | FC-TICK |

### PS-450 Magic (Ars, Iron’s, Botania-class)

| Weather | FC |
|---|---|
| Spell entity spam | FC-TICK |
| Cap revive / clone | FC-LEAK |
| Client FX | FC-TICK client |

### PS-460 Colonies / NPC sims (MineColonies, etc.)

| Weather | FC |
|---|---|
| Pathfinding storms | FC-TICK |
| Building AI | FC-TICK |
| Boot init order | FC-CRASH |
| JEI entity caches | FC-LEAK |

### PS-470 Storage / JEI / EMI / FTB / Sophisticated

| Weather | FC |
|---|---|
| Recipe UI graphs | FC-ALLOC, FC-TICK |
| History / transfer button maps | FC-LEAK |
| Nested GUIs | FC-LEAK |
| Config TOML | FC-CRASH |

### PS-480 Dimensions / exploration (TF, Aether, Blue Skies, Ad Astra, …)

| Weather | FC |
|---|---|
| Dim registration / DFU | FC-CRASH |
| Client maps holding Level | FC-LEAK |
| Portal seams | FC-SEAM |
| Worldgen cost | FC-TICK, FC-ALLOC |

### PS-490 Pokemon / creature stacks (Cobblemon-class)

| Weather | FC |
|---|---|
| Custom entity tick load | FC-TICK |
| Party/UI client state | FC-LEAK |
| Spawn/battle systems | FC-TICK, FC-CRASH |

### PS-495 Library / glue mods

Architectury, Cloth, Balm, GeckoLib, Kotlin for Forge, Moonlight, Curios, Patchouli, Jade/TOP, Spark — **force multipliers**: a bug in a library becomes a **pack-wide weather system**.

---

## 8. Multiplayer & ops problem space

### PS-500 Server ops

| Node | Notes |
|---|---|
| TPS / MSPT SLOs | Spark, Timings, Dynmap lag |
| Backup / region corruption | IO storms during save |
| View distance vs simulation distance | Java 1.18+ knobs |
| Entity activation / activation range plugins | Behavior tradeoffs |
| Anti-xray / security plugins | Extra chunk transforms |
| Proxy (Velocity/Bungee) | UUID/forwarding auth |
| LuckPerms / perm plugins | load failures (CrashDetector) |
| Long tick freeze | watchdog |

### PS-510 Client multiplayer

| Node | Notes |
|---|---|
| Resource pack enforce | join stall |
| Server brand mismatch | limited |
| High ping interaction desync | ghost blocks class |
| Replay / freecam mods | inject surface |

### PS-520 Realms / official multiplayer

- Restricted mod surface  
- Different backup/ops constraints  
- Mostly **out-of-scope** for deep mixins  

---

## 9. Client UX / policy / social problem space

### PS-600 UX

| Node | Notes |
|---|---|
| Loading screens never dismiss | force-close class |
| Experimental world screen spam | MF experimental flag |
| Narrator Linux stack traces | policy quiet |
| Chat reporting / signing | No Chat Reports / MF remove signing |
| Telemetry | privacy FC-POLICY |
| Log spam drowning real errors | Log Begone |

### PS-610 Social / marketplace

| Node | Notes |
|---|---|
| CurseForge / Modrinth malware rare events | supply chain |
| Stolen Bedrock marketplace content | BE |
| Pack piracy / redistributed jars | license |
| Support burden externalized to Discord | ops |

---

## 10. Cross-cutting failure classes (master FC index)

| Code | Name | Primary layers | Example corpus nodes |
|---|---|---|---|
| **FC-CRASH** | Hard exception / exit | All | Mixin miss, NightConfig, NPE |
| **FC-SOFT** | Soft-lock / hung join | Client/server | Infinite load, deadlock |
| **FC-CME** | Concurrency races | Loader, client threads | Registry, ModelData |
| **FC-LEAK** | Retained references | Content, client | Static Level maps |
| **FC-TIMEOUT** | Net timeouts | Net | Login 30s, keep-alive |
| **FC-ALLOC** | Hot alloc / RAM | Engine, models | enum.values, state maps |
| **FC-TICK** | Wasteful ticking | AI, BE, farms | Idle furnace, goal scans |
| **FC-IO** | Disk / parse thrash | Boot, save | Zip scan, TOML watch |
| **FC-DESYNC** | Client≠server | Net, prediction | Ghost blocks |
| **FC-SEAM** | Multi-mod inject | Content stacks | VS×IP, Sodium×mod |
| **FC-MEGA** | Engine rewrite required | Render, light, bakery | Starlight, dynamic resources |
| **FC-ARR** | Solution locked in ARR | Net, chunks | Connectivity ghosts |
| **FC-POLICY** | Intentional behavior | UX | Chat signing |
| **FC-DATA** | Save corruption / DFU | World | Partial upgrade |
| **FC-NATIVE** | Drivers / LWJGL / GPU | Client | GLFW, nvogl |
| **FC-OPS** | Misconfiguration | Packs | RAM, wrong Java, dupe mods |
| **FC-BRIDGE** | Hybrid translation | Connector/Geyser | Incomplete ports |

---

## 11. Diagnostic corpus (how problems present)

### PS-700 Crash report signatures (observed patterns)

| Signature / phrase | Likely PS |
|---|---|
| `Mixin apply failed` / missing target | PS-200 version skew |
| `ConfigLoadingException` + NightConfig `ParsingException` | PS-210 |
| `Rendering overlay` | Config or resource load during splash |
| `DecoderException` / `Payload may not be larger` | PS-140 |
| `java.lang.OutOfMemoryError` | PS-130 / FC-OPS RAM |
| `Ticking entity` / `Ticking block entity` | PS-120 content tick |
| `Exception ticking world` | PS-120 / PS-150 |
| Native `nvogl` / `atio6axx` / GLFW | PS-230 FC-NATIVE |
| `NoClassDefFoundError` library | PS-220 |
| Watchdog `A single server tick` | PS-500 long tick |

**CrashDetector / Crash Assistant** productize many of these as pattern matchers — evidence the problem space is **finite and enumerable**.

### PS-710 Profiling entry points

| Tool | Use |
|---|---|
| Spark | Server/client samplers |
| Observable / LagMap | Region lag |
| Vanilla `/debug` | Limited |
| Mixin export / `-Dmixin.debug` | Inject conflicts |
| JFR / async-profiler | Deep JVM |

---

## 12. License & absorbability constraints (corpus law)

| Constraint | Implication |
|---|---|
| **ARR peers** (Connectivity, many someaddon, AI Improvements, …) | Reimplement from vanilla contracts only; never copy |
| **LGPL/MIT peers** (MF, Lithium, FerriteCore, …) | Reimplementation preferred; license-aware if porting structure |
| **Engine rewrites** | Treat as FC-MEGA; not FeatureUnit-sized |
| **Behavior changes** | Must be FeatureUnit-gated + documented tradeoff |

This is why OmniFix’s **coexistence skip** exists: peers are catalogs, not fences, but double-apply is itself a PS node (FC-SEAM).

---

## 13. OmniFix coverage projection (JE Forge 1.20.1)

| Corpus region | Coverage (approx) | Notes |
|---|---|---|
| PS-100 Mojira high-value | **High** | ~57 vanilla units |
| PS-010 Forge core / NightConfig / scan | **High** | MF-class ports |
| PS-130 soft leaks ATL #3 | **High** | 50 leak units |
| PS-140 timeouts/size | **Medium** | 5 net units; ghost residual |
| PS-120 idle BE / AI throttle | **Growing** | Ongoing waves |
| PS-310 Lithium full suite | **Partial** | Selective independent ports |
| PS-320 renderer rewrite | **Out-of-scope** | Coexist with Embeddium |
| PS-410/420 VS×IP / Create×IP | **Exemplar high** | Seam program |
| PS-430–490 content pillars | **Leak-biased** | Tick systems mostly residual |
| PS-230 native drivers | **Out-of-scope** | Ops/driver |
| PS-014 Paper plugins | **Out-of-scope** | Different platform |
| BE edition | **Out-of-scope** | |

Authoritative shipped inventory: `SSOT.md` / `FeatureUnits.java` (**367** as of 2026-07-21).

---

## 14. Research program — expand this corpus

### P-Corpus continuous intake

| Cadence | Sources |
|---|---|
| Weekly | Mojira trending; MF/ATL/Debugify releases |
| Weekly | Create / VS / IP / Embeddium / Oculus issue trackers |
| Weekly | UsefulMods list diffs; Fabric Lithium/Sodium changelogs |
| Continuous | Crash Assistant / CrashDetector pattern lists |
| Continuous | ATM / Better MC / Cobblemon / kitchen-sink crash labels |
| Continuous | User logs (any pack) → map to PS-### before implementing |

### Expansion backlog (documentation, not code)

1. Per-version Mojira heat maps (1.20.1 vs 1.21.x drift)  
2. NeoForge-only PS nodes  
3. Paper plugin FC index  
4. Full Create 6 / AE2 grid tick micro-atlas  
5. Shader pipeline state machine (Iris/Oculus × portals)  
6. Supply-chain / launcher PS nodes  
7. Quantitative MSPT attribution templates (Spark recipes)

---

## 15. Explicit non-goals of the corpus

- Replacing official Mojira  
- Guaranteeing FPS numbers  
- Endorsing ARR copy-paste  
- Treating Bedrock Marketplace as Java modding  
- “One mod ends all lag” marketing claims  

---

## 16. Source index (crawl v1)

| Source class | Examples used |
|---|---|
| Official | Minecraft Help Center crash/lag articles; Mojang JE vs BE article |
| Bug trackers | Mojira issue classes; GitHub issues on JEI/MF/peers |
| Aggregators | [UsefulMods Performance120](https://github.com/TheUsefulLists/UsefulMods/blob/main/Performance/Performance120.md) Fabric/Forge lists |
| Diagnostics products | CrashDetector CurseForge description (pattern catalog) |
| Forums | Forge support threads (NightConfig, world load, mixin) |
| Community | Reddit/YouTube pack troubleshooting (RAM, conflicts, OOM) |
| Edition discourse | Java vs Bedrock modding depth comparisons (2025–2026) |
| Local OmniFix research | `RESEARCH_MASTER.md`, `ECOSYSTEM_MAP.md`, ATL #3, Debugify archives |

Citations in this file are **source classes**, not a legal bibliography. Re-verify licenses before any absorb.

---

## 17. Document history

| Date | Change |
|---|---|
| 2026-07-21 | **v1.0** — Initial full-ecosystem corpus from internet crawl + existing OmniFix research. |
| 2026-07-21 | Note: sibling Omni-Framework path is reference-only; do not modify from OmniFix. |
| 2026-07-21 | Coverage pointer: FeatureUnits **303** → **311** (AI throttle wave-2). |
| 2026-07-21 | Coverage pointer: FeatureUnits **311** → **319** (AI/entity throttle wave-3). |
| 2026-07-21 | Coverage pointer: FeatureUnits **319** → **327** (entity-brain wave-4). |
| 2026-07-21 | Coverage pointer: FeatureUnits **327** → **335** (entity-brain wave-5). |
| 2026-07-21 | Coverage pointer: FeatureUnits **335** → **343** (entity-brain wave-6). |
| 2026-07-21 | Coverage pointer: FeatureUnits **343** → **351** (entity-brain wave-7). |
| 2026-07-21 | Coverage pointer: FeatureUnits **351** → **359** (entity-brain wave-8). |
| 2026-07-21 | Coverage pointer: FeatureUnits **359** → **367** (entity-brain wave-9). |

---

*When a new failure mode is discovered in the wild, assign a **PS-###** (or sub-id), map FC + layer + absorbability, then either implement under SOUL or mark residual. Mapping is progress. Shipping incomplete code is not.*
