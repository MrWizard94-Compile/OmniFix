# OmniFix Compat Matrix — Base Wars (Forge 1.20.1)

Survey date: 2026-07-05, against `C:\Users\Bulkl\curseforge\minecraft\Instances\Base wars\mods`
(last successful boot in `logs/latest.log`: 2026-07-03, clean session and shutdown).

**Method.** OmniFix ships compat *layers*: each one fixes a reproduced, root-caused interaction
between specific mods, gates itself on those mods being present, and degrades to a no-op everywhere
else. "Make everything compatible" is therefore a pipeline, not a single patch: symptom observed
in-game → root cause pinned (probes/decompile) → gated layer → in-game verify. This document is the
pipeline's intake: the full pack triaged into what's fixed, what's diagnosed, what's watched, and
what's noise.

---

## 1. The pack at a glance (~95 enabled jars)

| Group | Mods |
|---|---|
| Physics + portals (OmniFix core focus) | Valkyrien Skies 2.4.11, Clockwork 0.5.6, Trackwork 1.2.4, Immersive Portals 3.0.8 |
| Renderer | Embeddium 0.3.31, Oculus 1.8.0, ImmediatelyFast 1.5.4, EMF/ETF, AdLods |
| Performance | ModernFix 5.27.51, FerriteCore 6.0.1 |
| Create suite | Create 6.0.8 + Connected, Addition, Big Cannons (+CBC shells/AT, Ritchie's PL), Deco, Ender Transmission, Radar, Copycats, Steam 'n' Rails |
| Colony | MineColonies + BlockUI/Structurize/Domum/Multipiston, SmallColonies, TownTalk |
| Progression/scripting | FTB suite (Chunks/Quests/Teams/…), KubeJS (+Create), CraftTweaker, Lootr |
| World gen | Terralith, Tectonic, Lithostitched, Geophilic, Dynamic Trees (+Quark/Terralith), Moog's, Underground Village |
| QoL/content | Quark+Zeta, Moonlight (lib), Sophisticated Backpacks/Storage/Core, Curios, JEI, TOP, Better Combat, Comforts, Tombstone, SecurityCraft, FramedBlocks, MutantMonsters/More, EnhancedAI, … |
| User's own | nodecore, omni32_loader, valkyrienportals 1.1.0 (to be replaced by OmniFix) |

**Library provisioning (verified in boot log):**
- **MixinSquared 0.1.1** arrives jar-in-jar via **Moonlight Lib** (`META-INF/jars/`) — *not* via the
  disabled Supplementaries. OmniFix's frustum canceller has its runtime. If Moonlight ever leaves the
  pack, add a standalone MixinSquared jar (roadmap: have OmniFix jar-in-jar its own copy).
- **MixinExtras 0.5.0-beta.5** wins the jar-in-jar election (from EnhancedAI); `@WrapMethod`
  (needs ≥0.4) is safe.

---

## 2. Active OmniFix layers

### VS × IP layer (`omnifix-compat-valkyrien-portals`, ported from standalone v1.1.0 — complete as of 2026-07-05)
| Fix | Mechanism | Status |
|---|---|---|
| Boot crash: VS/IP `Frustum` mixin collision | MixinSquared canceller drops VS's redundant dead-loop mixin | Verified in standalone |
| Empty portal panes while riding a ship | `prepareCullFrustum` wrap: bypass VS in nested passes + self-calibrating ship-bank transfer | Verified in standalone |
| Fog-collapsed render distance through portals | Embeddium `getEffectiveRenderDistance` decoupled from host-pass fog during portal passes | Verified in standalone |
| CCE on every dimension transit | `@WrapMethod` guard on VS's ship-unload handler when IP owns the chunk source | Pending in-game verify |
| Remote-dimension ships invisible through portals | Server-side: `PortalShipVisibility` + tracker-gate relax + vanilla chunk-packet guards | Pending in-game verify |
| Ships can't fly through portals | `PortalShipTransit`: per-tick IP-portal transit with momentum rotation + disarm/re-arm | Pending in-game verify |
| Ships drag & break IP portals | `EntityDragger.isDraggable` excludes IP `Portal` entities | Pending in-game verify |

### Create × IP layer (`omnifix-compat-create-portals`, NEW 2026-07-05)
| Fix | Mechanism | Status |
|---|---|---|
| Train tracks don't pair through nether portals (leg a) | IP-aware `PortalTrackProvider` for `minecraft:nether_portal`: far side resolved via the overlapping IP `Portal` entity (`transformPoint` + rotation), Create's stock provider kept as fallback | Built; pending in-game verify |
| Tracks through IP's *block-less* general portals (leg b) | `TrackBlock.connectToPortal` RETURN mixin → `IpEntityPortalTrackCompat`: pairs via portal entity `transformPoint` + rotation; `updateShape` keeps portal tracks without a pane block | Built; pending in-game verify |
| Train entity transit through IP portals | IP must not teleport `CarriageContraptionEntity` or seated passengers (Create owns dimensional carriages); portal dismount uses `IPGlobal.serverTeleportationManager.teleportPlayer` | Built; pending in-game verify |

### VS × IP interaction
| Fix | Mechanism | Status |
|---|---|---|
| Cross-portal place/use broken with VS (VS #1525) | At `Minecraft.startUseItem` HEAD, when IP is portal-pointing, write the switched remote `hitResult` into VS `MinecraftDuck.originalCrosshairTarget` so VS's `useItemOn` wrap no longer substitutes the pre-portal local raycast | Built; pending in-game verify |

---

## 3. Watchlist — known seams in the enabled set (no OmniFix action yet)

| Pair | Risk | Notes |
|---|---|---|
| Oculus shaders × IP portals | Portals black / unshaded through panes | IP does not support shader pipelines in nested passes; large project, not a quick layer |
| Trackwork/Clockwork × Create 6.0.8 | VS addons historically pinned older Create | Watch for contraption/wheel breakage in-game; report symptoms |
| Create contraptions × IP portals | Non-train contraptions won't transit portals | Same family as tracks leg (b) |
| VS ships × MineColonies | Colonist AI/pathing on moving ships | Untested combination in this pack |
| FTB Chunks map × IP | Claim/minimap rendering across portal views | Cosmetic; low priority |
| 4 mods patching Embeddium `RenderSectionManager` (IP, VS, Oculus, OmniFix) | Boot-log warning; any Embeddium update can shift the seam | Keep Embeddium version pinned unless retesting |

## 4. Pack hygiene findings (from the survey — no code needed)

1. **Duplicate Copycats jars**: `copycats-3.0.7+mc.1.20.1-forge-10kg.jar` *and*
   `copycats-3.0.7+mc.1.20.1-forge.jar` are both enabled; FML deduped to one at boot, but which one
   wins is undefined. Delete one (keep the plain `-forge.jar` unless the `-10kg` variant is a
   deliberate custom build).
2. **Forgified Fabric API** (`fabric-api-0.92.6…`) is enabled while **Connector is disabled** — its
   usual consumer is gone. If nothing in the pack declares a fabric_api dependency, it's dead weight;
   if re-enabling Connector is planned, note that Connector × VS/IP/Embeddium is a known minefield.
3. **ImmediatelyFast jar is the 1.20.4 build** (`+1.20.4`): it initialized fine (multi-version jar),
   but consider pinning the `+1.20.1` artifact to remove a variable.
4. **Disabled-jar audit** (conflict trail): Supplementaries + Amendments (likely crash/conflict —
   retest post-OmniFix if wanted; MixinSquared no longer depends on it), Eureka (ship helms — replaced
   by Clockwork/Trackwork control?), Krypton Reforged (known Forge-network instability, keep off),
   Continuity (Embeddium version mismatch, keep off), Waystones (FTB Essentials overlap), Essential,
   Connector + Extras, immersive-portals 3.0.7 (superseded), assorted content mods.

---

## 5. Deployment (Base Wars)

1. Build: `gradlew :omnifix-forge:build` → `omnifix-forge/build/libs/omnifix-0.1.0-alpha.jar`.
2. In `Instances/Base wars/mods`: rename `valkyrienportals-1.1.0.jar` → `.disabled` (OmniFix carries
   all of its fixes; running both double-applies mixins and the canceller).
3. Drop in the OmniFix jar. MixinSquared arrives via Moonlight (see §1).
4. Verify checklist, in order:
   - Boot reaches title screen; log shows `[OmniFix] Profile: HEAVY_PHYSICS_PORTAL`, the VP layer
     active line, and (at load-complete) `[OmniFix/CreateIP] Nether-portal track pairing routed…`.
   - OW↔Nether transit: no `ClassCastException` spam.
   - Ship parked at nether-side portal mouth: visible from overworld through the pane
     (`[VP-VISIBILITY]` debug lines when enabled).
   - Fly a ship into a portal: single clean transit, no bounce (`[VP-TRANSIT] TELEPORTED …`).
   - Create track against each face of a lit nether portal, matching track on the far side: they
     pair (train can be sent through).

## 6. Roadmap

**Pack-specific, next in line**
1. In-game verify of the v1.1.0 server layer + nether/entity track pairing + VS #1525 interact.
2. In-game verify Create train entity transit (`create.ip_train_transit`); far-side train visibility (renderer) if still broken.
3. In-game verify wanderwand ship selection (`vs.clockwork_clip` / Create RaycastHelper → clipIncludeShips).
4. Create redstone links keyed on world `BlockPos` vs ship space (hardest; may not be layer-fixable).

**Universal ("commonly used mods") strategy** — grow OmniFix where its identity is: the
physics/portal/renderer seams no other mod owns. Highest-value candidates beyond this pack:
VS × (Create addons, MineColonies, SecurityCraft), IP × (FTB Chunks, Waystones, minimaps),
Create contraptions × IP. Generic perf/compat for popular mods (JEI, Curios, Sophisticated*, Quark)
is already owned by ModernFix/FerriteCore — OmniFix should not duplicate it. Each new pair enters
this matrix as a watchlist row first, then becomes a layer once a symptom is reproduced and
root-caused; OmniFix's kernel (StackDomain probes + per-mixin LoadingModList gating) already
supports shipping all layers in one jar safely.
