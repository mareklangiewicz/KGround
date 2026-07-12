
// region [[Full MPP App Build Imports and Plugs]]

import com.android.build.api.dsl.*
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.buildlogic.*

plugins {
  id("my-convention")
  plugAll(
    plugs.KotlinMulti,
    plugs.KotlinMultiCompose,
    plugs.ComposeJbNoVer,
  )
}

// endregion [[Full MPP App Build Imports and Plugs]]


defaultBuildTemplateForFullMppApp(gradle.extLibDetails) {
  implementation(project(":template-raw-lib"))
}
