package pl.mareklangiewicz.templatelogic

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.compose.*
import org.jetbrains.compose.desktop.*
import org.jetbrains.compose.desktop.application.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.gradle.kotlin.dsl.*
import com.android.build.api.dsl.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.defaults.*

// region [[Full MPP Lib Build Template]]

fun Project.defaultBuildTemplateForFullMppLib(
  details: LibDetails = gradle.extLibDetails,
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
): Unit = context(details, details.settings) {
  if (details.settings.withAndro) {
    // Since AGP 9 'com.android.library' cannot be combined with KMP; the android target of a
    // KMP library comes from 'com.android.kotlin.multiplatform.library' instead, exactly as
    // template-raw already does.
    apply(plugin = plugs.AndroKmpNoVer.group) // group is actually id for plugins
  }
  defaultBuildTemplateForComposeMppLib(
    details = details,
    ignoreAndroConfig = true, // andro configured below
    ignoreAndroPublish = true, // andro publishing configured below (or ignored again, but below in defaultAndroLib)
    addCommonMainDependencies = addCommonMainDependencies,
  )

  if (details.settings.withAndro) {
    extensions.configure<KotlinMultiplatformExtension> { androDefault() }

    // The KMP android target names its configurations per source set, so the plain
    // "implementation"/"testImplementation" of the old com.android.library path are gone.
    // Still reusing defaultAndroDeps rather than restating the list (trust me future Marek:
    // I've tried configuring it all the "mpp way" already :) ).
    dependencies {
      // ignoreCompose because we have compose configured mpp way already.
      defaultAndroDeps(ignoreCompose = true, configuration = "androidMainImplementation")
      defaultAndroTestDeps(ignoreCompose = true, configuration = "androidHostTestImplementation")
    }
  }
}

// endregion [[Full MPP Lib Build Template]]

// region [[MPP Module Build Template]]

/**
 * Only for very standard small libs. In most cases it's better to not use this function.
 *
 * These ignoreXXX flags are hacky, but needed. see [allDefault] kdoc for details.
 */
fun Project.defaultBuildTemplateForBasicMppLib(
  details: LibDetails = gradle.extLibDetails,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
  ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  ignoreAndroPublish: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
): Unit = context(details, details.settings) {
  require(ignoreCompose || details.settings.compose == null) { "defaultBuildTemplateForBasicMppLib can not configure compose stuff" }
  details.settings.andro?.let {
    require(ignoreAndroConfig) { "defaultBuildTemplateForBasicMppLib can not configure android stuff (besides just adding target)" }
    require(ignoreAndroPublish || it.publishNoVariants) { "defaultBuildTemplateForBasicMppLib can not publish android stuff YET" }
  }
  repositories { addRepos() }
  defaultGroupAndVerAndDescription(details)
  extensions.configure<KotlinMultiplatformExtension> {
    // Four booleans became zero: hand allDefault the platform/testing flags and nothing else.
    // Whether compose or andro get configured is decided HERE, by which scopes are opened, not
    // there by which booleans were forwarded.
    context(details.settings.toTMP()) {
      allDefault(addCommonMainDependencies = addCommonMainDependencies)
    }
  }
  configurations.checkVerSync(warnOnly = true)
  tasks.defaultKotlinCompileOptions(jvmTargetVer = null) // jvmVer is set in fun allDefault using jvmToolchain
  tasks.defaultTestsOptions(onJvmUseJUnitPlatform = details.settings.withTestJUnit5)
  if (plugins.hasPlugin("com.vanniktech.maven.publish")) defaultPublishing()
  else println("MPP Module ${name}: publishing (and signing) disabled")
}

/**
 * Only for very standard small libs. In most cases it's better to not use this function.
 *
 * MIGRATED to the sibling model ([LibSettingsTMP]). ALL FOUR `ignoreXxx` flags are gone from this
 * function, and so are the three `require`s they guarded:
 *
 * ```
 * require(ignoreCompose || compose == null) { "allDefault can not configure compose stuff" }
 * andro?.let {
 *   require(ignoreAndroConfig) { "allDefault can not configure android stuff (besides just adding target)" }
 *   require(ignoreAndroPublish || it.publishNoVariants) { "allDefault can not publish android stuff YET" }
 * }
 * ```
 *
 * The old kdoc explained the flags as needed "because we want to inject this code also to such build
 * files, where plugins for compose and/or android are not applied at all". [LibSettingsTMP] has no
 * `compose` and no `andro` to begin with, so this body cannot name either one, and the caller opts
 * out by not opening those scopes rather than by passing a boolean.
 *
 * Note what did NOT simply vanish. `ignoreAndroPublish` guarded a constraint on the CONTENT of the
 * andro settings (`publishNoVariants`), not on their presence, so it has to live where the content
 * is visible: [defaultBuildTemplateForBasicMppLib], which still holds the whole [LibDetails].
 * Nothing had to be moved there, because it was ALREADY there — all three `require`s deleted here
 * were verbatim duplicates of checks the entry point performs immediately before calling this
 * function. That duplication is itself a symptom of the nesting: both levels were handed the same
 * over-broad object, so both had to re-assert the same things about it.
 *
 * `ignoreAndroTarget` was already dead here before this migration — the `androidTarget` block it
 * guarded is commented out (see below). With this change it is dead in the whole MPP chain: the
 * entry point now only forwards it to nothing. Removing it from those signatures is a follow-up.
 *
 * kmp andro publishing is in the middle of big changes, so let's not support it yet, and let's wait
 * for more clarity regarding:
 * https://youtrack.jetbrains.com/issue/KT-61575/Publishing-a-KMP-library-handles-Android-target-inconsistently-requiring-an-explicit-publishLibraryVariants-call-to-publish
 * https://youtrack.jetbrains.com/issue/KT-60623/Deprecate-publishAllLibraryVariants-in-kotlin-android
 */
context(settings: LibSettingsTMP)
fun KotlinMultiplatformExtension.allDefault(
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) = with(settings) {
  if (withJvm) jvm()
  if (withJs) jsDefault()
  if (withLinuxX64) linuxX64()
  // if (withAndro && !ignoreAndroTarget) androidTarget {
  //   // TODO_someday some kmp andro publishing. See kdoc above why not yet.
  // }
  withJvmVer?.let { jvmToolchain(it.toInt()) } // works for jvm and android
  sourceSets {
    commonMain {
      dependencies {
        if (withKotlinxHtml) implementation(KotlinX.html)
        addCommonMainDependencies()
      }
    }
    commonTest {
      dependencies {
        implementation(Kotlin.test)
        if (withTestUSpekX) implementation(Langiewicz.uspekx)
      }
    }
    if (withJvm) {
      jvmTest {
        dependencies {
          if (withTestJUnit4) implementation(JUnit.junit)
          if (withTestJUnit5) {
            implementation(Org.JUnit.Jupiter.junit_jupiter_engine)
            runtimeOnly(Org.JUnit.Platform.junit_platform_launcher)
          }
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
    if (withLinuxX64) {
      linuxX64Main
      linuxX64Test
    }
  }
}


fun KotlinMultiplatformExtension.jsDefault(
  withBrowser: Boolean = true,
  withNode: Boolean = false,
  testWithChrome: Boolean = true,
  testHeadless: Boolean = true,
) {
  js {
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

// region [[MPP App Build Template]]

fun Project.defaultBuildTemplateForBasicMppApp(
  details: LibDetails = gradle.extLibDetails,
  ignoreCompose: Boolean = false, // so user have to explicitly say THAT he wants to ignore compose settings here.
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
  ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
  defaultBuildTemplateForBasicMppLib(
    details = details,
    ignoreCompose = ignoreCompose,
    ignoreAndroTarget = ignoreAndroTarget,
    ignoreAndroConfig = ignoreAndroConfig,
    ignoreAndroPublish = true,
    addCommonMainDependencies = addCommonMainDependencies,
  )
  extensions.configure<KotlinMultiplatformExtension> {
    if (details.settings.withJvm) jvm {
      mainRun {
        mainClass = details.run { "$appMainPackage.$appMainClass" }
        logger.info("MPP App ${project.name}: MPP plugin (without compose) just adds jvmRun task (experimental). No executable.")
      }
    }
    if (details.settings.withJs) js {
      binaries.executable()
    }
    if (details.settings.withLinuxX64) linuxX64 {
      binaries {
        executable {
          entryPoint = details.run { "$appMainPackage.$appMainFun" }
        }
      }
    }
  }
}

// endregion [[MPP App Build Template]]

// region [[Compose MPP Module Build Template]]

/** Only for very standard compose mpp libs. In most cases, it's better to not use this function. */
@OptIn(ExperimentalComposeLibrary::class)
fun Project.defaultBuildTemplateForComposeMppLib(
  details: LibDetails = gradle.extLibDetails,
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
  ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  ignoreAndroPublish: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
): Unit = context(details, details.settings) { with(details.settings.compose ?: error("Compose settings not set.")) {
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
    allDefaultSourceSetsForCompose()
  }
} }


/**
 * Normal fun KotlinMultiplatformExtension.allDefault ignores compose stuff,
 * because it's also used for libs without compose plugin.
 * This one does the rest, so it has to be called additionally for compose libs, after .allDefault */
@OptIn(ExperimentalComposeLibrary::class)
context(settings: LibSettings)
fun KotlinMultiplatformExtension.allDefaultSourceSetsForCompose(
) = with(settings.compose ?: error("Compose settings not set.")) {
  val compose = project.extensions.getByName("compose") as ComposeExtension
  sourceSets {
    commonMain {
      dependencies {
        implementation(compose.dependencies.runtime)
        if (withComposeUi) {
          implementation(compose.dependencies.ui)
        }
        if (withComposeFoundation) implementation(compose.dependencies.foundation)
        if (withComposeFullAnimation) {
          implementation(compose.dependencies.animation)
          implementation(compose.dependencies.animationGraphics)
        }
        if (withComposeMaterial2) implementation(compose.dependencies.material)
        if (withComposeMaterial3) implementation(compose.dependencies.material3)
      }
    }
    if (settings.withJvm) {
      jvmMain {
        dependencies {
          if (withComposeUi) {
            implementation(compose.dependencies.uiTooling)
            implementation(compose.dependencies.preview)
          }
          if (withComposeMaterialIconsExtended) implementation(compose.dependencies.materialIconsExtended)
          if (withComposeDesktop) {
            implementation(compose.dependencies.desktop.common)
            implementation(compose.dependencies.desktop.currentOs)
          }
          if (withComposeDesktopComponents) {
            implementation(compose.dependencies.desktop.components.splitPane)
          }
        }
      }
      jvmTest {
        dependencies {
          @Suppress("DEPRECATION")
          if (withComposeTestUiJUnit4) implementation(compose.dependencies.desktop.uiTestJUnit4)
        }
      }
    }
    if (settings.withJs) {
      jsMain {
        dependencies {
          if (withComposeHtmlCore) implementation(compose.dependencies.html.core)
          if (withComposeHtmlSvg) implementation(compose.dependencies.html.svg)
        }
      }
      jsTest {
        dependencies {
          if (withComposeTestHtmlUtils) implementation(compose.dependencies.html.testUtils)
        }
      }
    }
  }
}

// endregion [[Compose MPP Module Build Template]]

// region [[Compose MPP App Build Template]]

/** Only for very standard compose mpp apps. In most cases it's better to not use this function. */
fun Project.defaultBuildTemplateForComposeMppApp(
  details: LibDetails = gradle.extLibDetails,
  ignoreAndroTarget: Boolean = false, // so user have to explicitly say IF he wants to ignore it.
  ignoreAndroConfig: Boolean = false, // so user have to explicitly say THAT he wants to ignore it.
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
  val compose = extensions.getByName("compose") as ComposeExtension
  val desktop = (compose as ExtensionAware).extensions.getByName("desktop") as DesktopExtension
  defaultBuildTemplateForComposeMppLib(
    details = details,
    ignoreAndroTarget = ignoreAndroTarget,
    ignoreAndroConfig = ignoreAndroConfig,
    ignoreAndroPublish = true,
    addCommonMainDependencies = addCommonMainDependencies,
  )
  extensions.configure<KotlinMultiplatformExtension> {
    if (details.settings.withJs) js {
      binaries.executable()
    }
    if (details.settings.withLinuxX64) linuxX64 {
      binaries {
        executable {
          entryPoint = "${details.appMainPackage}.${details.appMainFun}"
        }
      }
    }
  }
  if (details.settings.withJvm) {
    desktop.application {
        mainClass = details.run { "$appMainPackage.$appMainClass" }
        nativeDistributions {
          targetFormats(TargetFormat.Deb)
          packageName = details.name
          packageVersion = details.version.str
          description = details.description
        }
      }
  }
}

// endregion [[Compose MPP App Build Template]]
