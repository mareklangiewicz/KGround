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

## Templates — all four assemble

`./gradlew -p <template> assemble` is BUILD SUCCESSFUL for `template-raw`, `template-full`,
`template-andro` and `template-basic`. `template-raw` was never broken at configuration time,
but it did not assemble either until the compileSdk bump below; both it and `template-basic`
also needed `kotlinUpgradeYarnLock`, their committed lock files having rotted unnoticed.

All three were broken on `main` at 0.1.32. They are template projects, not published
artifacts, so the 0.1.32 release itself is unaffected.

Fixed here: imports sat below `val buildScanPublishingAllowed`, so the scripts did not
compile at all ("Expecting an element"). Past that, each hits its own wall:

| project | state |
|---|---|
| `template-full` | **FIXED.** `./gradlew -p template-full assemble` is BUILD SUCCESSFUL. |
| `template-andro` | **FIXED.** `./gradlew -p template-andro assemble` is BUILD SUCCESSFUL and produces `template-andro-app-debug.apk`. |
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

## De-nesting prototype — `LibTMP` (local, KGround only)

The DepsKt de-nesting (`~/code/kotlin/DepsKt/docs/design/lib-details-denesting.md`) is being
proven **here first**, as `LibDetailsTMP` / `LibSettingsTMP` / `LibComposeSettingsTMP` /
`LibAndroSettingsTMP` / `LibReposSettingsTMP` + `LibTMP`, same `TMP` convention as
`AndroSdkCompileMinorTMP`. DepsKt is published and consumed (KGround is on 0.4.25), so changing
it is a breaking change to a public model; prototyping in the consumer costs no version burn and
runs against a working control (four assembling templates).

Files: `LibDetailsTMP.kt` (the five siblings), `LibTMP.kt` (derivations, factory, adapter).
Nothing in the build templates calls them yet — this is the model, proven, not the migration.

### `LibDetails.toTMP()` is the seam

The prototype is driven from the REAL `gradle.extLibDetails`, so no build script and no
`settings.gradle.kts` changed. Probe 9 asserts the adapter is total (0 mismatched fields across
28 checks, presence included). When DepsKt de-nests for real, the adapter is what gets deleted.

### Proven

`./gradlew :kgroundx-experiments:probes` — **13 passed, 0 failed** (was 8).

- **Presence becomes a compile-time check.** THE headline claim, and it is a compile error, so it
  is not in the probes task — proven by construction: a call to `context(andro: LibAndroSettingsTMP)
  fun probeSdkFullTMP()` with no scope open fails with
  `No context argument for 'andro: LibAndroSettingsTMP' found.`
  The nested model's equivalents are a runtime `settings.andro!!` NPE or a runtime `require`.
- **Copy dance halves** (probe 10): same resulting flags, one `copy` instead of two, root named
  once instead of twice. The other four siblings need no re-wrapping.
- **The interdependent defaults do move** (probe 11). This was the design note's "actual design
  work" and it is smaller than feared: only TWO derivations actually cross object boundaries
  (`LibSettings.compose`'s ten flags, and `repos.withKotlinxHtml`). Both became named functions —
  `defaultComposeSettingsTMP()` / `defaultReposSettingsTMP()`, each `context(settings:
  LibSettingsTMP)`. Verified equal to the constructor defaults across all 8 combinations of
  `withJvm` × `withJs` × `withTestJUnit4`. Everything else (`withJvmVer`, `withTestJUnit5`) derives
  within ONE declaration and needs no change at all.

### Found on the way — a real bug in published DepsKt 0.4.25

`LibAndroSettings.publishOneVariant` is `!publishNoVariants && !publishNoVariants` — the second
conjunct should be `!publishAllVariants`. A lib with `publishVariant = "*"` therefore reports
**both** `publishAllVariants` and `publishOneVariant` true, and `defaultAndroLib` runs
`defaultAndroLibPublishAllVariants()` *and* `defaultAndroLibPublishVariant("*")`. Asserted as
probe 13; fixed in `LibAndroSettingsTMP`. Not a prototype feature — it is live in DepsKt today,
and no KGround module currently sets `publishVariant = "*"`, which is why nothing has burned.

### Correction to the design note — the `ignoreXxx` story is better AND worse than stated

The note says sibling scoping "plausibly deletes the four `ignoreXxx` booleans". Having read every
call site and every `require`, the real count is **three die, two survive** — there are five, not
four, because `ignoreCompose` is two different concepts sharing one name.

**Die — but not for the reason the note gives.** `ignoreCompose` (in
`defaultBuildTemplateForBasicJvmLib` / `jvmOnlyDefault` / `defaultBuildTemplateForBasicMppLib` /
`allDefault`), `ignoreAndroTarget`, and `ignoreAndroConfig` all guard requires of this shape:

```kotlin
details.settings.andro?.let {
  require(ignoreAndroConfig) { "allDefault can not configure android stuff" }
}
```

That is not "is android absent?" — it is *"android is present and you must acknowledge this
template will not configure it."* The flag exists because **nesting makes the sub-settings
impossible to withhold**: a caller passing `details` drags `andro` along whether the callee can
handle it or not, so the only way to say "ignore it" is to pass a boolean saying so.

With siblings the caller simply does not open the andro scope, and "this template cannot configure
android" is expressed by the signature not asking for it. So the win is sharper than the note
claims: not "a scope check replaces `settings.andro!!`", but **the caller can withhold the scope at
all** — which the nested model makes impossible. This is the strongest argument for de-nesting
found so far, and it is worth adding to the design note.

**Survive.** Two are not about presence and no scoping can remove them:

- `ignoreCompose` in `defaultAndroDeps` / `defaultAndroTestDeps` / `defaultAndroLib` means *compose
  mpp is configured instead of compose andro* — a ROUTING choice between two compose
  configurations, with compose very much present. `AndroBuildTemplates.kt:43` says so outright:
  `AndroidX.Activity.compose.takeIf { andro.withActivityCompose }, // this should not depend on
  ignoreCompose!`. It should be RENAMED (`composeConfiguredByMpp`?), not deleted.
- `ignoreAndroPublish` guards `require(ignoreAndroPublish || it.publishNoVariants)` — a constraint
  on the CONTENT of the andro scope (`publishVariant`), not on its existence.

### First migration — `jvmOnlyDefault` takes siblings

`jvmOnlyDefault` now reads `context(settings: LibSettingsTMP)` and takes only
`addJvmDependencies`. Deleted outright:

```kotlin
ignoreCompose: Boolean = false,
ignoreAndroTarget: Boolean = false,
require(ignoreCompose || compose == null) { "jvmOnlyDefault can NOT configure compose stuff" }
require(ignoreAndroTarget || settings.andro == null) { "jvmOnlyDefault can NOT configure android target" }
```

Its one call site in `defaultBuildTemplateForBasicJvmLib` went from forwarding two booleans to
`context(details.settings.toTMP()) { jvmOnlyDefault(addJvmDependencies = addJvmDependencies) }` —
handing over the jvm/testing flags and nothing else.

**Verified by construction, and the enforcement is stronger than predicted.** Referencing `compose`
inside the migrated body fails with:

```
e: Unresolved reference 'compose'.
```

Not "no context argument found" — the name does not EXIST. Under the old `with(settings)` receiver
`compose` resolved perfectly well and only a runtime `require` stood between it and being used.
This is the `ignoreXxx` claim discharged on real code rather than on a probe: the guarantee moved
from a runtime check to name resolution.

**CORRECTION (this was recorded wrongly at first).** An earlier note here claimed nothing calls
`defaultBuildTemplateForBasicJvmLib` / `...BasicJvmApp` / `jvmOnlyDefault`, so the green templates
only proved compilation. That was a bad grep — the pattern left out `BasicJvmApp`. In fact
`template-full/template-full-jvm-cli-app` and `template-raw/template-raw-jvm-cli-app` both call
`defaultBuildTemplateForBasicJvmApp(ignoreCompose = true, ignoreAndroTarget = true)`, which reaches
`defaultBuildTemplateForBasicJvmLib` and then `jvmOnlyDefault`. Two template assembles DO exercise
that path at configuration time, so the migration is runtime-verified, not compile-only.

**Not migrated here.** `defaultBuildTemplateForBasicJvmLib` keeps its own `LibDetails` signature
and its own two `require`s. Those guard the ENTRY POINT's contract with the whole details object,
which is a separate step — it pulls in `addRepos`, `defaultGroupAndVerAndDescription` and
`defaultPublishing`, all still on the nested types.

### Second migration — `allDefault` takes siblings (the MPP path)

`allDefault` now reads `context(settings: LibSettingsTMP)` and takes only
`addCommonMainDependencies`. **All four `ignoreXxx` gone, three `require`s gone:**

```kotlin
require(ignoreCompose || compose == null) { "allDefault can not configure compose stuff" }
andro?.let {
  require(ignoreAndroConfig) { "allDefault can not configure android stuff (besides just adding target)" }
  require(ignoreAndroPublish || it.publishNoVariants) { "allDefault can not publish android stuff YET" }
}
```

Call site in `defaultBuildTemplateForBasicMppLib` went from forwarding four booleans to
`context(details.settings.toTMP()) { allDefault(addCommonMainDependencies = ...) }`.

This one is a REAL runtime control, unlike the jvm increment: every one of KGround's 11 modules
calls `defaultBuildTemplateForBasicMppLib`, and `allDefault` runs at CONFIGURATION time, so simply
configuring the build executes the migrated code 11 times. `template-basic-lib` calls it too.

**Negative control.** Referencing the withheld siblings inside the migrated body is rejected:

```
e: ...MppBuildTemplates.kt:131:15 Unresolved reference 'andro'.
e: ...MppBuildTemplates.kt:130:15 None of the following candidates is applicable:
```

Note the asymmetry — `andro` is cleanly unresolved, but `compose` reports "no applicable candidate"
because a DIFFERENT `compose` symbol (the Gradle compose plugin accessor) is in scope in this file.
Rejected either way, but it is not the clean `Unresolved reference 'compose'` seen in
`JvmBuildTemplates.kt`, and it is worth knowing that withholding a scope does not guarantee the
name is free — an unrelated symbol can still answer to it.

### The three `require`s were DUPLICATES — a symptom worth naming

All three checks deleted from `allDefault` were verbatim copies of checks
`defaultBuildTemplateForBasicMppLib` performs immediately before calling it. The nested model handed
both levels the same over-broad object, so both levels had to re-assert the same facts about it.
De-nesting removes the duplication structurally: the entry point keeps the checks it can actually
make (it holds the whole `LibDetails`), and the worker cannot express them because it cannot see
the data.

### The build-script boundary — CORRECTED, it is a cost, not a limit

An earlier version of this section claimed the de-nesting "stops at the build-script boundary"
because Gradle pins the script language version to 2.2 and scripts therefore have no context
parameters. **That conclusion was wrong**, and probe 7 on this very branch already contradicted it:
a flagless script reaches a context function by coercing a reference to the flattened type.

Probes 14-15 now test the shape a de-nested PUBLIC entry point would actually have, and both pass:

- three sibling context parameters + the `Project` extension receiver + a value parameter;
- the same with a trailing lambda, which every entry point takes.

Order under coercion is context parameters, then extension receiver, then value parameters. So
scripts CAN drive sibling entry points today, with no flag and no Gradle change.

**What it really costs.** The coercion goes through a function TYPE, and a function type has no
default arguments. Control, in a script:

```
e: Initializer type mismatch: expected '(LibDetailsTMP, LibSettingsTMP, LibReposSettingsTMP, Project) -> String',
   actual 'KFunction5<LibDetailsTMP, LibSettingsTMP, LibReposSettingsTMP, Project, String, String>'.
```

Every parameter must be listed and every argument passed explicitly. That is exactly what today's
API is built on: `defaultBuildTemplateForBasicMppLib { ... }` works as a one-liner because
`details: LibDetails = gradle.extLibDetails` and all four `ignoreXxx` default. Under coercion each
call site needs a typed `val` declaration and a full argument list.

**So the decision is ergonomic, not technical.** Keeping `defaultBuildTemplateForBasicMppLib` on
`LibDetails` with boolean parameters is a defensible CHOICE — the nested object is what lets the
common call stay a single line — and it can be revisited whenever Gradle bumps that 2.2 literal,
at which point scripts get `context(...) { }` natively and the cost disappears. Nothing about the
sibling model is blocked on it.

### `ignoreAndroTarget` is now dead in the whole MPP chain

It was already dead inside `allDefault` (the `androidTarget` block it guarded is commented out).
Now the entry point forwards it nowhere, so it is an inert parameter on
`defaultBuildTemplateForBasicMppLib`, `...BasicMppApp`, `defaultBuildTemplateForComposeMppLib` and
`...ComposeMppApp`. Removing it from those four signatures is a follow-up, deliberately not done
here to keep this increment to one change.

### Script-facing helpers — the copy dance reaches build scripts, with no context params

Answering "do we just need a helper on the template-logic side?": yes, and it turns out the
flattened-reference coercion (probes 14-15) is not needed for it at all.

Added in `LibTMP.kt`:

- `Gradle.extLibTMP` — the sibling set for this build, a plain property.
- `Project.libTMP { }` — adjust one sibling, root named ONCE.
- `Project.defaultBuildTemplateForBasicMppLib(lib: LibTMP = gradle.extLibTMP, ...)` — an overload
  keeping full one-liner ergonomics: `lib` defaults, the trailing lambda stays trailing.
- `LibTMP.toNested()` — re-nests for the internals that are still `LibDetails`-based
  (`defaultPublishing`, `defaultGroupAndVerAndDescription`, `addRepos`). TEMPORARY, disappears with
  `toTMP()` once those migrate or DepsKt de-nests.

All of these are plain parameters and plain values, so a flagless script uses them naturally — no
coercion, no `val` declaration, no defaults lost, no Gradle change.

**The important split this makes visible.** The design note's symptoms do not share a mechanism:

| symptom | needs | reaches build scripts? |
|---|---|---|
| 1. nested-copy boilerplate | DATA SHAPE only | **yes, today** |
| 2. `context(details, details.settings)` pairs | context parameters | behind the boundary |
| 3. helpers reaching through (`settings.andro!!`) | context parameters | behind the boundary |

Symptom 1 — the one that shows up in every build script, four of them verbatim — is pure data shape
and is available immediately. Symptoms 2 and 3 need context parameters and stay inside
`template-logic`. Worth carrying into the DepsKt design: de-nesting the DATA and adopting context
parameters are two separable changes, and the first is the one consumers actually see.

### First real build script converted — `kgroundx-experiments`

```kotlin
// was: two statements, root named twice
val settings = gradle.extLibDetails.settings.copy(withJs = false, withLinuxX64 = false, withKotlinxHtml = true)
val details = gradle.extLibDetails.copy(settings = settings)
defaultBuildTemplateForBasicMppLib(details) { ... }

// now: one statement
defaultBuildTemplateForBasicMppLib(libTMP { it.copy(withJs = false, withLinuxX64 = false, withKotlinxHtml = true) }) { ... }
```

Probe 16 asserts the conversion is behaviour-preserving: the sibling form rebuilds a `LibDetails`
EQUAL to the one the nested dance produced, across every field including `compose` / `andro` /
`repos`. Probe 17 is its guard — it shows that comparison can actually fail, so probe 16 is not
passing vacuously. Three more scripts (`kgroundx-maintenance`, `kgroundx-workflows`,
`kgroundx-jupyter`) carry the same verbatim dance and are not converted yet.

### DepsKt can use context parameters internally

Worth recording since it shaped the plan above: DepsKt is an ordinary Kotlin module, so it compiles
at language version 2.4 with context parameters available by default, and can add
`-Xexplicit-context-arguments` exactly as `template-logic` does. Nothing in the sibling model is
blocked on DepsKt's own compilation. The flagless constraint applies ONLY to consumers' build
scripts — which is precisely why the helper layer above is the shape that matters for them.

### Cleanup pass — everything the earlier increments deferred

**All six copy-dance scripts converted.** `kgroundx-experiments`, `kgroundx-maintenance`,
`kgroundx-workflows`, `kgroundx-jupyter` (settings form) and `kommand-line`, `kommand-samples`
(identity form, via `libTMP(adjustDetails = { ... })`). Each lost two statements naming the root
twice. Probe 18 covers the identity form specifically because those scripts rename the lib and
`coordinates(artifactId = name)` publishes under `details.name`; probe 19 asserts the rebuilt
details keep the ORIGINAL `namespace` rather than one recomputed from the new name — `copy()` never
re-runs constructor defaults, and both forms must agree about that.

**`ignoreAndroTarget` deleted** from all four MPP signatures. It guarded a commented-out
`androidTarget` block, so it was inert before this work and provably dead after `allDefault` stopped
taking it.

**`defaultBuildTemplateForBasicMppLib(details: LibDetails, ...)` lost its default.** With two
overloads both fully defaulted, `defaultBuildTemplateForBasicMppLib(ignoreCompose = true) { }` in
`kground/build.gradle.kts` was ambiguous. Dropping the default on the nested overload makes the
sibling overload the only candidate for script-shaped calls; internal callers pass `details`
explicitly anyway.

**`addRepos` migrated** — the design note's own "helpers reach through the tree" example. Was
`context(settings: LibSettings)` + `with(settings.repos)`; now `context(reposSettings:
LibReposSettingsTMP)` with nothing to reach through. Five call sites updated.

The parameter is named `reposSettings`, not `repos`, and that is load-bearing: the body calls
`maven(repos.kotlinx)` where `repos` is a top-level DepsKt object, and a context parameter named
`repos` would take that name. Probe 1 established that a context parameter does not shadow an
extension RECEIVER; this is the complementary hazard — it does occupy its own NAME. Worth carrying
into DepsKt, where `LibReposSettings` and the `repos` object live side by side.

**`ignoreCompose` → `composeConfiguredByMpp`** across `defaultAndroDeps`, `defaultAndroTestDeps`,
`defaultAndroLib`, `defaultAndroApp`, and the two call sites in `allDefaultSourceSetsForCompose`.
This is the survivor identified earlier: it selects between two compose configurations rather than
asserting absence, so no scope can express it — withholding the compose scope would claim "no
compose at all", which is false here. The old name made it look like the presence flags that died
in `jvmOnlyDefault` and `allDefault`; it never was one.

### The andro family — presence-as-scope, where it actually pays

The biggest remaining migration, and the one the design note's strongest argument rests on.

**Migrated.** `defaultAndroDeps`, `defaultAndroTestDeps`, `defaultAndroLib`, `defaultAndroApp`,
both `defaultDefaultConfig` overloads, `androDefault`, and both entry points
(`defaultBuildTemplateForAndroLib` / `...AndroApp`).

**The count.** Before: six `?: error("No andro settings.")` plus one `settings.andro!!` — the design
note's literal example. After: **three** `?: error`, one at each point where "details that may or
may not have android" genuinely arrives from outside (the two andro entry points, and
`allDefaultSourceSetsForCompose`'s andro block). The `!!` is gone entirely.

**Presence-as-scope does not delete the check — it moves it to one boundary.** This is the honest
result and it is worth stating plainly, because "all the `!!`s disappear" would be wrong. Something
must still turn a nullable into a scope at the edge, since a build script may or may not have
configured android. What changes is that it happens ONCE per entry point, and everything below is
statically guaranteed:

```kotlin
val lib = details.toTMP()
val andro = lib.andro ?: error("No andro settings.")   // the only check
context(lib.settings, andro) { defaultAndroDeps(); defaultAndroTestDeps() }
```

`androDefault` shows the payoff most compactly. Its caller needed a separate presence test next to
the helper's own `!!`:

```kotlin
if (settings.withAndro) androDefault()   // ... and inside: val andro = settings.andro!!
lib.andro?.let { context(lib.details, it) { androDefault() } }   // one expression, both jobs
```

**`composeConfiguredByMpp` left the leaves.** Earlier this was classified as "survives, rename
rather than delete". Both halves held, but it moved further than predicted: the compose-android
dependencies became `defaultComposeAndroDeps` / `defaultComposeAndroTestDeps`, which require a
COMPOSE scope, so the routing is now expressed by whether the caller opens that scope. In
`allDefaultSourceSetsForCompose` — the "compose is configured the MPP way" case — the flag is gone
and replaced by simply not opening a compose scope. The boolean survives only as
`configureComposeAndro` on `defaultAndroLib` / `defaultAndroApp`, where a caller that knows both
facts (compose exists, and was not configured the MPP way) states its decision once.

**Negative control.** A compose scope does not satisfy an andro requirement:

```
e: No context argument for 'andro: LibAndroSettingsTMP' found.
```

**Unexercised parts, stated plainly.** `defaultAndroLib` and `LibraryExtension.defaultDefaultConfig`
remain dead code since the AGP 9 migration (an android library is a KMP module now, and
`LibraryExtension` is never applied). They were migrated to keep the family coherent, but no build
proves them — only that they compile. The app path (`defaultAndroApp`, `ApplicationExtension
.defaultDefaultConfig`, both entry points, `androDefault`) IS exercised: `template-andro` assembles
and produces `template-andro-app-debug.apk`.

### Last internals migrated — `toNested()` reaches ZERO production callers

The prototype is complete by its own measure. Migrated in this increment:

- **`defaultPOM` / `defaultPublishing`** → `context(details: LibDetailsTMP, settings: LibSettingsTMP)`.
  `defaultPublishing` reached through exactly one field (`details.settings.withCentralPublish`);
  as siblings it names the two things it uses and cannot see anything else. Still `context(..)`
  and not `with(..)`, for the reason probe 4/5 exist: `LibDetailsTMP` has a `name` too, and
  `coordinates(artifactId = name)` must resolve to the PROJECT name.
- **`defaultGroupAndVerAndDescription`** could NOT be migrated in place — it lives in *published*
  DepsKt (`src/main/kotlin/defaults/Defaults.kt`) and takes the nested type. Restated locally as
  `defaultGroupAndVerAndDescriptionTMP()` over `LibDetailsTMP`. Worth noting for the DepsKt branch:
  it reads only identity fields, so it never needed `settings` at all, and as a sibling it cannot
  even see it.
- **`defaultPublishingOfAndroLib` / `...App`** → `context(_: LibDetailsTMP)`, following `defaultPOM`.
- **`allDefaultSourceSetsForCompose`** → `context(settings: LibSettingsTMP, compose:
  LibComposeSettingsTMP)`. Its `settings.compose ?: error("Compose settings not set.")` is gone;
  the check now happens once, at the `defaultBuildTemplateForComposeMppLib` boundary, which is the
  same presence-as-scope shape the andro family already had.

**Finding 6 recurred, in the prototype's own code.** `allDefaultSourceSetsForCompose` already had a
local `val compose = project.extensions.getByName("compose") as ComposeExtension`. A context
parameter does not shadow a receiver, but it DOES occupy its own name, so one of the two had to
move: the settings take the bare name (the body spells their flags out under `with`) and the Gradle
extension became `composeExt`. This is exactly the `repos` / `reposSettings` collision predicted for
DepsKt, hit a second time without looking for it — evidence the rename is systematic, not incidental.

**The entry point flipped, and that is what zeroed the meter.** `defaultBuildTemplateForBasicMppLib`
is now defined over `LibTMP` (with the default `gradle.extLibTMP`), and the `LibDetails` overload is
a four-line shim that calls `details.toTMP()` and delegates *into* it. Before, the delegation ran the
other way and `toNested()` was in the path of every single build. Nothing was lost: all 11 KGround
build scripts and all four templates were already on the sibling overload, so the flip changed no
call site.

That shim is the shape of the DepsKt migration itself: un-nest ONCE at the top, siblings below.
Finding 7 is enforced there in code — the shim deliberately has no default for `details`, because
two fully-defaulted overloads of one name are ambiguous.

**Where `toNested()` still appears.** Only in `kgroundx-experiments/build.gradle.kts`, in probes
16-18, where the nested model is the *control* being compared against. That is the instrument, not
a dependency: no production path reaches it. When DepsKt de-nests for real, the control disappears
and `toNested()` / `toTMP()` go with it.

**Gate re-run after this increment:** probes **19/19**, `./gradlew assemble` green for all 11
modules, all four templates assemble, `template-andro-app-debug.apk` produced (2026-09-15).

### Every entry point takes siblings, with ONE scope opened at the top

The increment above left each entry point opening NARROW scopes at individual call sites
(`context(lib.details, lib.settings) { defaultPublishing() }`, three or four per function). That
worked, and it is what made the old `context(details, details.settings)` openings dead — but dead
is not the same as replaced, and Marek pushed back on deleting them rather than filling them with
the sibling types. He is right, and the resulting shape is the one DepsKt will want.

All nine entry points now take `lib: LibTMP = gradle.extLibTMP` and open
`context(lib.details, lib.settings)` exactly once, with a bare body underneath:
`defaultBuildTemplateForBasicJvmLib`/`App`, `...BasicMppLib`/`App`, `...ComposeMppLib`/`App`,
`...FullMppLib`/`App`, `...AndroLib`/`App`, `...RawMppLib`. Each keeps a four-line `LibDetails`
shim (un-nest once, delegate in), and finding 7 is enforced in code: the shims carry no default.

`compose` and `andro` stay narrow on purpose — they are the nullable ones, so they are opened at
their boundary inside the body, which is what makes the boundary visible.

**Two things fell out that the design note should claim.**

1. *Inner calls got SHORTER, not merely relocated.* With details and settings already in scope, a
   helper needing one more sibling names only that one:

   ```kotlin
   context(lib.details, andro) { androDefault() }   // before
   context(andro) { androDefault() }                // after
   ```

   The same happened to four `context(lib.settings, compose)` / `context(lib.settings, andro)`
   pairs. This is the compositional half of the argument: scopes ACCUMULATE, so each level adds
   only what it introduces. A nested model cannot do that — `details.settings` has to be spelled
   out again at every level because it is reached through a field, not held open.

2. *`withAndro` and `withCompose` disappeared from two more places.* In
   `defaultBuildTemplateForFullMppLib` and the raw template, `if (details.settings.withAndro)`
   appeared three times around code that then re-derived the andro settings. It is now one
   `lib.andro?.let { andro -> .. }`: the guard and the value arrive together, and the body cannot
   be entered without one. Add this to the `ignoreXxx` story — the booleans that died are not only
   the parameters, but the `withXxx` READS at call sites.

**Gate for this increment, run through `gate.sh`** (see below): probes 19/19, `assemble` green,
all four templates assemble, `template-andro-app-debug.apk` freshly produced 12:41 on 2026-09-15.

### Running the gate on a small machine — `gate.sh`

Running probes + KGround `assemble` + four template assembles back to back pins several Gradle and
Kotlin daemons at once and chokes a laptop with ~3Gi free. `./gate.sh` runs the same commands one
at a time, smallest first, with a settle pause and a memory readout between them.

It also stages what `assemble` depends on BEFORE calling it. The breakdown is from
`./gradlew assemble -m` (dry run), not from guessing: 6x `compileKotlinLinuxX64`, 6x
`compileTestKotlinLinuxX64` (yes, test compilation is in the `assemble` graph), 6x
`compileKotlinJs` plus npm setup, 10x `compileKotlinJvm`, 6x metadata, and
`downloadKotlinNativeDistribution`. The native steps run ONE MODULE PER INVOCATION.

Measured effect of staging: with all of the above warm, `assemble` itself finished in **4 seconds**,
and peak memory never dipped below ~3Gi available. Full order:

```
compile probes native-dist npm meta jvm js native native-test assemble \
  template-basic template-full template-andro template-raw
```

The apk check only fires when `template-andro` was actually in the run's steps, and compares the
file against a timestamp taken at startup — printing the path unconditionally reports an artifact
from an earlier run, which is a success signal that cannot fail.

### Still open

- `LibAndroSettingsTMP.sdkCompileMinor` is where `AndroSdkCompileMinorTMP` wants to live; the
  const is still the source of the default, so the two are not yet collapsed.
- The design note `~/code/kotlin/DepsKt/docs/design/lib-details-denesting.md` still has none of
  findings 1-7, nor the two above (scopes accumulate; the `withXxx` reads die too). Updating it is
  the next step; the prototype no longer has one.
- Unchanged and still true: `defaultAndroLib` and `LibraryExtension.defaultDefaultConfig` are
  migrated but UNEXERCISED (dead since AGP 9), and no tests were run on any template — `assemble`
  only, nothing installed or launched.

---

## The port to the real DepsKt model (2026-09-15)

The prototype is gone. DepsKt 0.4.26 ships the sibling model for real — `Lib`, `LibInfo`,
`LibFlags`, `LibCompose`, `LibAndro`, `LibRepos`, the `lib(..)` factory, `defaultLibCompose` /
`defaultLibRepos`, the `extLib` ext storage and both adapters — so everything this branch was
standing in for has been deleted here and replaced by imports.

### What changed

- `LibDetailsTMP.kt` (123 lines) and `LibTMP.kt` (305 lines) **deleted**. What remains is
  `LibHelpers.kt` (~43 lines): only `Project.myLib(adjustInfo, adjustFlags)`, which is genuinely
  KGround-side. The five types, the bundle, the factory, both derivations and both adapters now
  come from DepsKt.
- Pins bumped in two places: `settings.gradle.kts` (settings plugin `0.4.26`) and
  `template-logic/build.gradle.kts` (`pl.mareklangiewicz.deps:DepsKt:0.4.26`).
- `settings.gradle.kts` now builds the sibling form directly — `gradle.extLib = lib(info =
  myLibInfo(..), flags = LibFlags(..), withCompose = false)` — so `compose = null` became
  `withCompose = false`: presence, stated as presence.
- The local `defaultGroupAndVerAndDescriptionTMP` restatement is **deleted**. It existed only
  because DepsKt's version took the nested type; 0.4.26 ships a sibling one, so the call sites use
  it directly. One less duplicate.
- The `…TMP` suffix is gone everywhere, including the probe names — the model is real now, so the
  suffix would have been a lie.

### The rename was safe because every old spelling was an error

`LibDetailsTMP` → `LibInfo`, `LibSettingsTMP` → `LibFlags`, and so on: no old name survives as
anything valid, so a missed site is a compile error rather than silently-wrong behaviour. Two
rounds of compile-fix caught everything, and both rounds landed in the one file that deliberately
mixes the two models (`ProbeFuns.kt`) plus the probe build script.

Three fixes were NOT mechanical and are worth recording:

1. `probeSdkFull` used `andro.sdkCompileMinor`. **DepsKt's `LibAndro` has no such field**, on
   purpose — carrying a field the nested model lacks would make `Lib.toNested()` lossy and weaken
   DepsKt's own equivalence tests. So `AndroSdkCompileMinor` (the KGround const) is still the source
   of the minor level, and the "still open" item below stays open.
2. DepsKt's derivations take a plain parameter, not a context parameter, because DepsKt compiles
   without `-Xcontext-parameters`. `context(nested.toTMP()) { defaultComposeSettingsTMP() }` became
   `defaultLibCompose(nested.toFlags())`.
3. The per-type adapters have their own names in DepsKt (`toFlags()`, `toSibling()`), not one
   overloaded `toTMP()`.

### A probe changed meaning, and failed for the right reason first

`probePublishVariantBug` asserted `"true|false"` — DepsKt's `publishOneVariant` broken, the sibling
copy fixed. **DepsKt 0.4.26 fixed the bug**, so the broken control no longer exists and the probe
failed on the first run after the bump. That is the failure you want: the assertion had encoded a
bug as expected behaviour.

It is now `probePublishVariantAgreement`, a regression guard — `publishOneVariant` over
`""` / `"*"` / `"debug"` must read `false/false/true` in BOTH models.

### Gate

Probes **19/19** against the real model. The heavier steps were run with `PAUSE=45`.

### Still open (carried forward)

- `LibAndro.sdkCompileMinor` — still not collapsed; `AndroSdkCompileMinor` remains the const, and
  DepsKt deferred the field deliberately (see above). This is now a DepsKt-side additive step.
- Unchanged and still true: `defaultAndroLib` and `LibraryExtension.defaultDefaultConfig` are
  migrated but UNEXERCISED (dead since AGP 9), and no tests were run on any template — `assemble`
  only, nothing installed or launched.
- DepsKt step 4 (drop the nested types and both adapters) is blocked until the probes stop needing
  the nested model as a control — `probeCopyDance`, `probeAdapterFidelity` and
  `probePublishVariantAgreement` all compare against it by design.
