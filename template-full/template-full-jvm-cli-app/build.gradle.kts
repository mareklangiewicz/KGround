
// region [[Basic JVM App Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun")
  plugAll(
    plugs.KotlinJvm,
    plugs.JvmApp,
    plugs.VannikPublish,
  )
}

// endregion [[Basic JVM App Build Imports and Plugs]]

defaultBuildTemplateForBasicJvmApp(ignoreCompose = true, ignoreAndroTarget = true) {
   implementation(project(":template-full-lib"))
}

// Compose Multiplatform 1.12.0 relocated its runtime layer into AndroidX and left EMPTY stubs behind
// (org.jetbrains.compose.runtime:runtime and :runtime-saveable have 0 class files; the androidx
// artifacts of the same name carry 742 and 22). Both families land on runtimeClasspath, and distTar
// flattens them into one lib/ dir, where the basenames collide. Drop the empty stubs rather than set
// a duplicatesStrategy, which would silently absorb future, real collisions too.
configurations.runtimeClasspath {
  exclude(group = "org.jetbrains.compose.runtime", module = "runtime")
  exclude(group = "org.jetbrains.compose.runtime", module = "runtime-saveable")
}
