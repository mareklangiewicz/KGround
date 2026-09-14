
// region [[Basic MPP Lib Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatelogic.*

plugins {
  id("my-convention")
  plugAll(plugs.KotlinMulti, plugs.VannikPublishNoVer)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

defaultBuildTemplateForBasicMppLib(libTMP { it.copy(withJs = false, withLinuxX64 = false) }) {
  api(project(":kgroundx-io"))
}
