
// region [[Full Root Build Imports and Plugs]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*

plugins {
  plug(plugs.KotlinMulti) apply false
  plug(plugs.KotlinJvm) apply false
  plug(plugs.KotlinMultiCompose) apply false

  // plug(plugs.ComposeJb) apply false // ComposeJbEdge can be very slow to sync, clean, build (jb dev repo issue)
  id("org.jetbrains.compose") version "1.10.0-beta02"
  // TODO_later: Check again after compose update, because now default version fails with:
  // Cannot determine the version of Skiko for Compose '1.10.0-rc01'

  plug(plugs.AndroKmp) apply false
  plug(plugs.AndroApp) apply false
  plug(plugs.VannikPublish) apply false
}

// endregion [[Full Root Build Imports and Plugs]]

val enableJs = true
val enableLinux = false
val enableAndro = true
// Note: Andro works, but NOT under IntelliJ (with enabled andro plugin/jetpack compose plugin)
// Use Android Studio or disable andro target temporarily (or compile only with CLI).

defaultBuildTemplateForRootProject(
  myLibDetails(
    name = "TemplateRaw",
    description = "Raw template for multi platform projects.",
    githubUrl = "https://github.com/mareklangiewicz/KGround/tree/main/template-raw",
    version = Ver(0, 0, 34),
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
      ),
      andro = if (enableAndro) LibAndroSettings() else null,
    ),
  ),
)



// region [[Root Build Template]]

fun Project.defaultBuildTemplateForRootProject(details: LibDetails? = null) {
  details?.let {
    rootExtLibDetails = it
    defaultGroupAndVerAndDescription(it)
  }
}

// endregion [[Root Build Template]]
