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
val enableAndroApp = false

// Which targets and features the lib itself gets.
val enableJs = true
val enableLinux = true
val enableCompose = false // this template is deliberately the plain one: no compose, no android.
val enableAndro = false

gradle.extLib = lib(
  info = myLibInfo(
    name = "TemplateBasic",
    description = "Template for basic multi platform projects. No android or compose here.",
    githubUrl = "https://github.com/mareklangiewicz/KGround/tree/main/template-basic",
    version = Ver(0, 0, 1),
  ),
  flags = LibFlags(
    withJs = enableJs,
    withLinuxX64 = enableLinux,
    withKotlinxHtml = true, // also used in common code
  ),
  withCompose = enableCompose,
  withAndro = enableAndro,
)

rootProject.name = "template-basic"
include(":template-basic-lib")
if (enableMppApp) include(":template-basic-app")
if (enableJvmCliApp) include(":template-basic-jvm-cli-app")
