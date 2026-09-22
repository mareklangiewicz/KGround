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

### Two things it does that you have to undo by hand

Both measured 2026-09-22 while syncing `[[My Settings Stuff]]`.

**It rewrites the root `settings.gradle.kts` `depsDir` path, and `--include-main` does not stop
it.** The source region says `File(rootDir, "../../DepsKt")` because `template-full/` sits one
level deeper; the root project needs `"../DepsKt"`. Every `--apply` overwrites it with the
template's depth, which breaks the local-DepsKt composite workflow described below. `--include-main`
is no protection: its filter only drops paths starting with `kground`, and the root settings file
does not. **After any apply, check that line and put it back.** The `[[My Settings Stuff <~~]]`
region above it documents an arrow rewrite (`~~>".*/Deps\.kt"~~>"../DepsKt"<~~`) that would fix
this automatically, but it is not implemented -- `MyTemplates.kt` carries it as
`TODO_someday`, and both the collector and the injector pass `allowTildes = false`, so tilde
regions are skipped entirely.

**It reports every target as changed on every run.** The region it writes differs from the region
it reads by one trailing blank line, so a freshly synced file still shows as `(content changed)`
next run. Verified not cumulative: a second apply leaves the file byte-identical, so this is noise
in the report, not drift in the files. Do not chase it.

## Developing DepsKt and KGround (or the templates) together

Use a **scoped local publication**: DepsKt published under a suffixed version, picked up through a
content-filtered `mavenLocal`. Never `includeBuild("../DepsKt")` — measured, it substitutes
templatefun but not DepsKt, so you silently get a split classpath. Steps, cleanup and the
measurements behind them: `docs/design/local-build-logic-loop.md`.
