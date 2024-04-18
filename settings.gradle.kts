
rootProject.name = "KGround"

// Careful with auto publishing fails/stack traces
val buildScanPublishingAllowed =
  System.getenv("GITHUB_ACTIONS") == "true"
  // true
  // false

val kommandlineLocalAllowed = true

val kommandsamplesLocalAllowed = true

// UreRA|>".*/Deps\.kt"~~>"../DepsKt"<| TODO NOW: support ure replacements glued to special regions
// region [My Settings Stuff]

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }

  val depsDir = File(rootDir, "../DepsKt").normalize()
  val depsInclude =
    // depsDir.exists()
    false
  if (depsInclude) {
    logger.warn("Including local build $depsDir")
    includeBuild(depsDir)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.2.99" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "3.17.2" // https://docs.gradle.com/enterprise/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { buildScanPublishingAllowed && it.buildResult.failures.isNotEmpty() }
  }
}

// endregion [My Settings Stuff]

include(":kground")
include(":kgroundx")
include(":kground-io")
include(":kgroundx-io")
include(":kgroundx-maintenance")
include(":kgroundx-jupyter")


val kommandlineDir = File(rootDir, "../KommandLine/kommandline").normalize()
if (kommandlineLocalAllowed && kommandlineDir.exists()) {
  logger.warn("Adding local kommandline module.")
  include(":kommandline")
  project(":kommandline").projectDir = kommandlineDir
}

val kommandsamplesDir = File(rootDir, "../KommandLine/kommandsamples").normalize()
if (kommandsamplesLocalAllowed && kommandsamplesDir.exists()) {
  logger.warn("Adding local kommandsamples module.")
  include(":kommandsamples")
  project(":kommandsamples").projectDir = kommandsamplesDir
}
