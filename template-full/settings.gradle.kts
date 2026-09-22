@file:Suppress("UnstableApiUsage")

import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib

// gradle.logSomeEventsToFile(rootProjectPath / "my.gradle.log")

// Careful with auto publishing fails/stack traces
val enableBuildScanPublishingOnFailure = true &&
  System.getenv("GITHUB_ACTIONS") == "true" &&
  // System.getenv("GITHUB_ACTIONS") != "true" &&
  true
// false


// region [[My Settings Stuff <~~]]
// ~~>".*/Deps\.kt"~~>"../../DepsKt"<~~ Example how to adjust regions (in case source region is a bit different).
// endregion [[My Settings Stuff <~~]]
// region [[My Settings Stuff]]

// https://docs.gradle.org/current/userguide/upgrading_version_9.html#opt_into_gradle_10_behavior_by_disabling_implicit_lookup_in_parent_projects
enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  // Absolute on purpose: this is then the SAME line in every project, with no ../.. depth to
  // adjust per repo -- which is the only thing the (unimplemented) [[My Settings Stuff <~~]] arrow
  // region ever existed to patch up, and the only per-project text this region still had.
  val enableLocalDepsKtInDir: File? =
    null
    // File("/home/marek/code/kotlin/DepsKt")
    // File("/home/marek/code/kotlin/DepsKt").takeIf { it.exists() }
  if (enableLocalDepsKtInDir != null) {
    logger.warn("Including local build $enableLocalDepsKtInDir")
    includeBuild(enableLocalDepsKtInDir)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.4.63" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.5.1" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    // Copied to a local at configuration time. `onlyIf` runs at the END of the build, so reading
    // the settings-script top-level `val` from inside it captures the script OBJECT, which the
    // configuration cache rejects: "cannot serialize Gradle script object references". A local is
    // captured by value.
    val enabled = enableBuildScanPublishingOnFailure
    publishing.onlyIf { enabled && it.buildResult.failures.isNotEmpty() }
  }
}

// endregion [[My Settings Stuff]]

// Which sibling modules this template includes.
val enableMppApp = true
val enableJvmCliApp = true
val enableAndroApp = true

// Which targets and features the lib itself gets.
val enableJs = true
val enableLinux = true // ok since 0.4.58: composeMain (runtime only) reaches linuxX64, Compose UI does not.
val enableCompose = true
val enableAndro = true
// Note: Andro works, but NOT under IntelliJ (with enabled andro plugin/jetpack compose plugin)
// Use Android Studio or disable andro target temporarily (or compile only with CLI).

gradle.extLib = lib(
  info = myLibInfo(
    name = "TemplateFull",
    description = "Template for multi platform projects.",
    githubUrl = "https://github.com/mareklangiewicz/KGround/tree/main/template-full",
    version = Ver(0, 0, 33),
  ),
  flags = LibFlags(
    withJs = enableJs,
    withLinuxX64 = enableLinux,
    withKotlinxHtml = true, // also used in common code
    withTestJUnit5 = true,
    withTestJUnit4OnAndroidDevice = true,
  ),
  withCompose = enableCompose,
  withAndro = enableAndro,
  compose = LibCompose(
    withComposeHtmlCore = enableJs,
    withComposeHtmlSvg = enableJs,
    withComposeTestHtmlUtils = enableJs,
    withComposeTestUi = true,
    withComposeTestUiJUnit4 = true,
    // withComposeTestUiJUnit5 = true, // What about this??
  ).takeIf { enableCompose },
  // Note: stated explicitly, so it does NOT pick up withKotlinxHtml from flags - same as before.
  repos = LibRepos(
    withComposeJbDev = true,
      // TODO: remove after update when new stable compose is published.
      //   BTW it's very slow, use gradle offline mode after syncing to run tasks faster
  ),
)

rootProject.name = "template-full"
include(":template-full-lib")
if (enableMppApp) include(":template-full-app")
if (enableJvmCliApp) include(":template-full-jvm-cli-app")
if (enableAndroApp) include(":template-full-andro-app")
