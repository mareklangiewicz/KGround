
// region [[Basic Root Build Imports and Plugs]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*

plugins {
  plug(plugs.KotlinMulti) apply false
  plug(plugs.KotlinJvm) apply false
  plug(plugs.NexusPublish)
}

// endregion [[Basic Root Build Imports and Plugs]]

val enableJs = true
val enableNative = true

defaultBuildTemplateForRootProject(
  myLibDetails(
    name = "TemplateBasic",
    description = "Template for basic multi platform projects. No android or compose here.",
    githubUrl = "https://github.com/mareklangiewicz/KGround/tree/main/template-basic",
    version = Ver(0, 0, 1),
    settings = LibSettings(
      withJs = enableJs,
      withNativeLinux64 = enableNative,
      withKotlinxHtml = true, // also used in common code
      compose = null,
      andro = null,
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
