@file:Suppress("UnstableApiUsage")

// gradle.logSomeEventsToFile(rootProject.projectDir.toOkioPath() / "my.gradle.log")

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

rootProject.name = "KommandLine"

include(":kommandline")
include(":kommandsamples")
include(":kommandjupyter")

val kgroundDir = File("../KGround/kground")
val kgroundInclude =
  kgroundDir.exists()
  // false
if (kgroundInclude) {
  logger.warn("Adding local kground module.")
  include(":kground")
  project(":kground").projectDir = kgroundDir
}
