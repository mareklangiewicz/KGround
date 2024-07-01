@file:Suppress("UnstableApiUsage")

rootProject.name = "KommandLine"


// Careful with auto publishing fails/stack traces
val buildScanPublishingAllowed =
  System.getenv("GITHUB_ACTIONS") == "true"
  // true
  // false

val kgroundModulesLocalAllowed =
  // true
  false


// gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")

// region [[My Settings Stuff <~~]]
// ~~>".*/Deps\.kt"~~>"../DepsKt"<~~
// endregion [[My Settings Stuff <~~]]
// region [[My Settings Stuff]]

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
  id("pl.mareklangiewicz.deps.settings") version "0.3.32" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "3.17.5" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { buildScanPublishingAllowed && it.buildResult.failures.isNotEmpty() }
  }
}

// endregion [[My Settings Stuff]]

include(":kommandline")
include(":kommandsamples")
include(":kommandjupyter")

val kgroundDir = File(rootDir, "../KGround/kground").normalize()
val kgroundIoDir = File(rootDir, "../KGround/kground-io").normalize()
if (kgroundModulesLocalAllowed && kgroundDir.exists() && kgroundIoDir.exists()) {
  logger.warn("Adding local kground module.")
  include(":kground")
  project(":kground").projectDir = kgroundDir
  logger.warn("Adding local kground-io module.")
  include(":kground-io")
  project(":kground-io").projectDir = kgroundIoDir
}
