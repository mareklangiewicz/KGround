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
  id("pl.mareklangiewicz.deps.settings") version "0.4.63" // https://plugins.gradle.org/search?term=mareklangiewicz
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
val enableMppApp = false
val enableJvmCliApp = false
val enableAndroApp = true

// Which targets and features the lib itself gets.
val enableJs = false // android-only template: the lib has no js target.
val enableLinux = false
val enableCompose = true // Jetpack Compose, used directly by template-andro-app.
val enableAndro = true

gradle.extLib = lib(
  info = myLibInfo(
    name = "TemplateAndro",
    description = "Template for android projects.",
    githubUrl = "https://github.com/mareklangiewicz/KGround",
    version = Ver(0, 0, 17),
  ),
  flags = LibFlags(
    withJs = enableJs,
    withLinuxX64 = enableLinux,
    withTestJUnit4 = true,
    withTestJUnit5 = false,
    // Device tests take JUnit4 through their OWN flag -- the plain withTestJUnit4 does not reach
    // that configuration. Without this, @RunWith(USpekJUnit4Runner) in androidDeviceTest does not
    // resolve. See defaultAndroTestDeps' kdoc.
    withTestJUnit4OnAndroidDevice = true,
  ),
  withCompose = enableCompose,
  withAndro = enableAndro,
  andro = LibAndro().takeIf { enableAndro },
)

rootProject.name = "template-andro"
include(":template-andro-lib")
if (enableAndroApp) include(":template-andro-app")
