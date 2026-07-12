
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

val settings = rootExtLibDetails.settings.copy(
  withJs = false,
  withLinuxX64 = false,
  withKotlinxHtml = true,
)
val details = rootExtLibDetails.copy(settings = settings)
defaultBuildTemplateForBasicMppLib(details) {
  api(project(":kgroundx-io"))
  api(project(":kgroundx-maintenance"))
  implementation(Org.Hildan.Chrome.devtools_kotlin)
  implementation(Io.Ktor.client_cio)
  implementation(Org.Slf4j.simple)
  // implementation(KotlinX.serialization_json)
}
