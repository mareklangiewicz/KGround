
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

defaultBuildTemplateForBasicMppLib(
  ignoreCompose = true, // necessary because I sometimes include this module locally from UWidgets project
) {
  api(Langiewicz.abcdk)
  api(Langiewicz.tuplek)
  api(Langiewicz.upue)
  api(KotlinX.datetime)
  api(KotlinX.coroutines_core)
  implementation(Kotlin.reflect)
}
