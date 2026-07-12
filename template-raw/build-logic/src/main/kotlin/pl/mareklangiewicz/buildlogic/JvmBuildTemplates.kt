package pl.mareklangiewicz.buildlogic

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.dsl.*
import org.gradle.api.plugins.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.gradle.kotlin.dsl.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.defaults.*

// region [[JVM Module Build Template]]

/**
 * Only for very standard small jvm libs. In most cases it's better to not use this function.
 *
 * These ignoreXXX flags are hacky, but needed. see [jvmOnlyDefault] kdoc for details.
 */
fun Project.defaultBuildTemplateForBasicJvmLib(
  details: LibDetails = rootExtLibDetails,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say THAT he wants to ignore android target.
  addJvmDependencies: DependencyHandler.() -> Unit = {},
) {
  require(ignoreCompose || details.settings.compose == null) { "defaultBuildTemplateForBasicJvmLib can NOT configure compose stuff" }
  require(ignoreAndroTarget || details.settings.andro == null) { "defaultBuildTemplateForBasicJvmLib can NOT configure android target" }
  repositories { addRepos(details.settings.repos) }
  defaultGroupAndVerAndDescription(details)
  extensions.configure<KotlinJvmProjectExtension> {
    jvmOnlyDefault(
      settings = details.settings,
      ignoreCompose = ignoreCompose,
      ignoreAndroTarget = ignoreAndroTarget,
      addJvmDependencies = addJvmDependencies,
    )
  }
  configurations.checkVerSync(warnOnly = true)
  tasks.defaultKotlinCompileOptions(jvmTargetVer = null) // jvmVer is set in fun jvmDefault using jvmToolchain
  tasks.defaultTestsOptions(onJvmUseJUnitPlatform = details.settings.withTestJUnit5)
  if (plugins.hasPlugin("com.vanniktech.maven.publish")) defaultPublishing(details)
  else println("JVM Module ${name}: publishing (and signing) disabled")
}

/**
 * Only for very standard small jvm libs. In most cases it's better to not use this function.
 *
 * These ignoreXXX flags are hacky, but needed because we want to inject this code also to such build files,
 * where plugins for compose and/or android are not applied at all, so compose/android stuff should be explicitly ignored.
 */
fun KotlinJvmProjectExtension.jvmOnlyDefault(
  settings: LibSettings,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  addJvmDependencies: DependencyHandler.() -> Unit = {},
) = with(settings) {
  require(ignoreCompose || compose == null) { "jvmOnlyDefault can NOT configure compose stuff" }
  require(ignoreAndroTarget || settings.andro == null) { "jvmOnlyDefault can NOT configure android target" }
  withJvmVer?.let { jvmToolchain(it.toInt()) } // works for jvm and android
  project.dependencies.apply {
    if (withKotlinxHtml) add("implementation", KotlinX.html)
    add("testImplementation", Kotlin.test)
    if (withTestUSpekX) add("testImplementation", Langiewicz.uspekx)
    if (withTestJUnit4) add("testImplementation", JUnit.junit)
    if (withTestJUnit5) {
      add("testImplementation", Org.JUnit.Jupiter.junit_jupiter_engine)
      add("testRuntimeOnly", Org.JUnit.Platform.junit_platform_launcher)
    }
    if (withTestUSpekX) {
      add("testImplementation", Langiewicz.uspekx)
      if (withTestJUnit4) add("testImplementation", Langiewicz.uspekx_junit4)
      if (withTestJUnit5) add("testImplementation", Langiewicz.uspekx_junit5)
    }
    if (withTestGoogleTruth) add("testImplementation", Com.Google.Truth.truth)
    if (withTestMockitoKotlin) add("testImplementation", Org.Mockito.Kotlin.mockito_kotlin)
    addJvmDependencies()
  }
}

// endregion [[JVM Module Build Template]]

// region [[JVM App Build Template]]

fun Project.defaultBuildTemplateForBasicJvmApp(
  details: LibDetails = rootExtLibDetails,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say THAT he wants to ignore android target.
  addJvmDependencies: DependencyHandler.() -> Unit = {},
) {
  defaultBuildTemplateForBasicJvmLib(details, ignoreCompose, ignoreAndroTarget, addJvmDependencies)
  extensions.configure<JavaApplication> {
    mainClass.set(details.run { "$appMainPackage.$appMainClass" })
  }
}

// endregion [[JVM App Build Template]]
