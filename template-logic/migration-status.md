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

### Still blocked, and NOT caused by this work: compileSdk 37.0 vs 37.1

`androidx.compose.ui:ui-tooling*:1.13.0-alpha03` requires compiling against Android API 37.1;
the templates compile against 37.0, so `checkAarMetadata` fails wherever `defaultAndroDeps` adds
the android-compose tooling dependencies.

Control: **`template-raw` fails identically** (`:template-raw-andro-app:checkDebugAarMetadata`,
same three issues), so this is pre-existing dependency drift, not migration fallout.
`template-full` is unaffected only because it passes `ignoreCompose = true` (compose is configured
the mpp way there) and so never pulls those artifacts.

Fixing it needs a way to express a **minor** API level: `LibAndroSettings.sdkCompile` is an `Int`,
and AGP 9's `compileSdk { version = release(37) }` would need the minor variant. That is a DepsKt
model change — see the de-nesting note, same repo, same reason to do it separately.

## Constraint worth knowing before planning more context-parameter work

Consumer `build.gradle.kts` files **cannot** call contextual declarations — Gradle compiles
them without `-Xcontext-parameters`:

```
e: kground/build.gradle.kts: To call contextual declarations,
   specify the '-Xcontext-parameters' compiler option.
```

Measured directly, not inferred. So context parameters work only inside `template-logic`
and its precompiled script plugins. And this is not a missing flag that could be supplied
somewhere — **Gradle hardcodes the script language version, with no configuration surface
at all.** Read off the distribution in use (`gradle-kotlin-dsl-9.7.1.jar`,
`org.gradle.kotlin.dsl.support.KotlinCompilerKt`):

```
gradleKotlinDslLanguageVersionSettingsFor(KotlinCompilerOptions):
  LanguageVersionSettingsImpl(
    LanguageVersion.KOTLIN_2_2, ApiVersion.KOTLIN_2_2,          // literals, not config
    mapOf(skipMetadataVersionCheck, skipPrereleaseCheck, allowUnstableDependencies,
          jvmDefaultMode=ENABLE, javaTypeEnhancementState),     // analysis flags
    /* specificFeatures = */ default empty)                     // no LanguageFeature hook
```

`KotlinCompilerOptions` carries exactly three fields — `jvmTarget`, `allWarningsAsErrors`,
`explicitSkipMetadataVersionCheck`. There is no `freeCompilerArgs` anywhere in the script
compilation path; the only `-X` strings in the whole jar are the five above. The
`org.gradle.kotlin.dsl.*` system properties that exist (`allWarningsAsErrors`,
`skipMetadataVersionCheck`, `scriptCompilationAvoidance`, `dcl`, `internal.io.timeout`,
`locationAwareEditorHints`) touch no language feature.

So this is a property of the Gradle version, and it unblocks itself for free — with no
flag — whenever Gradle bumps that `KOTLIN_2_2` literal to 2.4. Until then, precompiled
script plugins (compiled by `kotlin-dsl` as ordinary Kotlin source, where `freeCompilerArgs`
does work) are the only supported route to context parameters in root-script-adjacent code.
That is the load-bearing reason roadmap idea #2 (persona plugins) is the route to
"extremely clean root scripts", rather than one option among several.

Roadmap idea #1 ("eliminate almost all explicit parameters") can therefore reach the
internal helpers but not the public entry points, which must keep an ordinary
`details: LibDetails` parameter for the six modules that override settings.

### Provide context with `context(x) { }`, NOT `with(x) { }`

Both supply a context argument, but they differ in one decisive way, measured on Kotlin
2.4.0 (the version Gradle 9.7.1 uses to compile this module):

```kotlin
// githubUrl exists only on LibDetails, so it detects an implicit receiver:
with(details)    { githubUrl }   // COMPILES  -> with() also makes it a RECEIVER
context(details) { githubUrl }   // Unresolved reference -> context() does NOT
```

That matters because `LibDetails` and `Project` share `name`, `group`, `version` and
`description`. Under `with(details)`, a `fun Project.…` body silently rebinds `name` from
the module name to the library name — which would corrupt
`coordinates(artifactId = name)` in `defaultPublishing` with no compile error.

`context(x) { }` supplies the context argument and nothing else, so that whole class of
shadowing cannot happen. Every context-providing site here uses it, and it takes several
arguments at once: `context(details, details.settings) { … }`.

Because it cannot shadow, the six entry points with anything to collapse establish context
for their WHOLE body — `): Unit = context(details, details.settings) { … }` — rather than
wrapping individual call sites. Verified safe by widening and re-running the controls
below: `println("MPP Module ${name}…")` still resolves `name` to the project, which is
exactly what `with` would have broken.

Two groups deliberately left alone:
- `defaultBuildTemplateForBasicJvmApp`, `…ForFullMppApp`, `…ForBasicMppApp`,
  `…ForComposeMppApp` are pure delegators — they pass `details` explicitly to another entry
  point and consume no context, so adding one would be dead scope.
- `defaultBuildTemplateForRawMppLib` declares `details`/`settings` as locals inside its
  ~200-line body, so widening would re-indent all of it to remove a single wrapper. Its one
  narrow `context(details) { defaultPublishing() }` stays.

Note the two are NOT interchangeable in the other direction: bodies that genuinely want
receiver semantics keep `with`, e.g. `allDefault`/`jvmOnlyDefault` are declared
`context(settings: LibSettings) fun … = with(settings) { … }` so the body can say `compose`,
`andro`, `withJvmVer` unqualified.

Regression control — `artifactId` must come from the module and `<name>` from the lib, and
the two must stay different:

```
./gradlew -q :kommand-samples:generatePomFileForJvmPublication
grep -E '<artifactId>|<name>' kommand-samples/build/publications/jvm/pom-default.xml
# expect artifactId kommand-samples-jvm  and  name "Kommand Samples"
```

### Context parameters are NOT callable from Kotlin without the flag

A context parameter IS compiled as a value parameter prepended before the extension
receiver — `javap` on `KotlinModuleBuildTemplateKt` shows:

```
public static final void defaultPublishing(LibDetails, Project);
```

That is true at the JVM/ABI level, but it does NOT make such functions callable from Kotlin
code compiled without `-Xcontext-parameters`. The frontend refuses on both counts:

```
e: To call contextual declarations, specify the '-Xcontext-parameters' compiler option.
e: Too many arguments for 'context(d: LibDetails) fun probeNoReceiver(): String'.
```

Measured with a receiverless probe, so "receiver type mismatch" is not the confound.

**But there IS a source-level escape hatch**, and it works from a flagless script. Taking a
callable reference and coercing it to an explicit FLATTENED function type is accepted, and
calls through that value are accepted too — proven by executing it, not just compiling:

```kotlin
// in build.gradle.kts, NO -Xcontext-parameters
val f: (LibDetails) -> String = ::someCtxFun   // context param becomes arg 1
println(f(gradle.extLibDetails))               // actually runs
```

The intermediate step is essential. A bare `val ref = X::ctxFun` resolves, but invoking
`ref(...)` still fails with the flag error; only the explicit flattened type lets the call
through. Argument order is context parameters, then extension receiver, then value
parameters — as the javap signature shows.

We deliberately do NOT use this for the entry points, because it is strictly worse than the
ordinary `details` parameter they already take:

- every default argument is lost — you must pass all of them, so the hacky `ignoreXXX`
  flags become bare positional booleans, `f(s, ext, false, false, {})`, where transposing
  two of them compiles and silently misconfigures the build
- named arguments are gone with them
- it defeats the purpose: you are passing the context explicitly anyway
- it hard-codes context-before-receiver ordering, an implementation detail

So the accurate statement is not "entry points cannot be context-based" but "they can, at a
cost that is not worth paying here".

### Project stays an extension receiver, never a context parameter

Entry points must keep `fun Project.…`: build scripts call them with Project as the implicit
receiver, and reaching a context parameter from a flagless script needs the awkward flattened
coercion above.
Internal helpers keep it too — receiver syntax is what makes `extensions`, `tasks`,
`repositories`, `plugins` and `dependencies` available unqualified; as a context parameter
every one of those becomes `project.…`. Context parameters and an extension receiver coexist
without trouble, as the javap signature shows.

### Unnamed context parameters for pure conduits

`context(_: LibDetails)` is supported and still propagates downstream. Used on the two
functions that hold the context ONLY to forward it and never name it —
`defaultPublishingOfAndroLib` (passes it to `defaultPOM`) and `defaultPublishingOfAndroApp`
(delegates to the former). Everywhere else the context is referenced by name, so `_` would
be wrong.

Related trap, already removed: a `context(Project) val libDetails get() = gradle.extLibDetails`
reads the AMBIENT details and silently discards those per-module overrides. Any future
context work must carry the *effective* `LibDetails`, not re-read it from the project.

### `-Xexplicit-context-arguments` — a second, separate opt-in

`ContextParameters` and `ExplicitContextArguments` are **two different `LanguageFeature`
entries** in the Kotlin 2.4.0 / 2.4.20 compilers. The second is opted into by
`-Xexplicit-context-arguments`, described by the compiler as *"Enable explicit passing of
context arguments using named argument syntax."* It lets a call site supply a context
argument by the parameter's declared name instead of establishing it with `context(..) { }`:

```kotlin
context(d: LibDetails)
fun probeContextFun(): String = "ctx:" + d.name

fun probeExplicitContextArg(details: LibDetails): String = probeContextFun(d = details)
```

Both flags are now in `template-logic/build.gradle.kts`. Control, measured: with
`-Xcontext-parameters` alone that call site fails with

```
e: ProbeFuns.kt:57:60 No context argument for 'd: LibDetails' found.
e: ProbeFuns.kt:57:76 No parameter with name 'd' found.
```

so the flag is doing real work and the probe cannot pass by accident. Asserted as probe #8.

It is a **call-site** feature, so it does not reach consumer `build.gradle.kts` — those
are the call sites that are pinned to language version 2.2 and cannot opt into anything.
It changes nothing about the 9 public entry points' signatures; its value is inside
`template-logic`, where a helper can now forward an ambient value by name rather than
wrapping the call in `context(..) { }`.

Not established: whether `ExplicitContextArguments` becomes default-on at language
version 2.4 the way `ContextParameters` does. The control above was measured on
`template-logic`, which is itself compiled at language version ~2.2 (metadata stamp
`2.2.0`), so it only proves the flag is required *below* 2.4.

## Three Kotlins are in play, and they differ

`./gradlew :kgroundx-experiments:probes` reports this, read off real bytecode and real
task config rather than assumed:

```
lib      template-logic sources (WITH flag) : metadata 2.2.0, stdlib 2.4.0
consumer build.gradle.kts    (flagless)     : metadata 2.2.0
^ both of the above are Gradle's embedded Kotlin: 2.4.0
module   kgroundx-experiments/src/**.kt     : Kotlin plugin 2.4.20, flag not set (not needed)
```

- **Build scripts and template-logic sources** are both compiled by Gradle's *embedded*
  Kotlin (`embeddedKotlinVersion` = 2.4.0). Same compiler; the ONLY difference between them
  is `-Xcontext-parameters`, which `template-logic/build.gradle.kts` sets for itself.
- **Module sources** are compiled by the Kotlin plugin (2.4.20) and **do not need the
  flag**. Measured: a `context(m: CtxProbeMarker) fun …` in
  `kgroundx-experiments/src/commonMain/kotlin` compiles, with a deliberate type error in
  the same file as the control proving it really was compiled.

### The gate is the LANGUAGE version 2.4, not the compiler release

Compiling that same probe against each language version, the compiler says it outright:

| languageVersion | result |
|---|---|
| 2.2 | `e: The feature "context parameters" is only available since language version 2.4` |
| 2.3 | same error |
| 2.4 | compiles, no flag needed |

So `-Xcontext-parameters` is the opt-in for language version **below** 2.4; from 2.4 the
feature is on by default. That single rule explains all three sides:

- module sources take the plugin default (2.4) → no flag needed
- build scripts are compiled by Gradle's embedded Kotlin **2.4.0** but at **language
  version 2.2** → the flag is required. This is not a contradiction: `-language-version`
  is a separate knob, so a 2.4.0 compiler can compile as 2.2. Measured two independent
  ways — declaring a context fun in a `build.gradle.kts` reports the *same* "only
  available since language version 2.4" error, and the `@Metadata` stamp (which tracks
  the language version) reads `2.2.0` on script/template-logic classes versus `mv=[2,4,0]`
  on a module class
- `template-logic/build.gradle.kts` therefore adds the flag for itself

Note `defaultCompiler()` pins `languageVersion = 2.3` AND adds the flag in the same place —
consistent, and the reason the raw template still needs it.

The probes assert that no module pins a languageVersion below 2.4 behind the compiler's
back; pinning 2.3 turns that red with `expected <[]> but got <[2.3]>`.
- Note `defaultCompiler()` *does* add the flag, but it is only ever called from
  `defaultBuildTemplateForRawMppLib`, so KGround's own modules never receive it — and do
  not need it. The probe asserts that, so the two facts cannot drift apart silently.

This also explains the kotlinc-vs-Gradle discrepancy noted below: not two compilers
disagreeing arbitrarily, just different language versions either side of the 2.4 gate.

**Consequence for the roadmap:** context parameters are fully available in KGround's own
library code today. The restriction is specific to `build.gradle.kts`.

The metadata stamp is not the compiler release — it tracks the LANGUAGE version, which is
what makes it useful here: 2.2.0 on the script side and 2.4.0 on a module class is direct
evidence of the split, independent of the flag behaviour it explains.

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
