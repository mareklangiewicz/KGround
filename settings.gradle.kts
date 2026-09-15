
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib

rootProject.name = "KGround"

// Careful with auto publishing fails/stack traces
val buildScanPublishingAllowed =
  System.getenv("GITHUB_ACTIONS") == "true"
  // true
  // false

// region [[My Settings Stuff <~~]]
// ~~>".*/Deps\.kt"~~>"../DepsKt"<~~
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

  val depsDir = File(rootDir, "../DepsKt").normalize()
  val depsInclude =
    depsDir.exists()
    // false
  if (depsInclude) {
    logger.warn("Including local build $depsDir")
    includeBuild(depsDir)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.4.26" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.5.1" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { buildScanPublishingAllowed && it.buildResult.failures.isNotEmpty() }
  }
}

// endregion [[My Settings Stuff]]

val enableJs = true
val enableNative = true

gradle.extLib = lib(
  info = myLibInfo(
    name = "KGround",
    description = "Kotlin Common Ground.",
    githubUrl = "https://github.com/mareklangiewicz/KGround",
    version = Ver(0, 1, 32),
    // https://central.sonatype.com/artifact/pl.mareklangiewicz/kground/
    // https://github.com/mareklangiewicz/KGround/releases
  ),
  flags = LibFlags(
    withJs = enableJs,
    withLinuxX64 = enableNative,
    withCentralPublish = true,
  ),
  withCompose = false, // was: compose = null - presence, stated as presence
)

// template-logic is gone: the build templates come from DepsKt's :templatefun now, reached through
// the composite include in pluginManagement above (and, once published, through a plugin version).
// probe-logic is what is left of it here -- see probe-logic/build.gradle.kts for why it exists.
includeBuild("probe-logic")

include(":kground")
include(":kgroundx")
include(":kground-io")
include(":kgroundx-io")
include(":kgroundx-maintenance")
include(":kgroundx-experiments")
include(":kgroundx-workflows")
include(":kgroundx-jupyter")
include(":kgroundx-app")

include(":kommand-line")
include(":kommand-samples")
