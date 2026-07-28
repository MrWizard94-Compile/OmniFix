# OmniFix Coding Agents

Project-specialized Grok agents for Forge 1.20.1 OmniFix work.  
Definitions: `.grok/agents/*.md` · Roles: `.grok/roles/*.toml` · Personas: `.grok/personas/*.toml`  
Manage in TUI: `/config-agents` (or `/agents`).

**Law:** `SOUL.md` · **Sibling platform (read-only):** `C:\WPAI\Gaming\Minecraft\Omni_Framework`

---

## Agent catalog

| `subagent_type` | Capability | Mission |
|---|---|---|
| **omnifix-feature-unit** | all | End-to-end FeatureUnit (root cause → mixin → wire → SSOT) |
| **omnifix-mixin-only** | read-write | Parallel-safe: write **only** assigned mixin `.java` (parent pre-wires rest) |
| **omnifix-rootcause** | read-only | Symptom → method → proposed unit id / residual |
| **omnifix-seam-compat** | all | VS×IP, Create×IP, Clockwork, inject-collision seams |
| **omnifix-leak-port** | all | `leak.*` ATL-class soft clear / instance track |
| **omnifix-docs-sync** | all | SSOT/BACKLOG/CORPUS counts match `FeatureUnits.java` |
| **omnifix-build-verify** | all | Gradle compile/reobf + minimal compile fixes |
| **omnifix-soul-review** | read-only | SOUL compliance verdict (PASS / FAIL) |
| **omnifix-ai-throttle** | all | Goal/Sensor AI throttles & interval bumps only |
| **omnifix-entity-idle** | all | Idle BE/entity tick skips & empty-scan caches |
| **omnifix-entity-brain** | all | Brain sensors, nested goals (Bee$*), target search volumes |

Built-ins still available: `general-purpose`, `explore`, `plan`.

---

## Personas (overlays)

| Persona | When |
|---|---|
| `omnifix-parallel-wave` | Multi-agent mixin waves; exclusive ownership |
| `omnifix-soul-strict` | Maximum anti-stub / anti-ARR strictness |

---

## Parallel wave playbook (parent)

```text
1. omnifix-rootcause     → candidate list (absorb now vs residual)
2. Parent pre-wires      → FeatureUnits + OmniFixMixinPlugin + omnifix.*.mixins.json
3. N × omnifix-mixin-only → one mixin file each (non-overlapping paths)
4. omnifix-build-verify  → compile + reobf
5. omnifix-docs-sync     → SSOT totals
6. omnifix-soul-review   → optional gate
```

### Pre-wire template (parent)

```java
// FeatureUnits.java
public static final String PERF_EXAMPLE = "perf.example";
// registerBuiltins: FeatureUnitRegistry.register(new FeatureUnit(PERF_EXAMPLE, "...", "...", true));
```

```java
// OmniFixMixinPlugin.featureUnitForMixin
if (mixinClassName.contains("ExampleMixin")) return FeatureUnits.PERF_EXAMPLE;
```

```json
// omnifix.perf.mixins.json mixins array
"ExampleMixin"
```

Agent prompt must state: **absolute mixin path**, **unit id**, **pattern sibling**, **forbidden paths**.

---

## Spawn examples

```text
subagent_type: omnifix-mixin-only
description: Furnace idle skip mixin
prompt: |
  Create ONLY omnifix-forge/src/main/java/org/omnifix/mixin/perf/ExampleMixin.java
  Unit: perf.example (already registered). Pattern: FurnaceIdleMixin.
  Do not edit FeatureUnits, plugin, JSON, or Omni_Framework.
```

```text
subagent_type: omnifix-feature-unit
description: Ship beehive empty skip
prompt: |
  Root-cause idle BeehiveBlockEntity.serverTick and ship complete FeatureUnit
  following SOUL + SSOT update. Build must pass.
```

---

## Document map

| Doc | Role |
|---|---|
| `SOUL.md` | Constitution |
| `SSOT.md` | Architecture + shipped inventory |
| `CORPUS.md` | Full Minecraft problem-space atlas |
| `ECOSYSTEM_MAP.md` | OmniFix FC/D projection |
| `BACKLOG.md` | Status matrix |
| This file | Agent ops catalog |

---

## History

| Date | Change |
|---|---|
| 2026-07-21 | Initial 8 specialized coding agents + 2 personas + parallel-wave playbook. |
