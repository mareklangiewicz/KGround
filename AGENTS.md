# Agent Cheat Sheet

This repository contains `KGround`, a Kotlin "Common Ground". Common code that should be useful for many (especially multiplatform) kotlin projects.

## Project Overview

- Modules starting with: `kgroundx` have additional, less common stuff.
  - More opinionated / dirty / experimental.
- Modules starting with: `kommand` contain DSLs for popular CLI commands.
- Code style is Kotlin Official with adjustments in `.editorconfig`

Keep this sheet handy when automating changes or onboarding new agents.

## Template Sync Tool

Build file regions (marked with `// region [[Name]]` / `// endregion [[Name]]`) can be synchronized from `template-raw/` to other templates.

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
