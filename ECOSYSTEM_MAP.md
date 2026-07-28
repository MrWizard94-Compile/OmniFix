# OmniFix — Forge 1.20.1 Ecosystem Problem-Space Map

**Role:** Living atlas of *what can go wrong* in the Forge 1.20.1 mod ecosystem and how OmniFix classifies, absorbs, or defers it.  
**Law:** `SOUL.md` — symptom → root cause → complete gated FeatureUnit. No stubs. Peer mods are catalogs, not fences.  
**Companion docs:** `CORPUS.md` (**entire** Minecraft + modding problem-space atlas) · `SSOT.md` (shipped inventory + architecture) · `BACKLOG.md` (status matrix) · `RESEARCH_MASTER.md` (intake pipeline) · `COMPAT_MATRIX.md` (coexistence).

> OmniFix-specific FC/D tables live here. The **global** map of Minecraft as a product + all loaders/content/ops is in **`CORPUS.md`**.

**Survey posture:** We are not “a fix mod that copies other fix mods.” We are **mapping the entire problem space** of Forge 1.20.1 heavy packs so every failure class is either **shipped**, **explicitly residual**, or **queued with a research contract**.

---

## 1. Problem-space dimensions

| Axis | Values | Notes |
|---|---|---|
| **Correctness** | Crash · soft lock · desync · data loss · wrong game rules | Band A first |
| **Liveness** | TPS stall · tick spike · hung watchdog · login freeze | Server + integrated |
| **Memory** | Leak (world/player/static) · retained soft caches · palette bloat | ATL syllabus + MF |
| **I/O & boot** | Jar scan · NightConfig · DFU · recipe/loot parse · texture stitch | Join-time dominant |
| **Network** | Timeout · payload size · compression · recipe graph · ghost blocks | Connectivity-class |
| **AI / entities** | Goal scans · sensors · path · merge · idle BE · spawners | Dense farms |
| **Worldgen** | Structure locate · surface rules · protochunks · spawn tickets | Explore cost |
| **Render / client** | Frustum · fog · cull · GL · nested portal pass · model bake | Stack-dependent |
| **Seams** | VS×IP · Create×IP · MF×Clockwork · Oculus×IP · dim-blind packets | Multi-mod inject |
| **Policy** | Chat signing · telemetry · narrator · branding | Feature units |

---

## 2. Failure-class taxonomy (FC)

| Code | Class | Typical sources | OmniFix posture |
|---|---|---|---|
| **FC-CRASH** | Exception / CCE / NPE in common path | MF bugfix, seams, missing BE | Absorb when root-caused |
| **FC-CME** | ConcurrentModification / races | Registry tags, ModelData, CTM | MF ports + guards |
| **FC-LEAK** | Static / cap / world map retention | ATL #3, FakePlayer, client leave | Soft clear + instance track |
| **FC-TIMEOUT** | Login / read / keep-alive kicks | Connectivity-class | Constant floors/ceilings |
| **FC-ALLOC** | Hot-path garbage / enum.values | State cache, models, ingredients | Cache/dedup/intern |
| **FC-TICK** | Wasteful per-tick work when idle | Furnace, hopper, AI, signs | Idle skip / rate bump |
| **FC-IO** | Disk/zip/DFU/reload thrash | Zip index, DFU, NightConfig | Index/throttle/lazy |
| **FC-DESYNC** | Client/server state mismatch | Ghost blocks, recipe book | Residual if multi-path |
| **FC-SEAM** | Two mods inject same target | VS frustum, Create tracks | MixinSquared / order |
| **FC-MEGA** | Requires rewrite of bakery/engine | dynamic_resources, Starlight | Defer deliberate |
| **FC-ARR** | Only known solution is ARR copy | Connectivity ghosts | Independent redesign only |
| **FC-POLICY** | Intentional behavior change | Chat signing, telemetry | Feature-gated |

---

## 3. Domain map (expanded D1–D12)

| Domain | Scope | Catalog peers | Shipped depth | Residual |
|---|---|---|---|---|
| **D1 Vanilla Mojira** | Debugify-class correctness | Debugify | **High** (57 units) | Cosmetic / no-contract tickets |
| **D2 Forge core** | FakePlayer, registry CME, handshake, caps, ObjectHolders | MF, Forge issues | **High** | ASM capability dispatcher |
| **D3 Network** | Timeouts, compression, payload | Connectivity (ARR) | **Medium** (5 units) | Ghost blocks, recipe protocol |
| **D4 Leaks** | ATL #3 soft+instance track | AllTheLeaks | **Complete** for #3 syllabus | Future ATL rows |
| **D5 Render / GPU** | Embeddium, Oculus, IF, EMF/ETF, GL | GPU Leak (ARR), cull mods | **Low–medium** | GPU lifecycle, RSM cancel |
| **D6 Content seams** | Create, VS, IP, AE2, Mek, Ars, Colonies, FTB, JEI/EMI, Cobblemon | Issue trackers | **Partial** (VS×IP, Create×IP, Clockwork clip) | Cobblemon, Mek, AE2 tick classes |
| **D7 Worldgen / structure** | Locate, DFU, spawn tickets, surface | MF Band D | **High** | Prefetch helper mega |
| **D8 Mixins / load** | Injector groups, ClassInfo, scan, manifest | MF | **High** | — |
| **D9 AI / mobs** | Goals, sensors, path, target, avoid, stroll | AI Improvements (ARR), Canary | **Growing** | Full Lithium AI suite |
| **D10 Block entities** | Idle furnace/hopper/chest/sign/bell/… | Canary/Lithium class | **Growing** | Comparator/redstone turbo |
| **D11 Boot / join** | NightConfig, scan, DFU, stitch, recipes | MF | **High** | dynamic_resources mega |
| **D12 Pack / server ops** | RCON, pregen, chunk send, view distance | Chunky, Dynamic View, etc. | **Low** | Tooling peers own domain |

---

## 4. Peer constellation (absorb surface)

### 4.1 Fix / util peers

| Peer | License posture | Primary FC | Absorb status |
|---|---|---|---|
| ModernFix | LGPL — reimplement | CRASH, ALLOC, IO, TICK | **Majority shipped**; mega residual |
| Debugify | Check — reimplement Mojira | CRASH / correctness | **Majority shipped**; skip if present |
| AllTheLeaks | MIT — reimplement patterns | LEAK | **#3 syllabus complete** |
| Connectivity | **ARR** — no source copy | TIMEOUT, DESYNC | Size/timeouts **shipped**; ghost **residual** |
| Recipe Essentials | **ARR** | DESYNC / join | Size side covered; protocol residual |
| FerriteCore | MIT | ALLOC | Partial (`fake_state_map`) |
| Canary / Radium | LGPL | TICK, ALLOC | Selective independent ports |
| MemoryLeakFix | LGPL | LEAK | Overlap with ATL/vanilla |
| Fix GPU Memory Leak | **ARR** | LEAK (GL) | Research |
| AI Improvements | **ARR** | TICK (AI) | Rate-bump class only |
| Clumps / LMD | MIT/LGPL | TICK | Overlap with XP/item merge caches |
| Krypton Reforged | MIT | TIMEOUT | Careful / fragile on Forge |
| Starlight | MIT | TICK (light) | Defer engine rewrite |
| ImmediatelyFast / Entity Culling | various | Render | Coexist; fix collisions only |

### 4.2 Content pillars (problem classes, not “ports”)

| Pillar | Dominant FC | Example symptoms | Map status |
|---|---|---|---|
| **Create 6** | SEAM, TICK, LEAK | ExtendoGrip leak, portal tracks, train transit, RaycastHelper | Partial shipped |
| **Valkyrien Skies + Clockwork** | SEAM, CRASH | Frustum loop, ship unload CCE, clip, MF clash | High for VS×IP; clash residual |
| **Immersive Portals** | SEAM, DESYNC, Render | Nested fog, entity drag, Oculus | Partial; Oculus hard defer |
| **AE2 / Applied** | TICK, LEAK | Grid ticks, terminal maps | Leak AE2WT shipped; grid residual |
| **Mekanism / IE / PNC / Thermal** | TICK, LEAK | Cable networks, machine ticks | Leak PNC shipped; network residual |
| **Ars / Iron’s / magic** | LEAK, TICK | Cap revive, spell data | Leak ports shipped |
| **MineColonies** | CRASH, TICK, LEAK | Boot init, colony ticks, JEI entities | Leak JEI entities; boot residual |
| **FTB / Sophisticated / storage** | LEAK, IO | GUI screens, configs | FTB library leak shipped |
| **JEI / EMI** | LEAK, ALLOC | Transfer buttons, history, stacks | Leak shipped |
| **GeckoLib / EMF / ETF** | LEAK, Render | Molang, held entity | Leak shipped |
| **Cobblemon** | TICK, CRASH | Pokémon entity tick load | Research |
| **Twilight / Aether / Blue Skies / Ad Astra** | LEAK, SEAM | Dim maps, models | Leak ports for several |
| **Quark / Supplementaries / Moonlight** | CRASH, TICK | Shape caches, config | MF shape cache; Moonlight leak |

---

## 5. Coverage matrix (how to read progress)

| Layer | Meaning |
|---|---|
| **Mapped** | Failure class documented with FC code + domain + research note |
| **Root-caused** | Exact method/field contract known on 1.20.1 Forge |
| **Shipped** | FeatureUnit + mixin/helper + gate + SSOT row |
| **Coexist** | Skip when peer present |
| **Residual** | Explicitly not shipped with SOUL reason |
| **Watch** | Needs pack repro / version pin before root cause |

**North star metrics (living):**

| Metric | Value (see `SSOT.md`) |
|---|---|
| FeatureUnits shipped | **367** (authoritative: `FeatureUnits.java`) |
| ATL #3 leak syllabus | Complete |
| Mojira Band A high-value | Mostly complete |
| MF Band D mega | Residual by design |
| Ecosystem seams exemplar | VS×IP + Create×IP shipped |

---

## 6. Continuous intake (problem-space ops)

1. **Weekly:** Mojira still-open 1.20.1 · MF/ATL/Debugify releases · Create/VS/IP/Embeddium issues.  
2. **Pack signal:** ATM / Better MC / Cobblemon crash labels · Crash Assistant patterns · user logs.  
3. **Gate for new unit:** FC code + domain + root cause snippet + coexistence plan + SOUL completeness.  
4. **Reject:** FPS cargo-cult, ARR paste, unresearched bulk ports, mega-system stubs.

---

## 7. Explicit non-goals (until mapped + proven)

- Claiming “ends all lag” without measurements  
- Engine rewrites (Starlight-class, full model bakery) without dedicated program  
- Pack hygiene (duplicate jar deletion) as mixins  
- Fabric-only Lithium behavior without Forge 1.20.1 equivalence  
- Owning content-mod features (new blocks/items)

---

## 8. Document links

| Doc | Owns |
|---|---|
| `SOUL.md` | Immutable delivery law |
| `CORPUS.md` | **Entire** Minecraft + modding problem space (global) |
| `SSOT.md` | Architecture + full shipped catalog |
| `BACKLOG.md` | Row-level status |
| `RESEARCH_MASTER.md` | P0–P5 research pipeline history |
| `COMPAT_MATRIX.md` | Peer skip / order notes |
| **This file** | OmniFix-oriented FC/D atlas (Forge 1.20.1 projection of CORPUS) |

*Update this map when a new failure class, pillar, or residual reason is discovered — even if no FeatureUnit ships yet. Mapping is progress under the mandate.*
