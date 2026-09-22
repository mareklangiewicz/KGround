
// region [[Basic MPP Lib Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun")
  plugAll(plugs.KotlinMulti, plugs.VannikPublish)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

defaultBuildTemplateForBasicMppLib(
  publish = LibPublish(toCentral = true),
  ignoreCompose = true, // necessary because I sometimes include this module locally from UWidgets project
) {
  api(project(":abcdk"))
  api(project(":tuplek"))
  api(Langiewicz.upue)
  api(KotlinX.datetime)
  api(KotlinX.coroutines_core)
  implementation(Kotlin.reflect)
}
