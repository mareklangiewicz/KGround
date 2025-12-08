
// region [[Full Root Build Imports and Plugs]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*

plugins {
  plug(plugs.KotlinMulti) apply false
  plug(plugs.KotlinJvm) apply false
  plug(plugs.KotlinMultiCompose) apply false

  plug(plugs.ComposeJbStable) apply false // ComposeJbEdge can be very slow to sync, clean, build (jb dev repo issue)
  // id("org.jetbrains.compose") version "1.10.0-beta02" apply false
  // TODO_later: Check again after compose update, because now default version fails with:
  // Cannot determine the version of Skiko for Compose '1.10.0-rc01'

  plug(plugs.AndroKmp) apply false
  plug(plugs.AndroApp) apply false
  plug(plugs.VannikPublish) apply false
}

// endregion [[Full Root Build Imports and Plugs]]

val enableJs = true
val enableLinux = false
val enableCompose = true
val enableAndro = true
// Note: Andro works, but NOT under IntelliJ (with enabled andro plugin/jetpack compose plugin)
// Use Android Studio or disable andro target temporarily (or compile only with CLI).

defaultBuildTemplateForRootProject(
  myLibDetails(
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
