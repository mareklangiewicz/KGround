
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib

rootProject.name = "KGround"

// Careful with auto publishing fails/stack traces
val buildScanPublishingAllowed =
  System.getenv("GITHUB_ACTIONS") == "true"
  // true
  // false

// region [[My Settings Stuff <~~]]
// ~~>".*/Deps\.kt"~~>"../DepsKt"<~~
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

val enableJs = true
val enableNative = true

gradle.extLib = lib(
  info = myLibInfo(
    name = "KGround",
    description = "Kotlin Common Ground.",
    githubUrl = "https://github.com/mareklangiewicz/KGround",
    version = Ver(0, 1, 35),
    // https://central.sonatype.com/artifact/pl.mareklangiewicz/kground/
    // https://github.com/mareklangiewicz/KGround/releases
  ),
  flags = LibFlags(
    withJs = enableJs,
    withLinuxX64 = enableNative,
    // withCentralPublish is GONE from LibFlags as of DepsKt 0.4.63. It was a per-REPO flag living in
    // the object every module clones for PLATFORM reasons, so `gradle.extLib.copy(flags = ..)`
    // carried it into modules that never asked -- that is how six of USpek's SAMPLE apps ended up
    // one green build away from permanent Maven Central coordinates. Each module that publishes now
    // says so itself: `publish = LibPublish(toCentral = true)` at its own
    // defaultBuildTemplateFor* call. All 13 included modules here do; a module that forgot would
    // FAIL at configuration time, not publish nothing quietly.
    // See DepsKt/docs/design/publish-intent-per-module.md.
  ),
  withCompose = false, // was: compose = null - presence, stated as presence
)

// template-logic is gone: the build templates come from DepsKt's :templatefun now, applied as a
// published plugin whose version rides along with the deps settings plugin pinned above.

// abcdk and tuplek were folded in from their own repos (mareklangiewicz/AbcdK, .../TupleK). They
// keep their artifact ids, so consumers of pl.mareklangiewicz:abcdk / :tuplek are unaffected -- but
// they now ride KGround's single version, which jumps them past their old 0.0.x line.
include(":abcdk")
include(":tuplek")

include(":kground")
include(":kgroundx")
include(":kground-io")
include(":kgroundx-io")
include(":kgroundx-maintenance")
include(":kgroundx-experiments")
include(":kgroundx-workflows")
include(":kgroundx-jupyter")
include(":kgroundx-app")

include(":kommand-line")
include(":kommand-samples")
