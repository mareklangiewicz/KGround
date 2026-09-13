# template-logic migration status

Experimental branch `build-logic-context-params`. Kept here, not on `main`.
Last verified 2026-09-13 against KGround 0.1.32.

## Done — shipped on main

- All 11 KGround modules use `id("my-convention")` + one `defaultBuildTemplateFor*` call.
  Build scripts went from ~240 duplicated lines each to 16–46 lines.
- `LibDetails` initialized once in `settings.gradle.kts` as `gradle.extLibDetails`.
- `template-raw` fully migrated, including `template-raw-lib` collapsed to a single
  `defaultBuildTemplateForRawMppLib()` call (the old `TODO: NOW continue based on
  template-full-lib` is resolved).
- `LibSettings` is a context parameter on `allDefault`, `allDefaultSourceSetsForCompose`,
  `defaultAndroDeps`, `defaultAndroTestDeps`.

## Not migrated — `template-full`, `template-basic`, `template-andro` do not configure

All three were broken on `main` at 0.1.32. They are template projects, not published
artifacts, so the 0.1.32 release itself is unaffected.

Fixed here: imports sat below `val buildScanPublishingAllowed`, so the scripts did not
compile at all ("Expecting an element"). Past that, each hits its own wall:

| project | blocker |
|---|---|
| `template-full` | AGP 9 refuses `com.android.library` together with `org.jetbrains.kotlin.multiplatform`. Needs the `plugs.AndroKmpNoVer` / `KotlinMultiplatformAndroidLibraryTarget` path that `template-raw` already uses. |
| `template-andro` | same class of failure via `com.android.application`. |
| `template-basic` | `com.vanniktech.maven.publish` 0.37.0 "already on the classpath with an unknown version". Note `template-raw` uses plain `plugs.VannikPublish` too and configures fine, so `NoVer` alone is NOT the fix — the difference has not been diagnosed. |

## Constraint worth knowing before planning more context-parameter work

Consumer `build.gradle.kts` files **cannot** call contextual declarations — Gradle compiles
them without `-Xcontext-parameters`:

```
e: kground/build.gradle.kts: To call contextual declarations,
   specify the '-Xcontext-parameters' compiler option.
```

Measured directly, not inferred. So context parameters work only inside `template-logic`
and its precompiled script plugins. Roadmap idea #1 ("eliminate almost all explicit
parameters") can therefore reach the internal helpers but not the public entry points,
which must keep an ordinary `details: LibDetails` parameter for the six modules that
override settings.

Related trap, already removed: a `context(Project) val libDetails get() = gradle.extLibDetails`
reads the AMBIENT details and silently discards those per-module overrides. Any future
context work must carry the *effective* `LibDetails`, not re-read it from the project.

## Regression control

`kgroundx-maintenance` / `-experiments` / `-workflows` set `withJs = false`; `kground` does
not. After any change to how settings propagate, check the override still bites:

```
./gradlew -q :kground:tasks --all | grep -c '^jsTest'               # expect 7
./gradlew -q :kgroundx-maintenance:tasks --all | grep -c '^jsTest'  # expect 0
```

A run where both report the same number means settings fell back to ambient.
