
// region [[Andro App Build Imports and Plugs]]

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
    plugs.AndroAppNoVer,
    plugs.VannikPublish,
  )
}

// endregion [[Andro App Build Imports and Plugs]]

var details = gradle.extLibDetails
val settings = details.settings.copy(compose = null)
details = details.copy(settings = settings, namespace = "pl.mareklangiewicz.templateraw.androapp")

defaultBuildTemplateForAndroApp(details) {
  implementation(project(":template-raw-lib"))
}


// TODO_later: better defaults for versions - algo from (major, minor, path) to code;
// Very important: default synchronization between app version and LibDetails
// I have to have one source of truth!! But carefully select defaults propagation!
// Also use new libs properties in compose.desktop.application...
