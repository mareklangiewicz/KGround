
// region [[Basic MPP Lib Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun")
  plugAll(plugs.KotlinMulti, plugs.VannikPublishNoVer)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

// Sibling model: one flat copy, root named once.
defaultBuildTemplateForBasicMppLib(myLib { it.copy(withJs = false, withLinuxX64 = false, withKotlinxHtml = true) }) {
  api(project(":kgroundx-io"))
  api(project(":kgroundx-maintenance"))
  implementation(Org.Hildan.Chrome.devtools_kotlin)
  implementation(Io.Ktor.client_cio)
  implementation(Org.Slf4j.simple)
  // implementation(KotlinX.serialization_json)
}
