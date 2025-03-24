
// region [[Full Root Build Imports and Plugs]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*

plugins {
  plug(plugs.KotlinMulti) apply false
  plug(plugs.KotlinJvm) apply false
  plug(plugs.KotlinMultiCompose) apply false
  plug(plugs.ComposeJb) apply false // ComposeJb(Edge) is very slow to sync, clean, build (jb dev repo issue)
  plug(plugs.AndroLib) apply false
  plug(plugs.AndroApp) apply false
  plug(plugs.NexusPublish)
}

// endregion [[Full Root Build Imports and Plugs]]

val enableJs = true
val enableNative = false
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
      withNativeLinux64 = enableNative,
      withKotlinxHtml = true, // also used in common code
      compose = LibComposeSettings(
        withComposeHtmlCore = enableJs,
        withComposeHtmlSvg = enableJs,
        withComposeTestHtmlUtils = enableJs,
      ),
      andro = if (enableAndro) LibAndroSettings(sdkCompilePreview = Vers.AndroSdkPreview) else null,
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
