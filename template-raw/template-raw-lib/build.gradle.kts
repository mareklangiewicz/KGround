
// region [[Raw MPP Lib Build Imports and Plugs]]

import com.vanniktech.maven.publish.MavenPublishBaseExtension
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
    plugs.AndroKmpNoVer,
    plugs.VannikPublish,
  )
}

// endregion [[Raw MPP Lib Build Imports and Plugs]]

// TODO_someday_maybe:
//  - move my logic to "convention plugin" and use "context parameters"
//    - instead of file-global details, settings, settpose
//      - BTW this plan avoids ever adding explicit parameters (that's why I use file-globals for now)
//      - BTW I also avoid adding flags like ignoreCompose, ignoreAndroTarget, etc (from old templates)
//        - just copy/modify these globals before executing defaultBuildTemplate...

val details = rootExtLibDetails
val settings = details.settings
val settpose = settings.compose ?: error("Compose settings not set.")

defaultBuildTemplateForRawMppLib()
// BTW I also removed addCommonMainDependencies lambda parameter, just add deps normally if needed.
// (it's almost always needed to add sth not just to commonMain, so default explicit dsl is better)

kotlin {
  sourceSets {
    androidMain {
      dependencies {
        implementation(AndroidX.Core.ktx)
        implementation(AndroidX.Activity.activity)
        implementation(AndroidX.Activity.ktx)
        implementation(AndroidX.Activity.compose)
      }
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

fun KotlinMultiplatformExtension.defaultCompiler(
  kotlinVer: KotlinVersion = KotlinVersion.KOTLIN_2_3,
  jvmVer: Int? = null,
  renderInternalDiagnosticNames: Boolean = false,
) {
  compilerOptions {
    languageVersion.set(kotlinVer)
    apiVersion.set(kotlinVer)
    if (renderInternalDiagnosticNames) freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
    // useful, for example, to suppress some errors when accessing internal code from some library, like:
    // @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXPOSED_PARAMETER_TYPE", "EXPOSED_PROPERTY_TYPE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
  }
  jvmVer?.let(::jvmToolchain)
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
  // println("TC dTO task $name:${this::class.qualifiedName}")
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
  if (lib.settings.withCentralPublish) publishToMavenCentral(automaticRelease = false)
  signAllPublications()
  signAllPublicationsFixSignatoryIfFound()
  // Note: artifactId is not lib.name but current project.name (module name)
  coordinates(groupId = lib.group, artifactId = name, version = lib.version.str)
  pom { defaultPOM(lib) }
}

@OptIn(ExperimentalComposeLibrary::class)
fun Project.defaultBuildTemplateForRawMppLib() {

  if (settpose.withComposeTestUiJUnit5)
    logger.warn("Compose UI Tests with JUnit5 are not supported yet! Configuring JUnit5 anyway.")

  repositories { addRepos(settings.repos) }
  defaultGroupAndVerAndDescription(details)

  kotlin {
    defaultCompiler(jvmVer = settings.withJvmVer?.toInt())

    if (settings.withJvm) jvm()
    if (settings.withJs) jsDefault()
    if (settings.withLinuxX64) linuxX64()
    if (settings.withAndro) androidLibrary {
      val andro = settings.andro!!
      minSdk { version = release(andro.sdkMin) }
      compileSdk {
        version = andro.sdkCompilePreview?.let { preview(it) } ?: release(andro.sdkCompile)
      }
      namespace = details.namespace
      withHostTest {
        // isIncludeAndroidResources = true
        // isReturnDefaultValues = true
      }
      withDeviceTest {
        instrumentationRunner = andro.withTestRunner
      }
    }

    sourceSets {
      commonMain {
        dependencies {
          if (settings.withKotlinxHtml) implementation(KotlinX.html)
          implementation(compose.runtime)
          if (settpose.withComposeUi) {
            implementation(compose.ui)
            implementation(compose.components.resources)
          }
          if (settpose.withComposeFoundation) implementation(compose.foundation)
          if (settpose.withComposeFullAnimation) {
            implementation(compose.animation)
            implementation(compose.animationGraphics)
          }
          if (settpose.withComposeMaterial2) implementation(compose.material)
          if (settpose.withComposeMaterial3) implementation(compose.material3)
        }
      }
      commonTest {
        dependencies {
          implementation(Kotlin.test)
          if (settings.withTestUSpekX) implementation(Langiewicz.uspekx)
        }
      }
      if (settings.withJvm) {
        jvmMain {
          dependencies {
            if (settpose.withComposeUi) {
              implementation(compose.uiTooling)
              implementation(compose.uiUtil)
              implementation(compose.preview)
            }
            if (settpose.withComposeMaterialIconsExtended) implementation(compose.materialIconsExtended)
            if (settpose.withComposeDesktop) {
              implementation(compose.desktop.common)
              implementation(compose.desktop.currentOs)
            }
            if (settpose.withComposeDesktopComponents) {
              implementation(compose.desktop.components.splitPane)
            }
          }
        }
        jvmTest {
          kotlin.srcDir("src/commonUiTest/kotlin")
          dependencies {
            if (settings.withTestJUnit4) implementation(JUnit.junit)
            if (settpose.withComposeTestUi) implementation(compose.uiTest)
            if (settpose.withComposeTestUiJUnit4) implementation(compose.desktop.uiTestJUnit4)
            if (settings.withTestJUnit5) {
              implementation(Org.JUnit.Jupiter.junit_jupiter_engine)
              runtimeOnly(Org.JUnit.Platform.junit_platform_launcher)
            }
            if (settings.withTestUSpekX) {
              if (settings.withTestJUnit4) implementation(Langiewicz.uspekx_junit4)
              if (settings.withTestJUnit5) implementation(Langiewicz.uspekx_junit5)
            }
            if (settings.withTestGoogleTruth) implementation(Com.Google.Truth.truth)
            if (settings.withTestMockitoKotlin) implementation(Org.Mockito.Kotlin.mockito_kotlin)
          }
        }
      }
      if (settings.withJs) {
        jsMain {
          dependencies {
            if (settpose.withComposeHtmlCore) implementation(compose.html.core)
            if (settpose.withComposeHtmlSvg) implementation(compose.html.svg)
          }
        }
        jsTest {
          dependencies {
            if (settpose.withComposeTestHtmlUtils) implementation(compose.html.testUtils)
          }
        }
      }
      if (settings.withLinuxX64) {
        linuxX64Main
        linuxX64Test
      }
      if (settings.withAndro) {
        androidMain {
          // TODO_maybe: some minimal default deps??

          // FIXME: Is this needed?, maybe I should use jetbrains based artifacts here too?
          // well, maybe preview is needed? will it work with the rest from jetbrains??
          dependencies {
            if (settpose.withComposeUi) {
              implementation(AndroidX.Compose.Ui.ui)
              implementation(AndroidX.Compose.Ui.util)
              implementation(AndroidX.Compose.Ui.tooling)
              implementation(AndroidX.Compose.Ui.tooling_preview)
            }
          }
        }
        val androidHostTest by getting {
          dependencies {
            if (settings.withTestJUnit4) implementation(JUnit.junit)
            if (settings.withTestJUnit5) {
              implementation(Org.JUnit.Jupiter.junit_jupiter_engine)
              runtimeOnly(Org.JUnit.Platform.junit_platform_launcher)
            }
            if (settings.withTestUSpekX) {
              if (settings.withTestJUnit4) implementation(Langiewicz.uspekx_junit4)
              if (settings.withTestJUnit5) implementation(Langiewicz.uspekx_junit5)
            }
          }
        }
        // NOTE: The `val androidDeviceTest by getting {...} works differently than
        // `androidInstrumentedTest {...}` and it is needed for on device tests.
        val androidDeviceTest by getting {
          kotlin.srcDir("src/commonUiTest/kotlin")
          dependencies {
            implementation(Kotlin.test) // by default device tests don't get common tests sourceSet (unlike host tests)
            if (settings.withTestJUnit4OnAndroidDevice) {
              implementation(JUnit.junit)
              implementation(AndroidX.Test.core)
              implementation(AndroidX.Test.core_ktx)
              implementation(AndroidX.Test.runner)
              implementation(AndroidX.Test.Ext.junit)
              implementation(AndroidX.Test.Ext.junit_ktx)
            }
            else if (settings.withTestJUnit5) {
              error("JUnit5 is NOT yet supported on android device tests.")
              // implementation(Org.JUnit.Jupiter.junit_jupiter_engine)
              // runtimeOnly(Org.JUnit.Platform.junit_platform_launcher)
            }
            if (settings.withTestUSpekX) {
              if (settings.withTestJUnit4OnAndroidDevice) implementation(Langiewicz.uspekx_junit4)
              // else if (settings.withTestJUnit5) implementation(Langiewicz.uspekx_junit5)
            }
            // FIXME: based on code added by Gemini, but maybe jetbrains based artifacts would work too?/better?
            if (settpose.withComposeTestUi) implementation(AndroidX.Compose.Ui.test)
            if (settpose.withComposeTestUiJUnit4) implementation(AndroidX.Compose.Ui.test_junit4)
          }
        }
      }
    }
  }
  configurations.checkVerSync(warnOnly = true)
  tasks.defaultTestsOptions(onJvmUseJUnitPlatform = settings.withTestJUnit5)
  if (plugins.hasPlugin("com.vanniktech.maven.publish")) defaultPublishing(details)
  else println("MPP Module ${name}: publishing (and signing) disabled")
}
// tasks.matching { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }
//   .configureEach { enabled = false }

compose.resources {
  // generateResClass = always
  generateResClass = never

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

// TODO: NOW continue based on /home/marek/code/kotlin/KGround/template-full/template-full-lib/build.gradle.kts
