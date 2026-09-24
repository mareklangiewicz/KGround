
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib

rootProject.name = "KGround"

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

  // Opt-in through the environment, so this region is identical in every project and no flag has
  // to live above it and be kept in sync. Unset means off. To enable for one run:
  //   ENABLE_LOCAL_DEPSKT_IN_DIR=/home/marek/code/kotlin/DepsKt ./gradlew build
  val enableLocalDepsKtInDir = System.getenv("ENABLE_LOCAL_DEPSKT_IN_DIR")?.let { File(it).normalize() }
  // The env var reaches nested builds too, so DepsKt's own copy of this region sees it: skip self.
  // Pass a String: this scope's includeBuild takes only String, and a File silently resolves to the
  // outer Settings.includeBuild, a plain composite that never offers DepsKt's PLUGINS.
  if (enableLocalDepsKtInDir != null && enableLocalDepsKtInDir != rootDir.normalize()) {
    logger.warn("Including local build $enableLocalDepsKtInDir")
    includeBuild(enableLocalDepsKtInDir.path)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.4.65" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.6.0" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    // Opt-in through the environment; unset means no scan is ever published, which is what keeps
    // private repos safe without anyone remembering to switch them off. A public repo turns it on
    // in its own CI workflow:  ENABLE_BUILD_SCAN_PUBLISHING_ON_FAILURE=true
    // Read into a local at configuration time: `onlyIf` runs at the END of the build, and reading
    // a settings-script top-level `val` from there would capture the script OBJECT, which the
    // configuration cache rejects. A local is captured by value.
    val enabled = System.getenv("ENABLE_BUILD_SCAN_PUBLISHING_ON_FAILURE") == "true"
    publishing.onlyIf { enabled && it.buildResult.failures.isNotEmpty() }
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
    version = Ver(0, 1, 36),
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
