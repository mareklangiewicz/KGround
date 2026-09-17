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

// https://docs.gradle.org/current/userguide/upgrading_version_9.html#opt_into_gradle_10_behavior_by_disabling_implicit_lookup_in_parent_projects
enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  val depsDir = File(rootDir, "../../DepsKt").normalize()
  val depsInclude =
    // depsDir.exists()
    false
  if (depsInclude) {
    logger.warn("Including local build $depsDir")
    includeBuild(depsDir)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.4.61" // https://plugins.gradle.org/search?term=mareklangiewicz
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
