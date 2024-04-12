
rootProject.name = "KGround"

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }

  val depsDir = File("../DepsKt")
  val depsInclude =
    depsDir.exists()
    // false
  if (depsInclude) {
    logger.warn("Including local build $depsDir")
    includeBuild(depsDir)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.2.97" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "3.17.1" // https://docs.gradle.com/enterprise/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { // careful with publishing fails especially from my machine (privacy)
      true &&
        it.buildResult.failures.isNotEmpty() &&
        // it.buildResult.failures.isEmpty() &&
        System.getenv("GITHUB_ACTIONS") == "true" &&
        // System.getenv("GITHUB_ACTIONS") != "true" &&
        true
        // false
    }
  }
}

include(":kground")
include(":kgroundx")
include(":kground-io")
include(":kgroundx-io")
include(":kgroundx-maintenance")
include(":kgroundx-jupyter")


val kommandlineDir = File("../KommandLine/kommandline")
val kommandlineInclude =
  kommandlineDir.exists()
  // false
if (kommandlineInclude) {
  logger.warn("Adding local kommandline module.")
  include(":kommandline")
  project(":kommandline").projectDir = kommandlineDir
}

val kommandsamplesDir = File("../KommandLine/kommandsamples")
val kommandsamplesInclude =
  kommandsamplesDir.exists()
  // false
if (kommandsamplesInclude) {
  logger.warn("Adding local kommandsamples module.")
  include(":kommandsamples")
  project(":kommandsamples").projectDir = kommandsamplesDir
}
