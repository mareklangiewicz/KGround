
// region [[KMP Lib Build Imports and Plugs]]

import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun")
  plugAll(
    plugs.KotlinMulti,
    plugs.KotlinMultiCompose,
    plugs.ComposeJbNoVer,
    plugs.VannikPublish,
  )
}

// endregion [[KMP Lib Build Imports and Plugs]]


defaultBuildTemplateForRawMppLib()
