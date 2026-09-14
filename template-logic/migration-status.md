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
  flag** — Kotlin 2.4.x enables context parameters by default. Measured: a
  `context(m: CtxProbeMarker) fun …` in `kgroundx-experiments/src/commonMain/kotlin`
  compiles, and a deliberate type error in the same file fails, proving it really was
  compiled.
- Note `defaultCompiler()` *does* add the flag, but it is only ever called from
  `defaultBuildTemplateForRawMppLib`, so KGround's own modules never receive it — and do
  not need it. The probe asserts that, so the two facts cannot drift apart silently.

This also explains the kotlinc-vs-Gradle discrepancy noted below: it is not two compilers
disagreeing arbitrarily. The regular Kotlin 2.4.x compiler has context parameters ON by
default; Gradle's *script* compiler is the conservative one that still demands the flag.

**Consequence for the roadmap:** context parameters are fully available in KGround's own
library code today. The restriction is specific to `build.gradle.kts`.

The metadata stamp (2.2.0) is the binary FORMAT version, not a compiler release — it is
evidence the two sides agree with each other, not evidence of which Kotlin built them.

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
