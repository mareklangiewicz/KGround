@file:Suppress("UnstableApiUsage")

import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib

// gradle.logSomeEventsToFile(rootProjectPath / "my.gradle.log")

// Careful with auto publishing fails/stack traces
val buildScanPublishingAllowed = true &&
  System.getenv("GITHUB_ACTIONS") == "true" &&
  // System.getenv("GITHUB_ACTIONS") != "true" &&
  true
// false


// region [[My Settings Stuff <~~]]
// ~~>".*/Deps\.kt"~~>"../../DepsKt"<~~ Example how to adjust regions (in case source region is a bit different).
// endregion [[My Settings Stuff <~~]]
// region [[My Settings Stuff]]

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  val depsDir = File(rootDir, "../../DepsKt").normalize()
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
val enableLinux = true

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
  withCompose = false, // was: compose = null
  // andro is absent by default - was: andro = null
)

rootProject.name = "template-basic"
include(":template-basic-lib")
include(":template-basic-app")
include(":template-basic-jvm-app")
