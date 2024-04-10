pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
  val includeLocalDepsKtBuild =
    File("../DepsKt").exists()
    // false

  if (includeLocalDepsKtBuild) {
    logger.warn("Including local build ../DepsKt")
    includeBuild("../DepsKt")
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.2.96" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.enterprise") version "3.16.2" // https://docs.gradle.com/enterprise/gradle-plugin/
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    val scanPublishEnabled: Boolean =
      System.getenv("GITHUB_ACTIONS") == "true"
      // true // careful with publishing fails especially from my machine (privacy)

    publishOnFailureIf(scanPublishEnabled)
    // publishAlwaysIf(scanPublishEnabled)
  }
}

rootProject.name = "KGround"

include(":kground")
include(":kgroundx")
include(":kground-io")
include(":kgroundx-io")
include(":kgroundx-maintenance")
include(":kgroundx-jupyter")


val kommandlineDir = File("../KommandLine/kommandline")
val kommandsamplesDir = File("../KommandLine/kommandsamples")

val kommandlineLocal =
  kommandlineDir.exists()
  // false

val kommandsamplesLocal =
  kommandsamplesDir.exists()
  // false

if (kommandlineLocal) {
  logger.warn("Adding local kommandline module.")
  include(":kommandline")
  project(":kommandline").projectDir = kommandlineDir
}
if (kommandsamplesLocal) {
  logger.warn("Adding local kommandsamples module.")
  include(":kommandsamples")
  project(":kommandsamples").projectDir = kommandsamplesDir
}
