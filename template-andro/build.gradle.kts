
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

defaultBuildTemplateForRootProject(
  myLibDetails(
    name = "TemplateAndro",
    description = "Template for android projects.",
    githubUrl = "https://github.com/mareklangiewicz/KGround",
    version = Ver(0, 0, 16),
    settings = LibSettings(
      withTestJUnit4 = true,
      withTestJUnit5 = false,
      andro = LibAndroSettings(
        sdkCompilePreview = Vers.AndroSdkPreview,
        publishVariant = "debug",
      ),
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
