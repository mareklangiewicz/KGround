
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

defaultBuildTemplateForBasicJvmApp(publish = LibPublish(toCentral = true)) {
  implementation(project(":kgroundx-jupyter"))
  implementation(project(":kgroundx-workflows"))
  implementation(project(":kgroundx-experiments"))
  implementation(Com.GitHub.Ajalt.Clikt.clikt)
}

// This is what `kgroundx --version` reads at runtime. Without it the manifest has no version at
// all, Package.getImplementationVersion() is null, and the only way to tell which build is on PATH
// is to look at the jar file names.
tasks.jar {
  manifest.attributes(
    "Implementation-Title" to "kgroundx",
    "Implementation-Version" to project.version.toString(),
  )
}
