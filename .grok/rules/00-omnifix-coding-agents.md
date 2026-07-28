# OmniFix — Coding Agents

Specialized Grok agent types live in `.grok/agents/`. Spawn with `subagent_type` equal to the agent `name`.

| Agent | Use when |
|---|---|
| `omnifix-feature-unit` | Full unit: root cause → code → wire → SSOT |
| `omnifix-mixin-only` | Parallel wave; parent pre-wired registry/plugin/JSON |
| `omnifix-rootcause` | Research only (read-only) |
| `omnifix-seam-compat` | VS×IP / Create×IP / seam inject collisions |
| `omnifix-leak-port` | ATL-class `leak.*` units |
| `omnifix-docs-sync` | Align SSOT/BACKLOG counts with FeatureUnits.java |
| `omnifix-build-verify` | Compile/reobf and fix compile breaks only |
| `omnifix-soul-review` | SOUL compliance review (read-only) |
| `omnifix-ai-throttle` | Goal/Sensor AI throttle specialist |
| `omnifix-entity-idle` | Idle BE/entity tick skip specialist |

## Project invariants (all agents)
- Platform: MC **1.20.1** / Forge **47.x** / Java **17**
- Law: `SOUL.md`
- Inventory: `SSOT.md` · matrix: `BACKLOG.md` · corpus: `CORPUS.md`
- **Never edit** `C:\WPAI\Gaming\Minecraft\Omni_Framework`

## Parallel wave recipe
1. Parent: research + pre-wire FeatureUnits + plugin + mixin JSON.
2. Spawn N × `omnifix-mixin-only` with exclusive mixin paths.
3. Parent: `omnifix-build-verify` then `omnifix-docs-sync`.
4. Optional: `omnifix-soul-review`.

Catalog detail: `CODING_AGENTS.md` (repo root).
