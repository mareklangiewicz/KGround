
// region [[Full MPP App Build Imports and Plugs]]

import com.android.build.api.dsl.*
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatelogic.*

plugins {
  plugAll(
    plugs.KotlinMulti,
    plugs.KotlinMultiCompose,
    plugs.ComposeJbNoVer,
  )
  plug(plugs.AndroAppNoVer) apply false // will be applied conditionally depending on LibSettings
}

// endregion [[Full MPP App Build Imports and Plugs]]


defaultBuildTemplateForFullMppApp {
  implementation(project(":template-full-lib"))
}
