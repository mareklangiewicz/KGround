# Running a build against a local DepsKt

> **Superseded 2026-09-24.** The everyday way is now the composite toggle,
> `ENABLE_LOCAL_DEPSKT_IN_DIR=/abs/path/to/DepsKt`, and it is NOT split: measured on KGround's root
> build, the settings plugin, a project build script and templatefun all came from the local DepsKt.
> The "composite never binds DepsKt" result below had one cause nobody saw at the time: every
> variant passed a `File` to `includeBuild` inside `pluginManagement { }`, which only accepts a
> `String`, so Kotlin resolved it to the outer `Settings.includeBuild` — a plain composite that
> substitutes jars but never contributes the SETTINGS plugin. Fixed in the synced settings region
> (`includeBuild(enableLocalDepsKtInDir.path)`, plus a guard so DepsKt does not include itself).
> The recipe and measurements below are kept as history; the recipe still works.

How to develop DepsKt together with KGround or its templates, and the measurements behind the
recipe. `AGENTS.md` points here. Prototyped in `template-raw` on 2026-09-16 and reverted; nothing
below is committed build config. (`template-raw` was deleted on 2026-09-17 once it and
`template-full` converged -- the recipe applies unchanged to `template-full`.)


## The recipe: a scoped local publication

There is no committed toggle for this, on purpose. Publish DepsKt under a suffixed version, point
one build at it, revert everything afterwards.

### 1. Publish DepsKt locally

Add the same suffix in BOTH places DepsKt's own comments say must stay in sync:

```kotlin
version = Ver(0, 4, 31, suffix = "-local1"),        // settings.gradle.kts
val DepsPlug = Ver(0, 4, 31, suffix = "-local1")    // deps/src/main/kotlin/deps/Vers.kt
```

```bash
cd ~/code/kotlin/DepsKt && ./gradlew publishToMavenLocal
```

### 2. Point the consuming build at it

Two temporary edits in the consumer's `settings.gradle.kts` (`KGround/`, `template-full/`, ..).
They are INSIDE the `[[My Settings Stuff]]` region, so **do not run `sync-regions` while they are
in place, and do not commit them**:

```kotlin
pluginManagement {
  repositories {
    mavenLocal { content { includeGroupByRegex("""pl\.mareklangiewicz\..*""") } }  // ADD, scoped
    gradlePluginPortal()
    // ..
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.4.31-local1"  // ADD the suffix
}
```

That is all of it. `Vers.DepsPlug` also drives `plugs.Deps`, `plugs.DepsSettings` and
`plugs.TemplateFun`, and it ships inside the DepsKt jar, so templatefun follows the suffix
automatically — there is no second version to keep in step, and therefore no split to guard
against.

**Always keep `mavenLocal` content-filtered.** Unfiltered it is a footgun, and `LibRepos`' own
`withMavenLocal` deprecation says so.

### 3. Clean up

Revert the two DepsKt edits and the two consumer edits, then drop the artifacts so a stale suffix
cannot answer a later build:

```bash
find ~/.m2/repository/pl/mareklangiewicz -type d -name '*-local1' -exec rm -rf {} +
```

`find`, not a `**` glob — bash needs `globstar` for that and would otherwise delete nothing while
looking like it worked.

### Checking which one you actually got

A green build says nothing about which DepsKt it used, and both builds report the same version
string. Register a probe in the consumer's root `build.gradle.kts` and read an API oracle —
something that exists in one build and not the other:

```kotlin
tasks.register("probeOrigins") {
  notCompatibleWithConfigurationCache("probe")
  doLast {
    val c = Class.forName("pl.mareklangiewicz.deps.LibInfo")
    println("DepsKt from: " + c.protectionDomain?.codeSource?.location)
    println("has getId(): " + c.methods.any { it.name == "getId" })  // pick any API only one side has
  }
}
```

# The measurements

## The probe

A `probeOrigins` task in the consumer's root build script, reporting which DepsKt and which
templatefun are actually on the classpath that runs the templates. It does NOT read version
strings — both builds say `0.4.29`, so a version cannot tell them apart. It used API oracles from
that day's DepsKt commit instead:

- local DepsKt has `LibInfo.getId()`; published 0.4.29 has `getAppId()`
- local templatefun has NO `setMyWeirdSubstitutions`; published 0.4.29 has it

The control run (nothing local) reports `PUBLISHED / PUBLISHED`, so the oracles discriminate. Each
run is ~5s: configuration plus one task, no compilation.

## The composite never binds DepsKt itself

| variant | templatefun | DepsKt |
|---|---|---|
| control, nothing local | PUBLISHED | PUBLISHED |
| `pluginManagement { includeBuild }`, project named `deps` | LOCAL | **PUBLISHED** |
| `pluginManagement { includeBuild }`, project renamed `DepsKt` | LOCAL | **PUBLISHED** |
| `--include-build ../../DepsKt`, no settings code | LOCAL | **PUBLISHED** |
| top-level `includeBuild`, and both includes at once | LOCAL | **PUBLISHED** |
| `includeBuild(dir) { dependencySubstitution { .. } }` | build FAILS |

Every composite variant gives a SPLIT classpath: local templatefun against published DepsKt.
(2026-09-24: the two `pluginManagement { includeBuild }` rows used a `File` argument, i.e. the WRONG
overload — see the note at the top. `--include-build` and top-level `includeBuild` are ordinary
composites by design and never supply a settings plugin, so those rows stand.)
DepsKt reaches a consumer through the SETTINGS plugin, whose classpath is established for the
settings script itself; templatefun arrives later as an ordinary project-script plugin, and that
one does substitute.

**This retired the reason the `:deps` -> `:DepsKt` rename existed.** The rename was there so
composite substitution would match `pl.mareklangiewicz.deps:DepsKt`; measured, it changes nothing.
An earlier session had read `deps:DepsKt:0.4.29 -> project ':DepsKt:DepsKt'` from
`dependencyInsight` — a claim about one configuration's resolution graph, which is NOT the claim
"the classes that run the templates are local". Only the second one matters.

### Why the explicit substitution fails

```kotlin
includeBuild(depsDir) {
  dependencySubstitution { substitute(module("pl.mareklangiewicz.deps:DepsKt")).using(project(":deps")) }
}
```

The rule also applies INSIDE the included build, to DepsKt's own buildscript, which resolves
templatefun -> `deps:DepsKt` -> project `:DepsKt:deps` -> *"No variants exist"*. DepsKt applying
its own published templatefun and DepsKt being substituted by a consumer are in direct tension. An
explicitly declared substitution is applied where the default rules are not.

## The recipe above, measured

Measured: DepsKt LOCAL and templatefun LOCAL together, all three template-raw projects configuring
including the andro app (which calls `info.id`). The control, with the `mavenLocal` line absent,
reports both PUBLISHED.

A `myLocalLibs` property (parsed from `local.properties`, mapping versions in
`resolutionStrategy.eachPlugin`, warning on partial coverage) was also built and worked. It was
dropped: all of it existed to support per-lib granularity, and granularity was what made splits
possible in the first place.

## Dead ends, measured

- `~/.gradle/gradle.properties`, plain or as `systemProp.myLocalLibs` — not resolvable while
  `pluginManagement` is being evaluated. Both re-tested after `--stop`. There is no machine-wide
  gradle.properties switch for this.
- A `val` declared in `settings.gradle.kts` above the region. `pluginManagement` is extracted and
  compiled as its own unit and sees only `Settings` members — which is why the old code computed
  `depsDir` inside the block. The region cannot react to an `enableMyLocalLibs` val.
- `~/.gradle/init.d/*.init.gradle.kts` with `beforeSettings { pluginManagement { .. } }` DOES work
  machine-wide with zero code in any repo (verified; the settings script's own repositories did not
  clobber it). Not adopted: it makes builds behave differently on one machine with no trace in the
  project.
