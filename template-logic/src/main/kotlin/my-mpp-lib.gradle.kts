import pl.mareklangiewicz.templatelogic.*

// PROTOTYPE (branch only) -- roadmap idea #2, the "persona" precompiled script plugin.
//
// Carries the whole Basic MPP Lib shape: the plugins, and the build template call. A
// consumer script is then `plugins { id("my-mpp-lib") }` plus an optional myMppLib { }
// block -- no imports, no plugAll, no gradle.extLibDetails, no copy dance.
//
// This file is compiled by the `kotlin-dsl` plugin as ordinary Kotlin source, so it gets
// -Xcontext-parameters. That is the whole reason personas are the only route to context
// parameters near root scripts: a consumer build.gradle.kts never can be.

plugins {
  id("my-convention")
  id("org.jetbrains.kotlin.multiplatform")
  id("com.vanniktech.maven.publish")
}

val persona = extensions.create<MyMppLibPersona>("myMppLib")

// Deferred on purpose: the myMppLib { } block below in the consumer script has not run
// yet at this point, so the template call has to wait until the project is evaluated.
afterEvaluate {
  defaultBuildTemplateForBasicMppLib(
    details = persona.detailsFrom(project),
    addCommonMainDependencies = persona.addCommonMainDependencies,
  )
  extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
    sourceSets.getByName("jvmMain").dependencies(persona.addJvmMainDependencies)
  }
}
