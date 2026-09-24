# Agent Cheat Sheet

This repository contains `KGround`, a Kotlin "Common Ground". Common code that should be useful for many (especially multiplatform) kotlin projects.

## Project Overview

- `abcdk` (tiny unions) and `tuplek` (tiny tuples) were folded in from their own repos.
  - They keep their artifact ids but ride KGround's single version; `kground` api-exposes both.
  - The old repos (mareklangiewicz/AbcdK, .../TupleK) are dead but NOT yet archived - their
    READMEs point here. Archive them once a KGround release has published both artifacts.
- Modules starting with: `kgroundx` have additional, less common stuff.
  - More opinionated / dirty / experimental.
- Modules starting with: `kommand` contain DSLs for popular CLI commands.
- Code style is Kotlin Official with adjustments in `.editorconfig`

Keep this sheet handy when automating changes or onboarding new agents.

## Template Sync Tool

Build file regions (marked with `// region [[Name]]` / `// endregion [[Name]]`) can be synchronized from `template-full/` to other templates.

### Usage

```bash
# List available regions
./kgroundx-maintenance/sync-regions --list-regions

# Dry run (show changes without applying)
./kgroundx-maintenance/sync-regions --all
./kgroundx-maintenance/sync-regions "Region Name"

# Apply changes
./kgroundx-maintenance/sync-regions --all --apply

# Include main KGround/ project files (default: only templates)
./kgroundx-maintenance/sync-regions --all --apply --include-main

# Create backup files before modifying
./kgroundx-maintenance/sync-regions --all --apply --backup
```

### Options

- `--dry-run` - Show changes without writing (default)
- `--apply` - Actually modify files
- `--all` - Sync all regions
- `--include-main` - Also update main `KGround/` project files (default: only templates)
- `--backup` - Create `.bak` files before modifying
- `--list-regions` - Show available regions and exit

### One thing it does that looks like a change but is not

**It reports every target as changed on every run.** The region it writes differs from the region
it reads by one trailing blank line, so a freshly synced file still shows as `(content changed)`
next run. Verified not cumulative: a second apply leaves the file byte-identical, so this is noise
in the report, not drift in the files. Do not chase it.

### It used to clobber the root `depsDir` path -- fixed at the source, 2026-09-22

Worth knowing, because the fix is the reason this tool is now safe to run unattended. The region
used to carry `File(rootDir, "../../DepsKt")`, a path whose correct depth differs per project, so
every `--apply` rewrote the root project's `"../DepsKt"` and had to be undone by hand.
`--include-main` was no protection: its filter only drops paths starting with `kground`, and the
root settings file does not. Both of the region's knobs are environment variables now (below), so
there is no path and no flag in the file at all: the region has **no per-project text left**, and a
sync needs no repair.

That also made the `[[My Settings Stuff <~~]]` regions dead, and **they have been deleted here**.
The arrow rewrite they documented (`~~>".*/Deps\.kt"~~>"../DepsKt"<~~`) existed to patch exactly
that per-project depth, was never implemented (`MyTemplates.kt` still carries it as
`TODO_someday`), and both the collector and the injector pass `allowTildes = false`, so tilde
regions were skipped entirely -- they described a fixup that never ran, for a problem that no
longer exists.

Note they are **not** synced, for the same reason: the injector skips them. So the ~19 other repos
carrying this region each keep their own copy, and deleting those is a manual sweep.

## The two settings-region toggles are environment variables

`[[My Settings Stuff]]` is synced across ~19 repos, so anything project-specific inside it is a
bug. Both knobs are read from the environment instead, which keeps the region byte-identical
everywhere and means **nothing above the region has to be kept in sync with it**. An earlier
attempt used a `val` above the region; the region referenced it, so the name crossed the boundary
and any repo taking a new region with an old `val` would fail to compile.

```bash
ENABLE_BUILD_SCAN_PUBLISHING_ON_FAILURE=true   # publish a build scan when a build fails
ENABLE_LOCAL_DEPSKT_IN_DIR=/abs/path/to/DepsKt # composite-include a local DepsKt for this run
```

**Both are opt-in: unset means off.** That is deliberate -- a private repo publishes no scan
without anyone remembering to switch it off, which is the failure that matters. The price is that a
PUBLIC repo wanting scans must say so in its own CI workflow. Those workflows are generated from a
Kotlin DSL (`kgroundx-workflows/src/jvmMain/kotlin/workflows/MyWorkflows.kt`), so the env var goes
in the DSL and the YAML is regenerated -- editing `.github/workflows/*.yml` by hand is pointless.
**Not done yet: KGround's own `dbuild` does not set it, so this repo currently publishes no scans.**

Note `System.getenv` works in every part of a settings script, including inside
`pluginManagement { }`, which runs before the script body -- that early pass is exactly why a
script-level `val` cannot be read there. Gradle tracks the reads as configuration-cache inputs, so
toggling a variable invalidates the entry rather than silently reusing a stale one (both measured).

## Developing DepsKt and KGround (or the templates) together

Use the composite toggle from the settings region (see above) — no code change, nothing to revert:

```bash
ENABLE_LOCAL_DEPSKT_IN_DIR=/home/marek/code/kotlin/DepsKt ./gradlew build
```

Both the settings plugin (so `Vers`, `plugs`, the Lib model) and templatefun then come from the
local DepsKt. Measured 2026-09-24 on this root build with a marker version bumped only in the local
DepsKt: the settings plugin AND a project build script both saw the marker, templatefun was built
from the included DepsKt, and the control (env unset) saw the published version everywhere.

**Check the banner, never a green build:** `DepsSettingsPlugin <ver> apply in project KGround`
must name the local version. Local and published often share a version, so bump `Vers.DepsPlug` in
the local DepsKt (uncommitted) when you need to tell them apart. Gradle silently falls back to the
published plugin when the include does not bind.

This replaces the old advice to never `includeBuild("../DepsKt")`. That measurement was real but
had one cause: the region passed a `File` to `includeBuild` inside `pluginManagement { }`, which
only takes a `String`, so the call resolved to the outer `Settings.includeBuild` — a plain composite
that substitutes jars (templatefun) but never contributes plugins, hence the split classpath. The
scoped `mavenLocal` publication in `docs/design/local-build-logic-loop.md` still works, but is no
longer needed for this.
