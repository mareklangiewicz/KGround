
// region [[Basic JVM App Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  plugAll(
    plugs.TemplateFun,
    plugs.KotlinJvm,
    plugs.JvmApp,
    plugs.VannikPublish,
  )
}

// endregion [[Basic JVM App Build Imports and Plugs]]

defaultBuildTemplateForBasicJvmApp(publish = LibPublish()) {
   implementation(project(":template-basic-lib"))
}
