package pl.mareklangiewicz.templatelogic

import org.gradle.api.Project
import pl.mareklangiewicz.deps.*

// Experimental probe helpers for the context-parameter claims in migration-status.md.
// Branch-only; nothing in the build templates calls these.
//
// This file is compiled WITH -Xcontext-parameters. The build script that calls it is
// compiled WITHOUT. That is the real setup the claims are about, so no scaffolding is
// needed -- see the `probes` task in kgroundx-experiments/build.gradle.kts.

/**
 * Returns the PROJECT name.
 *
 * The whole reason entry points use `context(details)` and not `with(details)`:
 * LibDetails and Project both have `name`, so under `with` this would silently return
 * the LIBRARY name instead -- which is exactly how `coordinates(artifactId = name)` in
 * [defaultPublishing] would end up publishing every module under the wrong artifactId.
 * A context parameter is not a receiver, so it cannot shadow.
 */
context(details: LibDetails)
fun Project.probeNameIsProjectName(): String = name

context(d: LibDetails, s: LibSettings)
private fun probeInner(): String = "${d.name}/${s.withJvm}"

/** Holds both contexts only to forward them, so it names neither. */
context(_: LibDetails, _: LibSettings)
private fun probeConduit(): String = probeInner()

/** `context(a, b)` takes several arguments at once, and `_` still propagates downstream. */
fun probeMultiAndUnnamed(d: LibDetails): String = context(d, d.settings) { probeConduit() }

/** Target for the flattened-coercion probe: reachable from a flagless script. */
context(d: LibDetails)
fun probeContextFun(): String = "ctx:" + d.name

private object LibMarker

/**
 * Kotlin metadata version stamped into template-logic's OWN bytecode, i.e. evidence of
 * which compiler actually built this side. Read off the class, not from configuration.
 */
fun probeLibMetadataVersion(): String =
  LibMarker::class.java.getAnnotation(Metadata::class.java)
    ?.metadataVersion?.joinToString(".") ?: "unknown"

/** kotlin-stdlib visible to template-logic at runtime. */
fun probeLibStdlibVersion(): String = KotlinVersion.CURRENT.toString()

/**
 * Probe for `-Xexplicit-context-arguments`: pass a context argument by NAME at the call
 * site instead of establishing it with `context(..) { }`. Separate LanguageFeature from
 * ContextParameters, so it needs its own opt-in.
 */
fun probeExplicitContextArg(details: LibDetails): String = probeContextFun(d = details)

// ===== de-nesting prototype probes (LibTMP) ==================================
// See LibDetailsTMP.kt and ~/code/kotlin/DepsKt/docs/design/lib-details-denesting.md.

/**
 * Needs an andro scope to exist at all. The NEGATIVE is the interesting half and it is a
 * COMPILE-time property, so it cannot be asserted from the `probes` task; it was verified by
 * construction instead — calling this with no scope open fails with
 * "No context argument for 'andro: LibAndroSettingsTMP' found."
 * In the nested model the same mistake costs a runtime `settings.andro!!` NPE, or an
 * `ignoreAndroTarget` boolean guarding a runtime `require`.
 */
context(andro: LibAndroSettingsTMP)
fun probeSdkFullTMP(): String = "${andro.sdkCompile}.${andro.sdkCompileMinor}"

/** Opening the scope is the only way in — and having opened it, no `!!` appears anywhere below. */
fun probeAndroScopeTMP(lib: LibTMP): String =
  lib.andro?.let { context(it) { probeSdkFullTMP() } } ?: "no-andro-scope"

/**
 * The copy dance, both ways, from the same starting point. Returns
 * "<nested flags>|<sibling flags>|<nested root mentions>|<sibling root mentions>".
 *
 * Nested (verbatim from four of KGround's build scripts — two statements, root named twice):
 *   val settings = gradle.extLibDetails.settings.copy(withJs = false, withLinuxX64 = false)
 *   val details = gradle.extLibDetails.copy(settings = settings)
 *
 * Sibling: one flat copy of the ONE sibling that changed. The other four are untouched and
 * keep flowing on their own, so there is nothing to re-wrap and no root to name a second time.
 */
fun probeCopyDanceTMP(orig: LibDetails): String {
  val nestedSettings = orig.settings.copy(withJs = false, withLinuxX64 = false)
  val nested = orig.copy(settings = nestedSettings)

  val lib = orig.toTMP()
  val sibling = lib.copy(settings = lib.settings.copy(withJs = false, withLinuxX64 = false))

  fun flags(withJs: Boolean, withLinuxX64: Boolean, withJvm: Boolean) = "$withJs/$withLinuxX64/$withJvm"
  val n = flags(nested.settings.withJs, nested.settings.withLinuxX64, nested.settings.withJvm)
  val s = flags(sibling.settings.withJs, sibling.settings.withLinuxX64, sibling.settings.withJvm)
  return "$n|$s|2|1"
}

/**
 * The adapter is the migration seam, so it has to be total: every flag of the real
 * `gradle.extLibDetails` must survive un-nesting. Returns the number of MISMATCHED fields.
 */
fun probeAdapterFidelityTMP(orig: LibDetails): Int {
  val lib = orig.toTMP()
  val d = lib.details
  val s = lib.settings
  val checks = listOf(
    d.name == orig.name, d.group == orig.group, d.description == orig.description,
    d.authorId == orig.authorId, d.authorName == orig.authorName, d.authorEmail == orig.authorEmail,
    d.githubUrl == orig.githubUrl, d.licenceName == orig.licenceName, d.licenceUrl == orig.licenceUrl,
    d.version == orig.version, d.namespace == orig.namespace, d.appId == orig.appId,
    d.appMainPackage == orig.appMainPackage, d.appMainClass == orig.appMainClass,
    d.appMainFun == orig.appMainFun, d.appVerCode == orig.appVerCode, d.appVerName == orig.appVerName,
    s.withJvm == orig.settings.withJvm, s.withJvmVer == orig.settings.withJvmVer,
    s.withJs == orig.settings.withJs, s.withLinuxX64 == orig.settings.withLinuxX64,
    s.withKotlinxHtml == orig.settings.withKotlinxHtml, s.withTestJUnit5 == orig.settings.withTestJUnit5,
    s.withTestJUnit4 == orig.settings.withTestJUnit4, s.withTestUSpekX == orig.settings.withTestUSpekX,
    s.withCentralPublish == orig.settings.withCentralPublish,
    // presence survives as presence
    (lib.compose != null) == (orig.settings.compose != null),
    (lib.andro != null) == (orig.settings.andro != null),
    lib.repos.withKotlinxHtml == orig.settings.repos.withKotlinxHtml,
  )
  return checks.count { !it }
}

/**
 * Transcribing [LibAndroSettings] for the prototype surfaced a real bug in the published model:
 * `publishOneVariant` reads `!publishNoVariants && !publishNoVariants` — the second conjunct
 * should be `!publishAllVariants`. So today a lib with `publishVariant = "*"` reports BOTH
 * `publishAllVariants` and `publishOneVariant` true, and [defaultAndroLib] runs both publish paths.
 * Returns "<broken>|<fixed>" for the all-variants case.
 */
fun probePublishVariantBugTMP(): String {
  val broken = LibAndroSettings(publishVariant = "*")
  val fixed = LibAndroSettingsTMP(publishVariant = "*")
  return "${broken.publishOneVariant}|${fixed.publishOneVariant}"
}

/**
 * The cross-object derivation moved into a named function still produces the nested defaults.
 *
 * Deliberately NOT tested against `gradle.extLibDetails`: KGround's `settings.gradle.kts` passes
 * `compose = null`, so that comparison could not tell "the derivation broke" from "compose is
 * switched off here" — it would pass for the wrong reason. Instead this walks flag combinations
 * that the derivation actually reads (withJvm, withJs, withTestJUnit4/5) and compares the named
 * function against the constructor default it replaces. Returns "<combos>/<matches>".
 */
fun probeDerivedDefaultsTMP(): String {
  val combos = buildList {
    for (withJvm in listOf(true, false))
      for (withJs in listOf(true, false))
        for (withTestJUnit4 in listOf(true, false))
          add(LibSettings(withJvm = withJvm, withJs = withJs, withTestJUnit4 = withTestJUnit4))
  }
  val matches = combos.count { nested ->
    context(nested.toTMP()) { defaultComposeSettingsTMP() } == nested.compose?.toTMP()
  }
  return "${combos.size}/$matches"
}

/**
 * The shape a de-nested PUBLIC entry point would really have: several sibling context parameters
 * plus the `Project` extension receiver plus a value parameter. Probe 7 proved the flattened
 * coercion for ONE context parameter; this asks whether it survives three of them.
 *
 * Order under coercion is: context parameters first, then the extension receiver, then value params.
 */
context(d: LibDetailsTMP, s: LibSettingsTMP, r: LibReposSettingsTMP)
fun Project.probeSiblingEntryPointTMP(suffix: String): String =
  "${d.name}/${s.withJvm}/${r.withMavenCentral}/$name$suffix"

/** Same, with a trailing lambda — entry points all take one (addCommonMainDependencies etc.). */
context(d: LibDetailsTMP, s: LibSettingsTMP)
fun Project.probeSiblingWithLambdaTMP(addStuff: () -> String): String =
  "${d.name}/${s.withJvm}/$name/${addStuff()}"
