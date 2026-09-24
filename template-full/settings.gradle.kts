@file:Suppress("UnstableApiUsage")

import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib


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
  id("pl.mareklangiewicz.deps.settings") version "0.4.63" // https://plugins.gradle.org/search?term=mareklangiewicz
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

// Which sibling modules this template includes.
val enableMppApp = true
val enableJvmCliApp = true
val enableAndroApp = true

// Which targets and features the lib itself gets.
val enableJs = true
val enableLinux = true // ok since 0.4.58: composeMain (runtime only) reaches linuxX64, Compose UI does not.
val enableCompose = true
val enableAndro = true
// Note: Andro works, but NOT under IntelliJ (with enabled andro plugin/jetpack compose plugin)
// Use Android Studio or disable andro target temporarily (or compile only with CLI).

gradle.extLib = lib(
  info = myLibInfo(
    name = "TemplateFull",
    description = "Template for multi platform projects.",
    githubUrl = "https://github.com/mareklangiewicz/KGround/tree/main/template-full",
    version = Ver(0, 0, 33),
  ),
  flags = LibFlags(
    withJs = enableJs,
    withLinuxX64 = enableLinux,
    withKotlinxHtml = true, // also used in common code
    withTestJUnit5 = true,
    withTestJUnit4OnAndroidDevice = true,
  ),
  withCompose = enableCompose,
  withAndro = enableAndro,
  compose = LibCompose(
    withComposeHtmlCore = enableJs,
    withComposeHtmlSvg = enableJs,
    withComposeTestHtmlUtils = enableJs,
    withComposeTestUi = true,
    withComposeTestUiJUnit4 = true,
    // withComposeTestUiJUnit5 = true, // What about this??
  ).takeIf { enableCompose },
  // Note: stated explicitly, so it does NOT pick up withKotlinxHtml from flags - same as before.
  repos = LibRepos(
    withComposeJbDev = true,
      // TODO: remove after update when new stable compose is published.
      //   BTW it's very slow, use gradle offline mode after syncing to run tasks faster
  ),
)

rootProject.name = "template-full"
include(":template-full-lib")
if (enableMppApp) include(":template-full-app")
if (enableJvmCliApp) include(":template-full-jvm-cli-app")
if (enableAndroApp) include(":template-full-andro-app")
