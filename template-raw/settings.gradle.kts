@file:Suppress("UnstableApiUsage")

import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLibDetails

rootProject.name = "template-raw"

// gradle.logSomeEventsToFile(rootProjectPath / "my.gradle.log")

// WARNING: Careful with auto publishing fails/stack traces (also on github after each push or sth)
val isCI = System.getenv("GITHUB_ACTIONS") == "true"
val allowBuildScanPublish = isCI
// val allowBuildScanPublish = !isCI
// val allowBuildScanPublish = false

val enableMppApp = true
val enableJvmCliApp = false
val enableAndroApp = true

val enableJs = true
val enableLinux = false // has to be false until JetBrains implements Compose UI for linuxX64..
val enableCompose = true // has to be true at least for now (too keep template-raw logic simple)
val enableAndro = true
// Note: Andro works, but NOT under IntelliJ (with enabled andro plugin/jetpack compose plugin)
// Use Android Studio or disable andro target temporarily (or compile only with CLI).

gradle.extLibDetails = myLibDetails(
  name = "TemplateRaw",
  description = "Raw template for multi platform projects.",
  githubUrl = "https://github.com/mareklangiewicz/KGround/tree/main/template-raw",
  version = Ver(0, 0, 35),
  settings = LibSettings(
    withJs = enableJs,
    withLinuxX64 = enableLinux,
    withKotlinxHtml = true, // also used in common code
    withTestJUnit5 = true,
    withTestJUnit4OnAndroidDevice = true,
    compose = LibComposeSettings(
      withComposeHtmlCore = enableJs,
      withComposeHtmlSvg = enableJs,
      withComposeTestHtmlUtils = enableJs,
      withComposeTestUi = true,
      withComposeTestUiJUnit4 = true,
      // withComposeTestUiJUnit5 = true, // What about this??
    ).takeIf { enableCompose },
    andro = LibAndroSettings().takeIf { enableAndro },
    repos = LibReposSettings(
      withComposeJbDev = true,
        // TODO: remove after update when new stable compose is published.
        //   BTW it's very slow, use gradle offline mode after syncing to run tasks faster
    ),
  ),
)


include(":template-raw-lib")
if (enableMppApp) include(":template-raw-app")
if (enableJvmCliApp) include(":template-raw-jvm-cli-app")
if (enableAndroApp) include(":template-raw-andro-app")

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
  id("pl.mareklangiewicz.deps.settings") version "0.4.10" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.3.2" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { allowBuildScanPublish && it.buildResult.failures.isNotEmpty() }
  }
}

// endregion [[My Settings Stuff]]
