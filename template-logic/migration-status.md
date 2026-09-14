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
- Context parameters are used throughout `template-logic`'s internals — 14 declarations.
  `LibSettings` on `allDefault`, `allDefaultSourceSetsForCompose`, `defaultAndroDeps`,
  `defaultAndroTestDeps`, `jvmOnlyDefault`; `LibDetails` on `defaultPOM`,
  `defaultPublishing`, `defaultPublishingOfAndroLib`/`App`, `defaultAndroLib`,
  `defaultAndroApp` and both `defaultDefaultConfig` overloads.
  Only the 9 public `defaultBuildTemplateFor*` entry points still take an explicit
  `details: LibDetails = gradle.extLibDetails` — see the constraint below.

## Templates — `template-full` and `template-andro` FIXED; `template-basic` still broken

All three were broken on `main` at 0.1.32. They are template projects, not published
artifacts, so the 0.1.32 release itself is unaffected.

Fixed here: imports sat below `val buildScanPublishingAllowed`, so the scripts did not
compile at all ("Expecting an element"). Past that, each hits its own wall:

| project | state |
|---|---|
| `template-full` | **FIXED.** `./gradlew -p template-full assemble` is BUILD SUCCESSFUL. |
| `template-andro` | **FIXED as far as anything can be** — it configures and compiles; `assemble` stops on a pre-existing SDK drift that blocks `template-raw` identically (see below). |
| `template-basic` | **FIXED.** Root script was missing `plug(plugs.VannikPublish) apply false` — see the diagnosis below. |

### What the fix was

The AGP 9 error is one rule with two different consequences, and the earlier guess ("migrate
to `plugs.AndroKmpNoVer` like template-raw") was right for libraries and wrong for apps.

**Libraries** — `com.android.library` cannot be combined with KMP since AGP 9, so an android
library becomes a KMP module with the `com.android.kotlin.multiplatform.library` target:

- `defaultBuildTemplateForAndroLib` and `defaultBuildTemplateForFullMppLib` now apply
  `plugs.AndroKmpNoVer` and configure `androDefault()`; `LibraryExtension` is not applied at all.
- Dependencies move to the KMP target's per-source-set configurations — the plain
  `implementation` / `testImplementation` of the old path do not exist. `defaultAndroDeps` and
  `defaultAndroTestDeps` are still reused, with `configuration = "androidMainImplementation"`,
  `"androidHostTestImplementation"` and `"androidDeviceTestImplementation"`.

**Apps** — AGP 9 ships **no KMP application plugin** (checked: the distribution registers
`com.android.kotlin.multiplatform.library`, and nothing equivalent for applications). So an
android app cannot be a KMP module at all. `template-andro-app` is now a plain
`com.android.application` with no KMP plugin, depending on `:template-andro-lib` for the shared
code — the structure `template-raw-andro-app` already had.

`template-full-app` needed nothing: its `plug(plugs.AndroAppNoVer) apply false` was vestigial,
since nothing in `template-logic` ever applied it.

### Two bugs that were hiding behind the AGP wall

Both only became visible after applying AGP's documented bypass
(`android.builtInKotlin=false`, `android.newDsl=false`) to get past the plugin conflict:

1. `template-andro-app` reported *"No Kotlin Targets Declared"* — `defaultBuildTemplateForAndroApp`
   never declares a KMP target, so the module was broken independently of AGP 9. Removing the KMP
   plugin (above) is what actually resolves it.
2. `template-andro-lib` called
   `defaultAndroTestDeps(gradle.extLibDetails.settings, configuration = "androidTestImplementation")`
   — a stale call site. That function takes its settings as a **context parameter** now, so it is
   not merely mis-called, it is **uncallable from any build script**. The device-test dependencies
   moved into the template instead. Worth noting as a general hazard: promoting a public helper's
   parameter to a context parameter silently removes it from every build script's reach.

### `template-basic` — diagnosed and fixed

Symptom: `Error resolving plugin [id: 'com.vanniktech.maven.publish', version: '0.37.0'] >
... already on the classpath with an unknown version, so compatibility cannot be checked.`

**Cause.** `template-logic` depends on the publish plugin (`implementation(
"com.vanniktech:gradle-maven-publish-plugin:0.37.0")`), so applying `id("my-convention")`
puts it on a subproject's classpath with **no version metadata**. A subproject then asking
for it *with* a version cannot be checked against that entry. There are exactly two ways
out, and both are already used in this repo:

| project | root declares it | subprojects ask for | result |
|---|---|---|---|
| KGround itself | no | `plugs.VannikPublishNoVer` | works |
| `template-raw` / `-full` / `-andro` | **yes**, `apply false` | `plugs.VannikPublish` | works |
| `template-basic` | no | `plugs.VannikPublish` | **failed** |

`template-basic` did neither. Fix: declare it once in its root script, matching the other
three templates.

**The earlier note in this file was a false inference.** It said "template-raw uses plain
`plugs.VannikPublish` too and configures fine, so `NoVer` alone is NOT the fix". Raw works
because of its *root* declaration, not because versioned requests are safe. Measured both
ways: switching the three subprojects to `VannikPublishNoVer` with no root line **also**
configures. Two independent fixes; the root line was chosen for consistency with the sibling
templates.

**The module named in the error was not special.** Gradle evaluates subprojects
alphabetically, so `template-basic-app` failed merely by being first. Proven: fixing only
that module moved the identical error to `template-basic-jvm-app`. All three shared the
defect — which is why looking for something specific to one module led nowhere.

**Why it drifted, and why nothing caught it.** `AGENTS.md`'s sync tool propagates build-file
regions *by name* from `template-raw`. The root region names do not match:

- `template-raw`: `[[KMP Root Build Template]]`
- `template-full` and `template-andro`: `[[Full Root Build Imports and Plugs]]` (same as each
  other, so they stay in sync)
- `template-basic`: `[[Basic Root Build Imports and Plugs]]` — **unique, so nothing ever syncs
  into it**

So raw's root line could never reach basic. And **no CI workflow builds any template**
(`.github/workflows/` has dbuild/ddepsub/drelease, none of which touch them), so all four
templates could rot silently. That missing gate is the real prevention story here, not the
one-line fix.

### compileSdk — templates now track the newest (37.2)

`Vers.ComposeAndro` is `AndroidX.Compose.Runtime.runtime.verLast`, i.e. deliberately the newest
(currently compose-android 1.13.0-alpha03), and that alpha refuses to be consumed by anything
compiling against less than API **37.1**. The templates compiled against 37.0, so
`checkAarMetadata` failed wherever `defaultAndroDeps` added the android-compose artifacts.
Templates are examples for new projects, so the fix is to raise the SDK, not pin compose back.

Available platforms are 37.1 and 37.2; the templates now target **37.2**, and AGP downloads the
platform itself (the licence was already accepted in the SDK's `licenses/`).

Expressing a *minor* API level needs AGP 9 APIs that the model does not reach yet:

- KMP android target: `compileSdk { version = release(37) { minorApiLevel = 2 } }`
- old DSL (`ApplicationExtension`): `compileSdk = 37; compileSdkMinor = 2`

`LibAndroSettings.sdkCompile` is a plain `Int` with nowhere to put the minor, so the value lives
in `AndroSdkCompileMinorTMP` in `AndroBuildTemplates.kt` — deliberately loud, because a VERSION
belongs in DepsKt's `Vers` next to `AndroSdkCompile`, with a matching
`LibAndroSettings.sdkCompileMinor`. It sits in KGround only because `settings.gradle.kts` has
`depsInclude = false`, so KGround consumes DepsKt *published* and a DepsKt change cannot reach
these templates without cutting a release. **Move it when DepsKt is next touched.**

### template-andro-app is a plain Jetpack Compose android app now

Fallout from it no longer being a KMP module (AGP 9 has no KMP application plugin), all found by
building rather than by reasoning:

- sources moved `src/androidMain/` → `src/main/`, the layout `template-raw-andro-app` uses
- its namespace collided with `:template-andro-lib` (both resolved to
  `pl.mareklangiewicz.templateandro`); the app is now `…templateandro.androapp`, and its Kotlin
  package was renamed to match so the manifest's `.MainActivity` still resolves
- it needs the compose **compiler** plugin (`plugs.KotlinMultiCompose`) and
  `buildFeatures.compose`, because unlike `template-raw-andro-app` — whose UI lives in the shared
  lib — this app uses Jetpack Compose directly. `defaultAndroApp` now enables
  `defaultComposeStuff()` the way `defaultAndroLib` always did. Without the compiler plugin the
  symptom is a misleading backend crash: *"Couldn't inline method call: CompositionLocal.current"*.

Result: `./gradlew -p template-andro assemble` is BUILD SUCCESSFUL and produces
`template-andro-app-debug.apk`.

## Roadmap idea #2 — "persona" precompiled script plugins: prototyped and REJECTED

Built, measured, and reverted (the prototype is in git history on this branch, one commit
before its revert). Do not re-prototype it without a new idea about the two costs below.

The shape was `my-mpp-lib.gradle.kts` carrying the plugins and the build-template call,
with overrides arriving through a `myMppLib { }` extension backed by a `MyMppLibPersona`
class. It worked: `kgroundx-workflows` (which overrides settings AND has its own jvmMain
dependencies) migrated to one `plugins` block plus one extension block, with the resolved
`jvmCompileClasspath` and the generated POM **byte-identical** to the original script's.

Rejected anyway, because what it buys does not cover what it costs:

- **It cannot inject imports.** `Io.GitHub…` still needs `import pl.mareklangiewicz.deps.*`
  in the consumer, so the imports region survives. Only the `plugins`/`plugAll` region and
  the `val settings = …copy(); val details = …copy(settings = settings)` dance disappear.
- **A consumer cannot keep its own `kotlin { }` block.** The consumer's
  `sourceSets { jvmMain { } }` runs during evaluation; the template call runs in
  `afterEvaluate`; Kotlin then fails with *"The compilation 'main' cannot be created after
  the source set 'jvmMain'"*. So every source set or KMP knob a consumer wants has to be
  mirrored onto the extension (`jvmMainDependencies { }`, and one more per migrated module).
  The extension grows toward mirroring the whole KMP DSL.
- **`afterEvaluate` is the mechanism, and it is an anti-pattern.** A lazier design cannot
  avoid it: settings are exactly what decide which targets exist, so the template call has
  to wait for the extension to be filled in.
- **It adds a concept.** A `MyMppLibPersona` class plus an extension plus deferred timing
  is more indirection to understand than the `defaultBuildTemplateFor*(details) { }` call
  it replaces, for a handful of saved lines.

Net: the plugins block and the copy dance are worth removing, but not at the price of
`afterEvaluate` plus a mirror API. If the copy dance is the thing to kill, kill it in the
entry-point signature instead — see the signature notes above.

## Next direction — de-nest LibDetails/LibSettings in DepsKt

The design note lives where the work would happen: **`DepsKt/docs/design/lib-details-denesting.md`**.
Short version, because it changes what is worth doing on this side:

`LibDetails → LibSettings → { LibComposeSettings?, LibAndroSettings?, LibReposSettings }` is
three levels deep, and three things here are that nesting leaking — the verbatim copy dance in
four build scripts, the `context(details, details.settings)` on every entry point (two context
arguments for one logical thing), and helpers reaching through the tree (`with(settings.repos)`,
`settings.andro!!`).

The fix is **siblings, not one flat class** — flattening into a single ~60-field type would make
context parameters worse. The prize is bigger than boilerplate: `compose`/`andro` being nullable
means the nesting encodes *presence*, so as sibling context parameters a function that needs
android declares `context(andro: LibAndroSettings)` and cannot be called outside an android scope
— plausibly deleting the four `ignoreXxx` booleans along with the `!!`s.

Consequence for this branch: a `settings: LibSettings.() -> LibSettings = { this }` parameter on
the entry points was prototyped and **reverted**. It removed the copy dance from four modules with
byte-identical POMs and task lists, but it only smooths ONE nesting level (a consumer reaching the
third level would write `settings = { copy(compose = compose!!.copy(…)) }`, which is worse), and it
forces `settings` to be declared BEFORE `details` so the fold can live in `details`'s default —
an ordering the de-nesting would undo anyway. Not worth baking in ahead of the real fix.

DepsKt is published and consumed (KGround is on 0.4.25), so that work needs its own branch and
version, with KGround migrated only once the new shape is proven.

## Probes — what is executable, and what is only measured

```
./gradlew :kgroundx-experiments:probes      # ~2s
```

No scaffolding: this repo already IS the setup the claims are about. The task lives in a
build script compiled WITHOUT `-Xcontext-parameters` and calls small helpers in
`template-logic/.../ProbeFuns.kt`, compiled WITH it. It asserts, at runtime:

| claim | how |
|---|---|
| `context(x)` does not shadow `Project.name` the way `with(x)` would | calls a `context(LibDetails) fun Project.…` that returns `name`, asserts it is the MODULE name |
| `context(a, b)` works and `context(_)` forwards without naming | calls a conduit that names neither context |
| a flagless script reaches a context fun via the flattened coercion | `val f: (LibDetails, Project) -> String = Project::probeNameIsProjectName` |

The first probe is guarded by a fixture check that the project name and library name
actually differ — otherwise it would prove nothing. Mutation-tested: switching the helper
to `with(details) { name }` fails it with `expected <kgroundx-experiments> but got
<KGround>`, which is precisely the wrong-artifactId bug it exists to catch.

Note the third row does double duty: the flagless script can only reach those helpers
*because* the coercion works, so every probe here also exercises it.

**Not executable — measured 2026-09-14, Kotlin 2.4.0 / Gradle 9.7.1:**

- A flagless script cannot call a context fun naturally: `e: To call contextual
  declarations, specify the '-Xcontext-parameters' compiler option.`
- A bare `val ref = ::ctxFun` resolves, but invoking `ref()` fails with the same error;
  only the explicit flattened type gets through.
- `context(d) { githubUrl }` does not resolve, while `with(d) { githubUrl }` does — the
  compile-level form of the shadowing claim above.

These are compile-FAILURE claims, so a Gradle task cannot assert them without generating
throwaway projects; that machinery existed briefly and was not worth its weight. They are
also background facts rather than invariants: if Kotlin relaxed any of them it would open
an option, not break this build. The consequence that would actually bite — shadowing — is
covered positively above.

Also worth knowing if you extend the task: standalone `kotlinc` and Gradle's build-script
compiler DISAGREE here. kotlinc reports "no context argument found" and rejects the
callable reference as "unsupported because it has context parameters". Probing with
kotlinc therefore contradicts the claims; only the real script compiler counts.

## Regression control

`kgroundx-maintenance` / `-experiments` / `-workflows` set `withJs = false`; `kground` does
not. After any change to how settings propagate, check the override still bites:

```
./gradlew -q :kground:tasks --all | grep -c '^jsTest'               # expect 7
./gradlew -q :kgroundx-maintenance:tasks --all | grep -c '^jsTest'  # expect 0
```

A run where both report the same number means settings fell back to ambient.
