// region [[Basic Root Build Imports and Plugs]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.templatelogic.*

plugins {
  id("my-convention") apply false
  plug(plugs.KotlinMulti) apply false
  plug(plugs.KotlinJvm) apply false
}

// endregion [[Basic Root Build Imports and Plugs]]

val enableJs = true
val enableNative = true

defaultBuildTemplateForRootProject(
  myLibDetails(
    name = "KGround",
    description = "Kotlin Common Ground.",
    githubUrl = "https://github.com/mareklangiewicz/KGround",
    version = Ver(0, 1, 29),
    // https://central.sonatype.com/artifact/pl.mareklangiewicz/kground/
    // https://github.com/mareklangiewicz/KGround/releases
    settings = LibSettings(
      withJs = enableJs,
      withLinuxX64 = enableNative,
      compose = null,
      withCentralPublish = true,
    ),
  ),
)
