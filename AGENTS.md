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
root settings file does not. The path is absolute now (`enableLocalDepsKtInDir`), which is the same
line everywhere, so the region has **no per-project text left at all** and a sync needs no repair.

That also makes the `[[My Settings Stuff <~~]]` region dead: the arrow rewrite it documents
(`~~>".*/Deps\.kt"~~>"../DepsKt"<~~`) exists to patch exactly that depth, was never implemented
(`MyTemplates.kt` carries it as `TODO_someday`), and both the collector and the injector pass
`allowTildes = false` so tilde regions are skipped entirely. It is left in place for now.

## Developing DepsKt and KGround (or the templates) together

Use a **scoped local publication**: DepsKt published under a suffixed version, picked up through a
content-filtered `mavenLocal`. Never `includeBuild("../DepsKt")` — measured, it substitutes
templatefun but not DepsKt, so you silently get a split classpath. Steps, cleanup and the
measurements behind them: `docs/design/local-build-logic-loop.md`.
