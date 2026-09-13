
// region [[Basic JVM App Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatelogic.*

plugins {
  id("my-convention")
  plugAll(
    plugs.KotlinJvm,
    plugs.JvmApp,
    plugs.VannikPublishNoVer,
  )
}

// endregion [[Basic JVM App Build Imports and Plugs]]

defaultBuildTemplateForBasicJvmApp {
  implementation(project(":kgroundx-jupyter"))
  implementation(project(":kgroundx-workflows"))
  implementation(project(":kgroundx-experiments"))
  implementation(Com.GitHub.Ajalt.Clikt.clikt)
}
