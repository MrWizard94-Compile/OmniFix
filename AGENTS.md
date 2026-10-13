# AGENTS — OmniFix project entry

**Status:** Level 4 project instructions
**Canonical Constitution:** https://github.com/MrWizard94-Compile/Agents-Constitution
**Accepted source commit:** `7fb576e4330aed21eee8fa4a5e107eb8c0b76a89`

The accepted GitHub repository is the only editable WPAI Constitution source.
Load its `SKILL.md`, `references/always-load.md`, `AGENTS.md`, `SOP.md`, and
applicable `law/*` files from this verified commit. A generated mirror is
usable only when its manifest matches that SHA. `.claude/AGENTS.md` points
here for Claude compatibility; it is not a second source of law.

## Project mission and boundaries

OmniFix is a root-caused Forge 1.20.1 fix and performance mod. `SSOT.md` is
the living product architecture and shipped FeatureUnit inventory;
`BACKLOG.md`, `RESEARCH_MASTER.md`, `ECOSYSTEM_MAP.md`, `CORPUS.md`, and
`COMPAT_MATRIX.md` provide product-specific context. None is a substitute
for the WPAI Constitution. `CODING_AGENTS.md` and `.grok/` define optional
specialized agent workflows, not independent governance.

Use the versions declared by `gradle.properties` and the Gradle build:
Minecraft 1.20.1, Forge 47.4.20, and Java 17. Keep the sibling
`C:\WPAI\Gaming\Minecraft\Omni_Framework` read-only from this project.
Do not alter gameplay, mixin behavior, configs, worlds, saves, or user data
as part of a governance migration.

## Verification and provenance

For a governance-only edit, validate the pinned canonical source and its
tests, run its `tools/check-adoption.py` against this Git root, and run the
relevant local Gradle checks without hiding warnings. `gradle.properties`
points `base_wars_mods_dir` to local compile-only mod jars, so a portable
source-build CI job needs an independently resolved input set before it can
make a clean-checkout build claim. A local build is not fresh mod launch,
world-lifecycle, compatibility, or physical acceptance.

The retired root and Claude `SOUL.md` pointers and three pre-pack law
variants remain recoverable from Git commit
`0d15cf086829aa0a9e2b8a851f8187c0eac1b5da`. The historical variants'
SHA-256 values are `C27928962FB033B4EC7434EAA90BCC63E2D2D026E23CBB7D1FC8336336F5C9EE`,
`3B375AACA3545FEA1AA91988FB9DD70FDE0BC123E7FA5EBDBACA5BD1883B5AA5`,
and `AD3137994B46B5581584B9A43049C4FB31173361D49EB1D872CCDBBCB1E2539E`.
Those are recovery evidence, not active law.
