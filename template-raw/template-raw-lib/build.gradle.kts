
// region [[KMP Lib Build Imports and Plugs]]

import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import pl.mareklangiewicz.buildlogic.*

plugins {
  id("my-convention")
  plugAll(
    plugs.KotlinMulti,
    plugs.KotlinMultiCompose,
    plugs.ComposeJbNoVer,
    plugs.VannikPublish,
  )
}

// endregion [[KMP Lib Build Imports and Plugs]]

// TODO_someday_maybe:
//  - move my logic to "convention plugin" and use "context parameters"
//    - instead of file-global details, settings, settpose
//      - BTW this plan avoids ever adding explicit parameters (that's why I use file-globals for now)
//      - BTW I also avoid adding flags like ignoreCompose, ignoreAndroTarget, etc (from old templates)
//        - just copy/modify these globals before executing defaultBuildTemplate...

val details = gradle.extLibDetails
val settings = details.settings

defaultBuildTemplateForRawMppLib()
// BTW I also removed addCommonMainDependencies lambda parameter, just add deps normally if needed.
// (it's almost always needed to add sth not just to commonMain, so default explicit dsl is better)

kotlin {
  sourceSets {
    if (settings.withAndro) androidMain {
      dependencies {
        implementation(AndroidX.Core.ktx)
        implementation(AndroidX.Activity.activity)
        implementation(AndroidX.Activity.ktx)
        implementation(AndroidX.Activity.compose)
      }
    }
  }
}

tasks.matching { it.name == "copyAndroidDeviceTestComposeResourcesToAndroidAssets" }
  .configureEach { enabled = false }

compose.resources {
  // generateResClass = always
  generateResClass = never

}

// TODO: NOW continue based on /home/marek/code/kotlin/KGround/template-full/template-full-lib/build.gradle.kts
