package pl.mareklangiewicz.templatelogic

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
  lib: LibTMP = gradle.extLibTMP,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say THAT he wants to ignore android target.
  addJvmDependencies: DependencyHandlerScope.() -> Unit = {},
): Unit = context(lib.details, lib.settings) {
  require(ignoreCompose || lib.compose == null) { "defaultBuildTemplateForBasicJvmLib can NOT configure compose stuff" }
  require(ignoreAndroTarget || lib.andro == null) { "defaultBuildTemplateForBasicJvmLib can NOT configure android target" }
  repositories { context(lib.repos) { addRepos() } }
  defaultGroupAndVerAndDescriptionTMP()
  extensions.configure<KotlinJvmProjectExtension> {
    // The whole opt-out: the jvm/testing flags are in scope and NOTHING else is. There is no
    // ignoreCompose/ignoreAndroTarget to forward any more, because there is nothing to ignore —
    // `compose` and `andro` are not reachable from [LibSettingsTMP] at all.
    jvmOnlyDefault(addJvmDependencies = addJvmDependencies)
  }
  configurations.checkVerSync(warnOnly = true)
  tasks.defaultKotlinCompileOptions(jvmTargetVer = null) // jvmVer is set in fun jvmDefault using jvmToolchain
  tasks.defaultTestsOptions(onJvmUseJUnitPlatform = lib.settings.withTestJUnit5)
  if (plugins.hasPlugin("com.vanniktech.maven.publish")) defaultPublishing()
  else println("JVM Module ${name}: publishing (and signing) disabled")
}
/** Nested-model compat shim: un-nest ONCE at the top, siblings below. No default for [details] (finding 7). */
fun Project.defaultBuildTemplateForBasicJvmLib(
  details: LibDetails,
  ignoreCompose: Boolean = false,
  ignoreAndroTarget: Boolean = false,
  addJvmDependencies: DependencyHandlerScope.() -> Unit = {},
): Unit = defaultBuildTemplateForBasicJvmLib(
  lib = details.toTMP(),
  ignoreCompose = ignoreCompose,
  ignoreAndroTarget = ignoreAndroTarget,
  addJvmDependencies = addJvmDependencies,
)


/**
 * Only for very standard small jvm libs. In most cases it's better to not use this function.
 *
 * MIGRATED to the sibling model ([LibSettingsTMP]) — the first entry point to move. Both
 * `ignoreCompose` and `ignoreAndroTarget`, and both `require`s they guarded, are GONE:
 *
 * ```
 * require(ignoreCompose || compose == null) { "jvmOnlyDefault can NOT configure compose stuff" }
 * require(ignoreAndroTarget || settings.andro == null) { "jvmOnlyDefault can NOT configure android target" }
 * ```
 *
 * The old kdoc called the flags "hacky, but needed because we want to inject this code also to such
 * build files, where plugins for compose and/or android are not applied at all". That need came
 * entirely from NESTING: a caller holding `LibSettings` could not hand over the jvm flags without
 * also handing over `compose` and `andro`, so the only way to say "those do not apply here" was a
 * boolean plus a runtime check. As a sibling, [LibSettingsTMP] carries no `compose` and no `andro`
 * at all — this function cannot configure them because it cannot NAME them, and the caller opts out
 * by simply not opening those scopes. A runtime `require` became the absence of a parameter.
 */
context(settings: LibSettingsTMP)
fun KotlinJvmProjectExtension.jvmOnlyDefault(
  addJvmDependencies: DependencyHandlerScope.() -> Unit = {},
) = with(settings) {
  withJvmVer?.let { jvmToolchain(it.toInt()) } // works for jvm and android
  project.dependencies.apply {
    val scope = DependencyHandlerScope.of(this)
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
    scope.addJvmDependencies()
  }
}

// endregion [[JVM Module Build Template]]

// region [[JVM App Build Template]]

fun Project.defaultBuildTemplateForBasicJvmApp(
  lib: LibTMP = gradle.extLibTMP,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say THAT he wants to ignore android target.
  addJvmDependencies: DependencyHandlerScope.() -> Unit = {},
): Unit = context(lib.details, lib.settings) {
  defaultBuildTemplateForBasicJvmLib(lib, ignoreCompose, ignoreAndroTarget, addJvmDependencies)
  extensions.configure<JavaApplication> {
    mainClass.set(lib.details.run { "$appMainPackage.$appMainClass" })
  }
}
/** Nested-model compat shim: un-nest ONCE at the top, siblings below. No default for [details] (finding 7). */
fun Project.defaultBuildTemplateForBasicJvmApp(
  details: LibDetails,
  ignoreCompose: Boolean = false,
  ignoreAndroTarget: Boolean = false,
  addJvmDependencies: DependencyHandlerScope.() -> Unit = {},
): Unit = defaultBuildTemplateForBasicJvmApp(
  lib = details.toTMP(),
  ignoreCompose = ignoreCompose,
  ignoreAndroTarget = ignoreAndroTarget,
  addJvmDependencies = addJvmDependencies,
)


// endregion [[JVM App Build Template]]
