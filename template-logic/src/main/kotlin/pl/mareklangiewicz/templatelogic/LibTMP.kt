package pl.mareklangiewicz.templatelogic

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

// region [[Lib TMP — sibling set, defaults and adapter]]

/**
 * The five siblings, carried together only so a build script can pass ONE value around and open the
 * scopes it needs. This is a bundle, NOT a nesting: nothing here derives from anything here, no
 * member reaches through another, and [compose] / [andro] are consumed by opening a scope rather
 * than by `!!`.
 *
 * Compare [LibDetails], where `settings` is a field, so `copy(settings = settings.copy(...))` is the
 * only way to change one flag and `context(details, details.settings)` is needed to supply two.
 */
data class LibTMP(
  val details: LibDetailsTMP,
  val settings: LibSettingsTMP,
  val repos: LibReposSettingsTMP,
  val compose: LibComposeSettingsTMP?,
  val andro: LibAndroSettingsTMP?,
)

/**
 * THE HARD PART, isolated. In the nested model these ten flags are default values of
 * [LibSettings.compose], so they can read `withJvm` / `withJs` / `withTestJUnit4` / `withTestJUnit5`
 * from the enclosing declaration. Siblings cannot do that — a default argument only sees earlier
 * parameters of the SAME declaration — so the derivation becomes an explicit function.
 *
 * That is a gain, not a workaround: the rule is now named, callable, and overridable at one place,
 * instead of being spelled out in a constructor default that fires only when you omit the argument.
 */
context(settings: LibSettingsTMP)
fun defaultComposeSettingsTMP() = with(settings) {
  LibComposeSettingsTMP(
    withComposeMaterial2 = withJvm,
    withComposeMaterial3 = withJvm,
    withComposeFullAnimation = withJvm,
    withComposeDesktop = withJvm,
    withComposeHtmlCore = withJs,
    withComposeHtmlSvg = withJs,
    withComposeTestUi = withTestJUnit4 || withTestJUnit5,
    withComposeTestUiJUnit4 = withTestJUnit4,
    withComposeTestUiJUnit5 = withTestJUnit5,
    withComposeTestHtmlUtils = withJs,
  )
}

/** The second cross-object derivation: [LibReposSettings.withKotlinxHtml] tracked [LibSettings.withKotlinxHtml]. */
context(settings: LibSettingsTMP)
fun defaultReposSettingsTMP() = LibReposSettingsTMP(
  withKotlinxHtml = settings.withKotlinxHtml,
  withComposeJbDev = false,
)

/**
 * Assembles the sibling set, applying the derivations above for whatever is not given explicitly.
 *
 * [withCompose] / [withAndro] say whether those scopes EXIST at all — the one thing the nested model
 * expressed as `null` and consumers had to re-check with `!!` or `withCompose`.
 */
fun libTMP(
  details: LibDetailsTMP,
  settings: LibSettingsTMP = LibSettingsTMP(),
  withCompose: Boolean = true,
  withAndro: Boolean = false,
  repos: LibReposSettingsTMP? = null,
  compose: LibComposeSettingsTMP? = null,
  andro: LibAndroSettingsTMP? = null,
): LibTMP = context(settings) {
  LibTMP(
    details = details,
    settings = settings,
    repos = repos ?: defaultReposSettingsTMP(),
    compose = compose ?: defaultComposeSettingsTMP().takeIf { withCompose },
    andro = andro ?: LibAndroSettingsTMP().takeIf { withAndro },
  )
}

/**
 * The migration seam. KGround's `settings.gradle.kts` still builds a published-DepsKt [LibDetails]
 * and stores it in `gradle.extLibDetails`; this un-nests that instance so the prototype can be
 * driven from the REAL data all 11 modules and all four templates already use, with no build script
 * changed. When DepsKt de-nests for real, this adapter is what gets deleted.
 */
fun LibDetails.toTMP(): LibTMP = LibTMP(
  details = LibDetailsTMP(
    name = name,
    group = group,
    description = description,
    authorId = authorId,
    authorName = authorName,
    authorEmail = authorEmail,
    githubUrl = githubUrl,
    licenceName = licenceName,
    licenceUrl = licenceUrl,
    version = version,
    namespace = namespace,
    appId = appId,
    appMainPackage = appMainPackage,
    appMainClass = appMainClass,
    appMainFun = appMainFun,
    appVerCode = appVerCode,
    appVerName = appVerName,
  ),
  settings = settings.toTMP(),
  repos = settings.repos.toTMP(),
  compose = settings.compose?.toTMP(),
  andro = settings.andro?.toTMP(),
)

fun LibSettings.toTMP() = LibSettingsTMP(
  withJvm = withJvm,
  withJvmVer = withJvmVer,
  withJs = withJs,
  withLinuxX64 = withLinuxX64,
  withKotlinxHtml = withKotlinxHtml,
  withTestJUnit5 = withTestJUnit5,
  withTestJUnit4 = withTestJUnit4,
  withTestJUnit4OnAndroidDevice = withTestJUnit4OnAndroidDevice,
  withTestUSpekX = withTestUSpekX,
  withTestGoogleTruth = withTestGoogleTruth,
  withTestMockitoKotlin = withTestMockitoKotlin,
  withCentralPublish = withCentralPublish,
)

fun LibComposeSettings.toTMP() = LibComposeSettingsTMP(
  withComposeUi = withComposeUi,
  withComposeFoundation = withComposeFoundation,
  withComposeMaterial2 = withComposeMaterial2,
  withComposeMaterial3 = withComposeMaterial3,
  withComposeMaterialIconsExtended = withComposeMaterialIconsExtended,
  withComposeFullAnimation = withComposeFullAnimation,
  withComposeDesktop = withComposeDesktop,
  withComposeDesktopComponents = withComposeDesktopComponents,
  withComposeHtmlCore = withComposeHtmlCore,
  withComposeHtmlSvg = withComposeHtmlSvg,
  withComposeTestUi = withComposeTestUi,
  withComposeTestUiJUnit4 = withComposeTestUiJUnit4,
  withComposeTestUiJUnit5 = withComposeTestUiJUnit5,
  withComposeTestHtmlUtils = withComposeTestHtmlUtils,
)

fun LibAndroSettings.toTMP() = LibAndroSettingsTMP(
  sdkCompilePreview = sdkCompilePreview,
  sdkCompile = sdkCompile,
  sdkTargetPreview = sdkTargetPreview,
  sdkTarget = sdkTarget,
  sdkMin = sdkMin,
  withAppCompat = withAppCompat,
  withLifecycle = withLifecycle,
  withActivityCompose = withActivityCompose,
  withMDC = withMDC,
  withTestEspresso = withTestEspresso,
  withTestRunner = withTestRunner,
  publishVariant = publishVariant,
)

@Suppress("DEPRECATION")
fun LibReposSettings.toTMP() = LibReposSettingsTMP(
  withMavenLocal = withMavenLocal,
  withMavenCentral = withMavenCentral,
  withGradle = withGradle,
  withGoogle = withGoogle,
  withKotlinx = withKotlinx,
  withKotlinxHtml = withKotlinxHtml,
  withComposeJbDev = withComposeJbDev,
  withKtorEap = withKtorEap,
  withJitpack = withJitpack,
)

// endregion [[Lib TMP — sibling set, defaults and adapter]]

// region [[Lib TMP — build-script facing helpers]]

/**
 * The sibling set for this build, un-nested from `gradle.extLibDetails`.
 *
 * Deliberately a PLAIN property returning a plain value: build scripts are compiled flagless
 * (Gradle pins script language version to 2.2), so nothing here may require context parameters at
 * the call site. Scripts do not need them — see [defaultBuildTemplateForBasicMppLib] below.
 */
val Gradle.extLibTMP: LibTMP get() = extLibDetails.toTMP()

/**
 * Sibling-model entry point. Same one-liner ergonomics as the [LibDetails] overload — `lib`
 * defaults, the trailing lambda stays trailing — so scripts gain the flat `copy` WITHOUT needing
 * context parameters, the flattened-reference coercion, or any Gradle change:
 *
 * ```kotlin
 * // nested: two statements, root named twice, inner type named explicitly
 * val settings = gradle.extLibDetails.settings.copy(withJs = false, withLinuxX64 = false)
 * val details = gradle.extLibDetails.copy(settings = settings)
 * defaultBuildTemplateForBasicMppLib(details) { ... }
 *
 * // sibling: one statement
 * defaultBuildTemplateForBasicMppLib(libTMP { it.copy(withJs = false, withLinuxX64 = false) }) { ... }
 * ```
 *
 * This is the point where the two halves of the de-nesting separate. The copy-dance win (symptom 1
 * in the design note) is pure DATA SHAPE and reaches build scripts today. The presence-as-scope win
 * needs context parameters and stays behind this boundary, where [LibSettingsTMP] and friends are
 * handed to the workers individually.
 */
fun Project.defaultBuildTemplateForBasicMppLib(
  lib: LibTMP = gradle.extLibTMP,
  ignoreCompose: Boolean = false,
  ignoreAndroConfig: Boolean = false,
  ignoreAndroPublish: Boolean = false,
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
): Unit = defaultBuildTemplateForBasicMppLib(
  details = lib.toNested(),
  ignoreCompose = ignoreCompose,
  ignoreAndroConfig = ignoreAndroConfig,
  ignoreAndroPublish = ignoreAndroPublish,
  addCommonMainDependencies = addCommonMainDependencies,
)

/**
 * Adjust just the platform/testing flags, leaving every other sibling alone — the flat single
 * `copy` the design note wants, with the root named ONCE.
 */
fun Project.libTMP(adjustSettings: (LibSettingsTMP) -> LibSettingsTMP): LibTMP =
  gradle.extLibTMP.let { it.copy(settings = adjustSettings(it.settings)) }

/**
 * Re-nests the siblings so the still-nested internals ([LibDetails]-based `defaultPublishing`,
 * `defaultGroupAndVerAndDescription`, `addRepos`) keep working unchanged. TEMPORARY: it disappears
 * together with [toTMP] once those migrate, or once DepsKt de-nests for real.
 */
fun LibTMP.toNested(): LibDetails = LibDetails(
  name = details.name,
  group = details.group,
  description = details.description,
  authorId = details.authorId,
  authorName = details.authorName,
  authorEmail = details.authorEmail,
  githubUrl = details.githubUrl,
  licenceName = details.licenceName,
  licenceUrl = details.licenceUrl,
  version = details.version,
  namespace = details.namespace,
  appId = details.appId,
  appMainPackage = details.appMainPackage,
  appMainClass = details.appMainClass,
  appMainFun = details.appMainFun,
  appVerCode = details.appVerCode,
  appVerName = details.appVerName,
  settings = LibSettings(
    withJvm = settings.withJvm,
    withJvmVer = settings.withJvmVer,
    withJs = settings.withJs,
    withLinuxX64 = settings.withLinuxX64,
    withKotlinxHtml = settings.withKotlinxHtml,
    withTestJUnit5 = settings.withTestJUnit5,
    withTestJUnit4 = settings.withTestJUnit4,
    withTestJUnit4OnAndroidDevice = settings.withTestJUnit4OnAndroidDevice,
    withTestUSpekX = settings.withTestUSpekX,
    withTestGoogleTruth = settings.withTestGoogleTruth,
    withTestMockitoKotlin = settings.withTestMockitoKotlin,
    withCentralPublish = settings.withCentralPublish,
    compose = compose?.toNested(),
    andro = andro?.toNested(),
    repos = repos.toNested(),
  ),
)

fun LibComposeSettingsTMP.toNested() = LibComposeSettings(
  withComposeUi = withComposeUi,
  withComposeFoundation = withComposeFoundation,
  withComposeMaterial2 = withComposeMaterial2,
  withComposeMaterial3 = withComposeMaterial3,
  withComposeMaterialIconsExtended = withComposeMaterialIconsExtended,
  withComposeFullAnimation = withComposeFullAnimation,
  withComposeDesktop = withComposeDesktop,
  withComposeDesktopComponents = withComposeDesktopComponents,
  withComposeHtmlCore = withComposeHtmlCore,
  withComposeHtmlSvg = withComposeHtmlSvg,
  withComposeTestUi = withComposeTestUi,
  withComposeTestUiJUnit4 = withComposeTestUiJUnit4,
  withComposeTestUiJUnit5 = withComposeTestUiJUnit5,
  withComposeTestHtmlUtils = withComposeTestHtmlUtils,
)

fun LibAndroSettingsTMP.toNested() = LibAndroSettings(
  sdkCompilePreview = sdkCompilePreview,
  sdkCompile = sdkCompile,
  sdkTargetPreview = sdkTargetPreview,
  sdkTarget = sdkTarget,
  sdkMin = sdkMin,
  withAppCompat = withAppCompat,
  withLifecycle = withLifecycle,
  withActivityCompose = withActivityCompose,
  withMDC = withMDC,
  withTestEspresso = withTestEspresso,
  withTestRunner = withTestRunner,
  publishVariant = publishVariant,
)

@Suppress("DEPRECATION")
fun LibReposSettingsTMP.toNested() = LibReposSettings(
  withMavenLocal = withMavenLocal,
  withMavenCentral = withMavenCentral,
  withGradle = withGradle,
  withGoogle = withGoogle,
  withKotlinx = withKotlinx,
  withKotlinxHtml = withKotlinxHtml,
  withComposeJbDev = withComposeJbDev,
  withKtorEap = withKtorEap,
  withJitpack = withJitpack,
)

// endregion [[Lib TMP — build-script facing helpers]]
