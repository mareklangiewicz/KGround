
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
    plugs.VannikPublish,
  )
}

// endregion [[Basic JVM App Build Imports and Plugs]]

defaultBuildTemplateForBasicJvmApp(gradle.extLibDetails, ignoreCompose = true, ignoreAndroTarget = true) {
   implementation(project(":template-raw-lib"))
}
