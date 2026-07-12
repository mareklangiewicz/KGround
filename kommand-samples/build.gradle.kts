
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
  plugAll(plugs.KotlinMulti, plugs.VannikPublishNoVer)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

val details = rootExtLibDetails.copy(
  name = "Kommand Samples",
  description = "Additional samples for KommandLine."
)


defaultBuildTemplateForBasicMppLib {
  api(project(":kommand-line"))

  // TODO: Now needed for SampleLinesTests, move reflect stuff to kground.ureflect later
  implementation(Kotlin.reflect)
}

// FIXME NOW: do I need it?
// kotlin { js { nodejs() } }
