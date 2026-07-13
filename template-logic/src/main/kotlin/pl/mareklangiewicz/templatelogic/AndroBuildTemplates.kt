package pl.mareklangiewicz.templatelogic

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.dsl.*
import org.gradle.api.publish.*
import org.gradle.api.publish.maven.*
import org.gradle.kotlin.dsl.*
import com.android.build.api.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.defaults.*

// region [[Andro Common Build Template]]

/** @param ignoreCompose Should be set to true if compose mpp is configured instead of compose andro */
fun DependencyHandler.defaultAndroDeps(
  settings: LibSettings,
  ignoreCompose: Boolean = false,
  configuration: String = "implementation",
) {
  val andro = settings.andro ?: error("No andro settings.")
  addAll(
    configuration,
    AndroidX.Core.ktx,
    AndroidX.AppCompat.appcompat.takeIf { andro.withAppCompat },
    AndroidX.Activity.compose.takeIf { andro.withActivityCompose }, // this should not depend on ignoreCompose!
    AndroidX.Lifecycle.compiler.takeIf { andro.withLifecycle },
    AndroidX.Lifecycle.runtime_ktx.takeIf { andro.withLifecycle },
    // TODO_someday_maybe: more lifecycle related stuff by default (viewmodel, compose)?
    Com.Google.Android.Material.material.takeIf { andro.withMDC },
  )
  if (!ignoreCompose && settings.withCompose) {
    val compose = settings.compose!!
    addAllWithVer(
      configuration,
      Vers.ComposeAndro,
      AndroidX.Compose.Ui.ui,
      AndroidX.Compose.Ui.tooling,
      AndroidX.Compose.Ui.tooling_preview,
      AndroidX.Compose.Material.material.takeIf { compose.withComposeMaterial2 },
    )
    addAll(
      configuration,
      AndroidX.Compose.Material3.material3.takeIf { compose.withComposeMaterial3 },
    )
  }
}

/** @param ignoreCompose Should be set to true if compose mpp is configured instead of compose andro */
fun DependencyHandler.defaultAndroTestDeps(
  settings: LibSettings,
  ignoreCompose: Boolean = false,
  configuration: String = "testImplementation",
) {
  val andro = settings.andro ?: error("No andro settings.")
  addAll(
    configuration,
    AndroidX.Test.Espresso.core.takeIf { andro.withTestEspresso },
    Com.Google.Truth.truth.takeIf { settings.withTestGoogleTruth },
    AndroidX.Test.rules,
    AndroidX.Test.runner,
    AndroidX.Test.Ext.truth.takeIf { settings.withTestGoogleTruth },
    Org.Mockito.Kotlin.mockito_kotlin.takeIf { settings.withTestMockitoKotlin },
  )

  if (settings.withTestJUnit4) {
    addAll(
      configuration,
      Kotlin.test_junit.withVer(Vers.Kotlin),
      JUnit.junit,
      Langiewicz.uspekx_junit4.takeIf { settings.withTestUSpekX },
      AndroidX.Test.Ext.junit_ktx,
    )
  }
  // android doesn't fully support JUnit5, but adding deps anyway to be able to write JUnit5 dependent code
  if (settings.withTestJUnit5) {
    addAll(
      configuration,
      Kotlin.test_junit5.withVer(Vers.Kotlin),
      Org.JUnit.Jupiter.junit_jupiter_api,
      Org.JUnit.Jupiter.junit_jupiter_engine,
      Langiewicz.uspekx_junit5.takeIf { settings.withTestUSpekX },
    )
  }

  if (!ignoreCompose && settings.withCompose) addAllWithVer(
    configuration,
    vers.ComposeAndro,
    AndroidX.Compose.Ui.test,
    AndroidX.Compose.Ui.test_manifest,
    AndroidX.Compose.Ui.test_junit4.takeIf { settings.withTestJUnit4 },
  )
}

fun MutableSet<String>.defaultAndroExcludedResources() = addAll(
  listOf(
    "**/*.md",
    "**/attach_hotspot_windows.dll",
    "META-INF/licenses/**",
    "META-INF/AL2.0",
    "META-INF/LGPL2.1",
    "META-INF/kotlinx_coroutines_core.version",
  ),
)

fun CommonExtension.defaultCompileOptions(
  jvmVer: String? = null, // it's better to use jvmToolchain (normally done in fun allDefault)
) = compileOptions.apply {
  jvmVer?.let {
    sourceCompatibility = JavaVersion.toVersion(it)
    targetCompatibility = JavaVersion.toVersion(it)
  }
}

fun CommonExtension.defaultComposeStuff() {
  buildFeatures.compose = true
}

fun CommonExtension.defaultPackagingOptions() = packaging.apply {
  resources.excludes.defaultAndroExcludedResources()
}

/** Use template-andro/build.gradle.kts:fun defaultAndroLibPublishAllVariants() to create component with name "default". */
fun Project.defaultPublishingOfAndroLib(
  lib: LibDetails,
  componentName: String = "default",
) {
  afterEvaluate {
    extensions.configure<PublishingExtension> {
      publications.register<MavenPublication>(componentName) {
        from(components[componentName])
        pom { defaultPOM(lib) }
      }
    }
  }
}

fun Project.defaultPublishingOfAndroApp(
  lib: LibDetails,
  componentName: String = "release",
) = defaultPublishingOfAndroLib(lib, componentName)


// endregion [[Andro Common Build Template]]

// region [[Andro Lib Build Template]]

fun Project.defaultBuildTemplateForAndroLib(
  details: LibDetails = gradle.extLibDetails,
  addAndroMainDependencies: DependencyHandler.() -> Unit = {},
) {
  val andro = details.settings.andro ?: error("No andro settings.")
  repositories { addRepos(details.settings.repos) }
  extensions.configure<KotlinMultiplatformExtension> {
    androidTarget()
    jvmToolchain(details.settings.withJvmVer?.toInt() ?: 17) // works for jvm and android
  }
  extensions.configure<LibraryExtension> {
    defaultAndroLib(details)
  }
  dependencies {
    defaultAndroDeps(details.settings)
    defaultAndroTestDeps(details.settings)
    add("debugImplementation", AndroidX.Tracing.ktx) // https://github.com/android/android-test/issues/1755
    addAndroMainDependencies()
  }
  configurations.checkVerSync(warnOnly = true)
  tasks.defaultKotlinCompileOptions(
    jvmTargetVer = null, // jvmVer is set jvmToolchain in fun allDefault
  )
  defaultGroupAndVerAndDescription(details)
  if (andro.publishAllVariants) defaultPublishingOfAndroLib(details, "default")
  if (andro.publishOneVariant) defaultPublishingOfAndroLib(details, andro.publishVariant)
}

fun LibraryExtension.defaultAndroLib(
  details: LibDetails,
  ignoreCompose: Boolean = false,
  ignoreAndroPublish: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
) {
  val andro = details.settings.andro ?: error("No andro settings.")
  andro.sdkCompilePreview?.let { compileSdkPreview = it } ?: run { compileSdk = andro.sdkCompile }
  defaultCompileOptions(jvmVer = null) // actually it does nothing now. jvm ver is normally configured via jvmToolchain
  defaultDefaultConfig(details)
  defaultBuildTypes()
  details.settings.compose?.takeIf { !ignoreCompose }?.let { defaultComposeStuff() }
  defaultPackagingOptions()
  if (!ignoreAndroPublish && andro.publishAllVariants) defaultAndroLibPublishAllVariants()
  if (!ignoreAndroPublish && andro.publishOneVariant) defaultAndroLibPublishVariant(andro.publishVariant)
}

fun LibraryExtension.defaultDefaultConfig(details: LibDetails) = defaultConfig {
  val asettings = details.settings.andro ?: error("No andro settings.")
  namespace = details.namespace
  minSdk = asettings.sdkMin
  testInstrumentationRunner = asettings.withTestRunner
}

fun LibraryExtension.defaultBuildTypes() = buildTypes { release { isMinifyEnabled = false } }

fun LibraryExtension.defaultAndroLibPublishVariant(
  variant: String = "debug",
  withSources: Boolean = true,
  withJavadoc: Boolean = false,
) {
  publishing {
    singleVariant(variant) {
      if (withSources) withSourcesJar()
      if (withJavadoc) withJavadocJar()
    }
  }
}

fun LibraryExtension.defaultAndroLibPublishAllVariants(
  withSources: Boolean = true,
  withJavadoc: Boolean = false,
) {
  publishing {
    multipleVariants {
      allVariants()
      if (withSources) withSourcesJar()
      if (withJavadoc) withJavadocJar()
    }
  }
}

// endregion [[Andro Lib Build Template]]

// region [[Andro App Build Template]]

fun Project.defaultBuildTemplateForAndroApp(
  details: LibDetails = gradle.extLibDetails,
  addAndroDependencies: DependencyHandler.() -> Unit = {},
) {
  val andro = details.settings.andro ?: error("No andro settings.")
  require(!andro.publishAllVariants) { "Only single app variant can be published" }
  val variant = andro.publishVariant.takeIf { andro.publishOneVariant }
  repositories { addRepos(details.settings.repos) }
  extensions.configure<ApplicationExtension> {
    defaultAndroApp(details)
    variant?.let { defaultAndroAppPublishVariant(it) }
  }
  dependencies {
    defaultAndroDeps(details.settings)
    defaultAndroTestDeps(details.settings)
    add("debugImplementation", AndroidX.Tracing.ktx) // https://github.com/android/android-test/issues/1755
    addAndroDependencies()
  }
  configurations.checkVerSync(warnOnly = true)
  tasks.defaultKotlinCompileOptions(
    jvmTargetVer = null, // jvmVer is set jvmToolchain in fun allDefault
  )
  defaultGroupAndVerAndDescription(details)
  variant?.let { defaultPublishingOfAndroApp(details, it) }
}

fun ApplicationExtension.defaultAndroApp(
  details: LibDetails,
  ignoreCompose: Boolean = false,
) {
  val andro = details.settings.andro ?: error("No andro settings.")
  andro.sdkCompilePreview?.let { compileSdkPreview = it } ?: run { compileSdk = andro.sdkCompile }
  defaultDefaultConfig(details)
  defaultBuildTypes()
}

fun ApplicationExtension.defaultDefaultConfig(details: LibDetails) = defaultConfig {
  val asettings = details.settings.andro ?: error("No andro settings.")
  applicationId = details.appId
  namespace = details.namespace
  asettings.sdkTargetPreview?.let { targetSdkPreview = it } ?: run { targetSdk = asettings.sdkTarget }
  minSdk = asettings.sdkMin
  versionCode = details.appVerCode
  versionName = details.appVerName
  testInstrumentationRunner = asettings.withTestRunner
}

fun ApplicationExtension.defaultBuildTypes() = buildTypes { getByName("release") { isMinifyEnabled = false } }

fun ApplicationExtension.defaultAndroAppPublishVariant(
  variant: String = "debug",
  publishAPK: Boolean = true,
  publishAAB: Boolean = false,
) {
  require(!publishAAB || !publishAPK) { "Either APK or AAB can be published, but not both." }
  publishing { singleVariant(variant) { if (publishAPK) publishApk() } }
}

// endregion [[Andro App Build Template]]
