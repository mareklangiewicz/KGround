package pl.mareklangiewicz.templatelogic

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.gradle.kotlin.dsl.*
import com.android.build.api.dsl.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.defaults.*

// region [[Raw Lib Build Template]]

@OptIn(ExperimentalComposeLibrary::class)
fun Project.defaultBuildTemplateForRawMppLib() {
  val details = gradle.extLibDetails
  val settings = details.settings

  if (settings.withAndro) {
    apply(plugin = plugs.AndroKmpNoVer.group) // group is actually id for plugins
  }
  if (settings.compose?.withComposeTestUiJUnit5 == true)
    logger.warn("Compose UI Tests with JUnit5 are not supported yet! Configuring JUnit5 anyway.")

  repositories { addRepos(settings.repos) }
  defaultGroupAndVerAndDescription(details)

  val compose = extensions.getByName("compose") as ComposeExtension

  extensions.configure<KotlinMultiplatformExtension> {
    defaultCompiler(jvmVer = settings.withJvmVer?.toInt())

    if (settings.withJvm) jvm()
    if (settings.withLinuxX64) linuxX64()
    if (settings.withJs) jsDefault()
    if (settings.withAndro) androDefault()

    applyDefaultHierarchyTemplate()

    sourceSets {
      commonMain {
        dependencies {
          if (settings.withKotlinxHtml) implementation(KotlinX.html)
        }
      }
      commonTest {
        dependencies {
          implementation(Kotlin.test)
          if (settings.withTestUSpekX) implementation(Langiewicz.uspekx)
        }
      }

      val composeMain = create("composeMain") {
        dependsOn(commonMain.get())
        dependencies {
          if (settings.withCompose) implementation(compose.dependencies.runtime)
        }
      }

      val composeTest = create("composeTest") {
        // dependsOn(composeMain) check if it's not needed and it even generates warnings!
        // TODO_later: understand root cause - check kotlin mpp warnings and where it's generated in sources.
        dependsOn(commonTest.get())
        dependencies {
          val settpose = settings.compose ?: return@dependencies
          // TODO_later anything here? any compose testing util not related to compose ui?
        }
      }

      val composeUiMain = create("composeUiMain") {
        dependsOn(composeMain)
        dependencies {
          val settpose = settings.compose ?: return@dependencies
          if (settpose.withComposeUi) {
            implementation(compose.dependencies.ui)
            implementation(compose.dependencies.components.resources)
          }
          if (settpose.withComposeFoundation) implementation(compose.dependencies.foundation)
          if (settpose.withComposeFullAnimation) {
            implementation(compose.dependencies.animation)
            implementation(compose.dependencies.animationGraphics)
          }
          if (settpose.withComposeMaterial2) implementation(compose.dependencies.material)
          if (settpose.withComposeMaterial3) implementation(compose.dependencies.material3)
        }
      }

      val composeUiTest = create("composeUiTest") {
        // dependsOn(composeUiMain) looks like not it's not needed and it even generates warnings!
          // TODO_later: understand root cause - check kotlin mpp warnings and where it's generated in sources.
        dependsOn(composeTest)
        dependencies {
          val settpose = settings.compose ?: return@dependencies
          if (settpose.withComposeTestUi) implementation(compose.dependencies.uiTest)
        }
      }

      if (settings.withJvm) {
        jvmMain {
          dependsOn(composeUiMain)
          dependencies {
            val settpose = settings.compose ?: return@dependencies
            if (settpose.withComposeUi) {
              implementation(compose.dependencies.uiTooling)
              implementation(compose.dependencies.uiUtil)
              implementation(compose.dependencies.preview)
            }
            if (settpose.withComposeMaterialIconsExtended) implementation(compose.dependencies.materialIconsExtended)
            if (settpose.withComposeDesktop) {
              implementation(compose.dependencies.desktop.common)
              implementation(compose.dependencies.desktop.currentOs)
            }
            if (settpose.withComposeDesktopComponents) {
              implementation(compose.dependencies.desktop.components.splitPane)
            }
          }
        }
        jvmTest {
          dependsOn(composeUiTest)
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
            if (settings.withTestGoogleTruth) implementation(Com.Google.Truth.truth)
            if (settings.withTestMockitoKotlin) implementation(Org.Mockito.Kotlin.mockito_kotlin)

            val settpose = settings.compose ?: return@dependencies
            if (settpose.withComposeTestUiJUnit4) implementation(compose.dependencies.desktop.uiTestJUnit4)
          }
        }
      }
      if (settings.withJs) {
        jsMain {
          dependsOn(composeMain)
          dependencies {
            val settpose = settings.compose ?: return@dependencies
            if (settpose.withComposeHtmlCore) implementation(compose.dependencies.html.core)
            if (settpose.withComposeHtmlSvg) implementation(compose.dependencies.html.svg)
          }
        }
        jsTest {
          dependsOn(composeTest)
          dependencies {
            val settpose = settings.compose ?: return@dependencies
            if (settpose.withComposeTestHtmlUtils) implementation(compose.dependencies.html.testUtils)
          }
        }
      }
      if (settings.withLinuxX64) {
        linuxX64Main
        linuxX64Test
      }
      if (settings.withAndro) {
        androidMain {
          dependsOn(composeUiMain)
          dependencies {
            val settpose = settings.compose ?: return@dependencies
            if (settpose.withComposeUi) {
              implementation(AndroidX.Compose.Ui.ui)
              implementation(AndroidX.Compose.Ui.util)
              implementation(AndroidX.Compose.Ui.tooling)
              implementation(AndroidX.Compose.Ui.tooling_preview)
            }
          }
        }
        getByName("androidHostTest") {
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
        getByName("androidDeviceTest") {
          dependencies {
            implementation(Kotlin.test)
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
            }
            if (settings.withTestUSpekX) {
              if (settings.withTestJUnit4OnAndroidDevice) implementation(Langiewicz.uspekx_junit4)
            }
            val settpose = settings.compose ?: return@dependencies
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

fun KotlinMultiplatformExtension.androDefault() {
  val details = project.gradle.extLibDetails
  val settings = details.settings
  extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
    val andro = settings.andro!!
    minSdk { version = release(andro.sdkMin) }
    compileSdk {
      version = andro.sdkCompilePreview?.let { preview(it) } ?: release(andro.sdkCompile)
    }
    namespace = details.namespace
    withHostTest {
    }
    withDeviceTest {
      instrumentationRunner = andro.withTestRunner
    }
  }
}

// endregion [[Raw Lib Build Template]]
