@file:Suppress("UnstableApiUsage")

// gradle.logSomeEventsToFile(rootProjectPath / "my.gradle.log")

// Careful with auto publishing fails/stack traces
val buildScanPublishingAllowed = true &&
  System.getenv("GITHUB_ACTIONS") == "true" &&
  // System.getenv("GITHUB_ACTIONS") != "true" &&
  true
// false

import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLibDetails

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
  id("pl.mareklangiewicz.deps.settings") version "0.4.23" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.5.0" // https://docs.gradle.com/develocity/gradle-plugin/
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
val enableLinux = false
val enableCompose = true
val enableAndro = true
// Note: Andro works, but NOT under IntelliJ (with enabled andro plugin/jetpack compose plugin)
// Use Android Studio or disable andro target temporarily (or compile only with CLI).

gradle.extLibDetails = myLibDetails(
  name = "TemplateFull",
  description = "Template for multi platform projects.",
  githubUrl = "https://github.com/mareklangiewicz/KGround/tree/main/template-full",
  version = Ver(0, 0, 33),
  settings = LibSettings(
    withJs = enableJs,
    withLinuxX64 = enableLinux,
    withKotlinxHtml = true, // also used in common code
    compose = LibComposeSettings(
      withComposeHtmlCore = enableJs,
      withComposeHtmlSvg = enableJs,
      withComposeTestHtmlUtils = enableJs,
    ).takeIf { enableCompose },
    andro = LibAndroSettings().takeIf { enableAndro },
  ),
)

rootProject.name = "template-full"
includeBuild("../template-logic")
include(":template-full-lib")
// include(":template-full-app")
// include(":template-full-jvm-cli-app")
