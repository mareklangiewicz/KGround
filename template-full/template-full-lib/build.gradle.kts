
// region [[Full MPP Lib Build Imports and Plugs]]

import com.android.build.api.dsl.*
import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

plugins {
  plugAll(
    plugs.KotlinMulti,
    plugs.KotlinMultiCompose,
    plugs.ComposeJbNoVer,
    plugs.VannikPublish,
  )
  plug(plugs.AndroLibNoVer) apply false // will be applied conditionally depending on LibSettings
}

// endregion [[Full MPP Lib Build Imports and Plugs]]


defaultBuildTemplateForFullMppLib()

// region [[Full MPP Lib Build Template]]

fun Project.defaultBuildTemplateForFullMppLib(
  details: LibDetails = rootExtLibDetails,
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
  if (details.settings.withAndro) {
    apply(plugin = plugs.AndroLibNoVer.group) // group is actually id for plugins
    // TODO_later: try to move the rest of andro config from below here
  }
  defaultBuildTemplateForComposeMppLib(
    details = details,
    ignoreAndroConfig = true, // andro configured below
    ignoreAndroPublish = true, // andro publishing configured below (or ignored again, but below in defaultAndroLib)
    addCommonMainDependencies = addCommonMainDependencies,
  )

  if (details.settings.withAndro) {
    extensions.configure<LibraryExtension> {
      defaultAndroLib(
        details,
        ignoreCompose = true, // compose mpp configured already
        ignoreAndroPublish = true,
          // FIXME: maybe it's fine to publish in andro way here too (full mpp lib case),
          //  but let's analyze/test publications more before doing that (commiting to: ignoreAndroPublish = false).
      )
    }

    // this is "single platform way" / "android way" to declare deps,
    // it would be more "correct" to configure everything "mpp way" (android deps too),
    // but it's more important to reuse andro related functions like "fun defaultAndroDeps"
    // (trust me future Marek: I've tried this already :) )
    dependencies {
      // ignoreCompose because we have compose configured mpp way already.
      defaultAndroDeps(details.settings, ignoreCompose = true)
      defaultAndroTestDeps(details.settings, ignoreCompose = true)
    }
  }
}

// endregion [[Full MPP Lib Build Template]]


// region [[Kotlin Module Build Template]]

// Kind of experimental/temporary.. not sure how it will evolve yet,
// but currently I need these kind of substitutions/locals often enough
// especially when updating kground <-> kommandline (trans deps issues)
fun Project.setMyWeirdSubstitutions(
  vararg rules: Pair<String, String>,
  myProjectsGroup: String = "pl.mareklangiewicz",
  tryToUseLocalProjects: Boolean = true,
) {
  val foundLocalProjects: Map<String, Project?> =
    if (tryToUseLocalProjects) rules.associate { it.first to findProject(":${it.first}") }
    else emptyMap()
  configurations.all {
    resolutionStrategy.dependencySubstitution {
      for ((projName, projVer) in rules)
        substitute(module("$myProjectsGroup:$projName"))
          .using(
            // Note: there are different fun in gradle: Project.project; DependencySubstitution.project
            if (foundLocalProjects[projName] != null) project(":$projName")
            else module("$myProjectsGroup:$projName:$projVer")
          )
    }
  }
}

fun RepositoryHandler.addRepos(settings: LibReposSettings) = with(settings) {
  @Suppress("DEPRECATION")
  if (withMavenLocal) mavenLocal()
  if (withMavenCentral) mavenCentral()
  if (withGradle) gradlePluginPortal()
  if (withGoogle) google()
  if (withKotlinx) maven(repos.kotlinx)
  if (withKotlinxHtml) maven(repos.kotlinxHtml)
  if (withComposeJbDev) maven(repos.composeJbDev)
  if (withKtorEap) maven(repos.ktorEap)
  if (withJitpack) maven(repos.jitpack)
}

// TODO_maybe: doc says it could be now also applied globally instead for each task (and it works for andro too)
//   But it's only for jvm+andro, so probably this is better:
//   https://kotlinlang.org/docs/gradle-compiler-options.html#for-all-kotlin-compilation-tasks
fun TaskCollection<Task>.defaultKotlinCompileOptions(
  apiVer: KotlinVersion = KotlinVersion.KOTLIN_2_1,
  jvmTargetVer: String? = null, // it's better to use jvmToolchain (normally done in fun allDefault)
  renderInternalDiagnosticNames: Boolean = false,
) = withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  compilerOptions {
    apiVersion.set(apiVer)
    jvmTargetVer?.let { jvmTarget = JvmTarget.fromTarget(it) }
    if (renderInternalDiagnosticNames) freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
    // useful, for example, to suppress some errors when accessing internal code from some library, like:
    // @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE", "EXPOSED_PROPERTY_TYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
  }
}

fun TaskCollection<Task>.defaultTestsOptions(
  printStandardStreams: Boolean = true,
  printStackTraces: Boolean = true,
  onJvmUseJUnitPlatform: Boolean = true,
) = withType<AbstractTestTask>().configureEach {
  testLogging {
    showStandardStreams = printStandardStreams
    showStackTraces = printStackTraces
  }
  if (onJvmUseJUnitPlatform) (this as? Test)?.useJUnitPlatform()
}

// Provide artifacts information requited by Maven Central
fun MavenPom.defaultPOM(lib: LibDetails) {
  name put lib.name
  description put lib.description
  url put lib.githubUrl

  licenses {
    license {
      name put lib.licenceName
      url put lib.licenceUrl
    }
  }
  developers {
    developer {
      id put lib.authorId
      name put lib.authorName
      email put lib.authorEmail
    }
  }
  scm { url put lib.githubUrl }
}

fun Project.defaultPublishing(lib: LibDetails) = extensions.configure<MavenPublishBaseExtension> {
  propertiesTryOverride("signingInMemoryKey", "signingInMemoryKeyPassword", "mavenCentralPassword")
  if (lib.settings.withSonatypeOssPublishing)
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
  signAllPublications()
  // Note: artifactId is not lib.name but current project.name (module name)
  coordinates(groupId = lib.group, artifactId = name, version = lib.version.str)
  pom { defaultPOM(lib) }
}

// endregion [[Kotlin Module Build Template]]

// region [[MPP Module Build Template]]

/**
 * Only for very standard small libs. In most cases it's better to not use this function.
 *
 * These ignoreXXX flags are hacky, but needed. see [allDefault] kdoc for details.
 */
fun Project.defaultBuildTemplateForBasicMppLib(
  details: LibDetails = rootExtLibDetails,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
  ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  ignoreAndroPublish: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
  require(ignoreCompose || details.settings.compose == null) { "defaultBuildTemplateForBasicMppLib can not configure compose stuff" }
  details.settings.andro?.let {
    require(ignoreAndroConfig) { "defaultBuildTemplateForBasicMppLib can not configure android stuff (besides just adding target)" }
    require(ignoreAndroPublish || it.publishNoVariants) { "defaultBuildTemplateForBasicMppLib can not publish android stuff YET" }
  }
  repositories { addRepos(details.settings.repos) }
  defaultGroupAndVerAndDescription(details)
  extensions.configure<KotlinMultiplatformExtension> {
    allDefault(
      settings = details.settings,
      ignoreCompose = ignoreCompose,
      ignoreAndroTarget = ignoreAndroTarget,
      ignoreAndroConfig = ignoreAndroConfig,
      ignoreAndroPublish = ignoreAndroPublish,
      addCommonMainDependencies = addCommonMainDependencies,
    )
  }
  configurations.checkVerSync(warnOnly = true)
  tasks.defaultKotlinCompileOptions(jvmTargetVer = null) // jvmVer is set in fun allDefault using jvmToolchain
  tasks.defaultTestsOptions(onJvmUseJUnitPlatform = details.settings.withTestJUnit5)
  if (plugins.hasPlugin("com.vanniktech.maven.publish")) defaultPublishing(details)
  else println("MPP Module ${name}: publishing (and signing) disabled")
}

/**
 * Only for very standard small libs. In most cases it's better to not use this function.
 *
 * These ignoreXXX flags are hacky, but needed because we want to inject this code also to such build files,
 * where plugins for compose and/or android are not applied at all, so compose/android stuff should be explicitly ignored,
 * and then configured right after this call, using code from another special region (region using compose and/or andro plugin stuff).
 * Also kmp andro publishing is in the middle of big changes, so let's not support it yet, and let's wait for more clarity regarding:
 * https://youtrack.jetbrains.com/issue/KT-61575/Publishing-a-KMP-library-handles-Android-target-inconsistently-requiring-an-explicit-publishLibraryVariants-call-to-publish
 * https://youtrack.jetbrains.com/issue/KT-60623/Deprecate-publishAllLibraryVariants-in-kotlin-android
 */
fun KotlinMultiplatformExtension.allDefault(
  settings: LibSettings,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
  ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  ignoreAndroPublish: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) = with(settings) {
  require(ignoreCompose || compose == null) { "allDefault can not configure compose stuff" }
  andro?.let {
    require(ignoreAndroConfig) { "allDefault can not configure android stuff (besides just adding target)" }
    require(ignoreAndroPublish || it.publishNoVariants) { "allDefault can not publish android stuff YET" }
  }
  if (withJvm) jvm()
  if (withJs) jsDefault()
  if (withNativeLinux64) linuxX64()
  if (withAndro && !ignoreAndroTarget) androidTarget {
    // TODO_someday some kmp andro publishing. See kdoc above why not yet.
  }
  withJvmVer?.let { jvmToolchain(it.toInt()) } // works for jvm and android
  sourceSets {
    val commonMain by getting {
      dependencies {
        if (withKotlinxHtml) implementation(KotlinX.html)
        addCommonMainDependencies()
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(Kotlin.test)
        if (withTestUSpekX) implementation(Langiewicz.uspekx)
      }
    }
    if (withJvm) {
      val jvmTest by getting {
        dependencies {
          if (withTestJUnit4) implementation(JUnit.junit)
          if (withTestJUnit5) implementation(Org.JUnit.Jupiter.junit_jupiter_engine)
          if (withTestUSpekX) {
            implementation(Langiewicz.uspekx)
            if (withTestJUnit4) implementation(Langiewicz.uspekx_junit4)
            if (withTestJUnit5) implementation(Langiewicz.uspekx_junit5)
          }
          if (withTestGoogleTruth) implementation(Com.Google.Truth.truth)
          if (withTestMockitoKotlin) implementation(Org.Mockito.Kotlin.mockito_kotlin)
        }
      }
    }
    if (withNativeLinux64) {
      val linuxX64Main by getting
      val linuxX64Test by getting
    }
  }
}


fun KotlinMultiplatformExtension.jsDefault(
  withBrowser: Boolean = true,
  withNode: Boolean = false,
  testWithChrome: Boolean = true,
  testHeadless: Boolean = true,
) {
  js(IR) {
    if (withBrowser) browser {
      testTask {
        useKarma {
          when (testWithChrome to testHeadless) {
            true to true -> useChromeHeadless()
            true to false -> useChrome()
          }
        }
      }
    }
    if (withNode) nodejs()
  }
}

// endregion [[MPP Module Build Template]]

// region [[Compose MPP Module Build Template]]

/** Only for very standard compose mpp libs. In most cases, it's better to not use this function. */
@OptIn(ExperimentalComposeLibrary::class)
fun Project.defaultBuildTemplateForComposeMppLib(
  details: LibDetails = rootExtLibDetails,
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
  ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  ignoreAndroPublish: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) = with(details.settings.compose ?: error("Compose settings not set.")) {
  if (withComposeTestUiJUnit5)
    logger.warn("Compose UI Tests with JUnit5 are not supported yet! Configuring JUnit5 anyway.")
  defaultBuildTemplateForBasicMppLib(
    details = details,
    ignoreCompose = true,
    ignoreAndroTarget = ignoreAndroTarget,
    ignoreAndroConfig = ignoreAndroConfig,
    ignoreAndroPublish = ignoreAndroPublish,
    addCommonMainDependencies = addCommonMainDependencies,
  )
  extensions.configure<KotlinMultiplatformExtension> {
    allDefaultSourceSetsForCompose(details.settings)
  }
}


/**
 * Normal fun KotlinMultiplatformExtension.allDefault ignores compose stuff,
 * because it's also used for libs without compose plugin.
 * This one does the rest, so it has to be called additionally for compose libs, after .allDefault */
@OptIn(ExperimentalComposeLibrary::class)
fun KotlinMultiplatformExtension.allDefaultSourceSetsForCompose(
  settings: LibSettings,
) = with(settings.compose ?: error("Compose settings not set.")) {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        if (withComposeUi) {
          implementation(compose.ui)
        }
        if (withComposeFoundation) implementation(compose.foundation)
        if (withComposeFullAnimation) {
          implementation(compose.animation)
          implementation(compose.animationGraphics)
        }
        if (withComposeMaterial2) implementation(compose.material)
        if (withComposeMaterial3) implementation(compose.material3)
      }
    }
    if (settings.withJvm) {
      val jvmMain by getting {
        dependencies {
          if (withComposeUi) {
            implementation(compose.uiTooling)
            implementation(compose.preview)
          }
          if (withComposeMaterialIconsExtended) implementation(compose.materialIconsExtended)
          if (withComposeDesktop) {
            implementation(compose.desktop.common)
            implementation(compose.desktop.currentOs)
          }
          if (withComposeDesktopComponents) {
            implementation(compose.desktop.components.splitPane)
          }
        }
      }
      val jvmTest by getting {
        dependencies {
          @Suppress("DEPRECATION")
          if (withComposeTestUiJUnit4) implementation(compose.uiTestJUnit4)
        }
      }
    }
    if (settings.withJs) {
      val jsMain by getting {
        dependencies {
          if (withComposeHtmlCore) implementation(compose.html.core)
          if (withComposeHtmlSvg) implementation(compose.html.svg)
        }
      }
      val jsTest by getting {
        dependencies {
          if (withComposeTestHtmlUtils) implementation(compose.html.testUtils)
        }
      }
    }
  }
}

// endregion [[Compose MPP Module Build Template]]


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

fun CommonExtension<*, *, *, *, *, *>.defaultCompileOptions(
  jvmVer: String? = null, // it's better to use jvmToolchain (normally done in fun allDefault)
) = compileOptions {
  jvmVer?.let {
    sourceCompatibility(it)
    targetCompatibility(it)
  }
}

fun CommonExtension<*, *, *, *, *, *>.defaultComposeStuff() {
  buildFeatures {
    compose = true
  }
}

fun CommonExtension<*, *, *, *, *, *>.defaultPackagingOptions() = packaging {
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
        defaultPOM(lib)
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
  details: LibDetails = rootExtLibDetails,
  addAndroMainDependencies: DependencyHandler.() -> Unit = {},
) {
  val andro = details.settings.andro ?: error("No andro settings.")
  repositories { addRepos(details.settings.repos) }
  extensions.configure<KotlinMultiplatformExtension> {
    androidTarget()
    details.settings.withJvmVer?.let { jvmToolchain(it.toInt()) } // works for jvm and android
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
  if (!andro.publishNoVariants) defaultSigning()
}

fun LibraryExtension.defaultAndroLib(
  details: LibDetails = rootExtLibDetails,
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
