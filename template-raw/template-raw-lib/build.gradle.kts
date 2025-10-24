
// region [[Raw MPP Lib Build Imports and Plugs]]

import com.android.build.api.dsl.*
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
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
    // plugs.AndroKmpNoVer,
    plugs.VannikPublish,
  )
}

// endregion [[Raw MPP Lib Build Imports and Plugs]]

// TODO: ADD LATER:
//  - enable js flag (main build.gradle.kts)
//  - enable linuxX64 flag (same)
//  - enable android and add configuration code (apply plugin conditionally?)
//  - maybe compose could also be applied and configured conditionally here? (only if not too complex)
//  - Tests
//    - compose Tests (check JUnit5 current support? JUnit6??)
//    - android Tests (both kinds)
//

val details = rootExtLibDetails
val settings = details.settings
val settpose = settings.compose ?: error("Compose settings not set.")

defaultBuildTemplateForRawMppLib()


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
  apiVer: KotlinVersion = KotlinVersion.KOTLIN_2_3,
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
    // inlined fun allDefault:
    if (settings.withJvm) jvm()
    if (settings.withJs) jsDefault()
    if (settings.withNativeLinux64) linuxX64()
    settings.withJvmVer?.let { jvmToolchain(it.toInt()) } // works for jvm and android
    sourceSets {
      commonMain {
        dependencies {
          if (settings.withKotlinxHtml) implementation(KotlinX.html)
          implementation(compose.runtime)
          if (settpose.withComposeUi) {
            implementation(compose.ui)
          }
          if (settpose.withComposeFoundation) implementation(compose.foundation)
          if (settpose.withComposeFullAnimation) {
            implementation(compose.animation)
            implementation(compose.animationGraphics)
          }
          if (settpose.withComposeMaterial2) implementation(compose.material)
          if (settpose.withComposeMaterial3) implementation(compose.material3)
          // addCommonMainDependencies()
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
              implementation(compose.preview)
            }
            if (settpose.withComposeMaterialIconsExtended) implementation(compose.materialIconsExtended)
            if (settpose.withComposeDesktop) {
              implementation(compose.desktop.common)
              implementation(compose.desktop.currentOs)
            }
            if (settpose.withComposeDesktopComponents) {
              @OptIn(ExperimentalComposeLibrary::class)
              implementation(compose.desktop.components.splitPane)
            }
          }
        }
        jvmTest {
          dependencies {
            if (settings.withTestJUnit4) implementation(JUnit.junit)
            @OptIn(ExperimentalComposeLibrary::class)
            if (settpose.withComposeTestUiJUnit4) implementation(compose.uiTestJUnit4)
            if (settings.withTestJUnit5) {
              implementation(Org.JUnit.Jupiter.junit_jupiter_engine)
              runtimeOnly(Org.JUnit.Platform.junit_platform_launcher)
            }
            if (settings.withTestUSpekX) {
              implementation(Langiewicz.uspekx)
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
      if (settings.withNativeLinux64) {
        linuxX64Main
        linuxX64Test
      }
    }
  }
  configurations.checkVerSync(warnOnly = true)
  tasks.defaultKotlinCompileOptions(jvmTargetVer = null) // jvmVer is set in fun allDefault using jvmToolchain
  tasks.defaultTestsOptions(onJvmUseJUnitPlatform = settings.withTestJUnit5)
  if (plugins.hasPlugin("com.vanniktech.maven.publish")) defaultPublishing(details)
  else println("MPP Module ${name}: publishing (and signing) disabled")
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

