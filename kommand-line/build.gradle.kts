
// region [[Basic MPP Lib Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatelogic.*

plugins {
  id("my-convention")
  plugAll(plugs.KotlinMulti, plugs.VannikPublish)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

val details = rootExtLibDetails.copy(
  name = "Kommand Line",
  description = "Kotlin DSL for popular CLI commands."
)

defaultBuildTemplateForBasicMppLib(details) {
  api(project(":kground"))
  api(project(":kground-io"))
}

// FIXME NOW: do I need it?
// kotlin { js { nodejs() } }
