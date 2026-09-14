package pl.mareklangiewicz.templatelogic

import pl.mareklangiewicz.deps.*

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
