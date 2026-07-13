
// region [[Andro Lib Build Imports and Plugs]]

import com.android.build.api.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatelogic.*

plugins {
  id("my-convention")
  plugAll(
    plugs.KotlinMulti,
    plugs.KotlinMultiCompose,
    plugs.ComposeJbNoVer,
    plugs.AndroLibNoVer,
    plugs.VannikPublish,
  )
}

// endregion [[Andro Lib Build Imports and Plugs]]

defaultBuildTemplateForAndroLib()

dependencies {
  defaultAndroTestDeps(gradle.extLibDetails.settings, configuration = "androidTestImplementation")
  // TODO_someday: investigate why "androidTestImplementation" doesn't inherit from "testImplementation"
}
