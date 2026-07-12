
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
  // plugAll(plugs.KotlinMulti, plugs.VannikPublish)
  plugAll(plugs.KotlinMulti)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

defaultBuildTemplateForBasicMppLib {
  api(project(":kground"))
}
