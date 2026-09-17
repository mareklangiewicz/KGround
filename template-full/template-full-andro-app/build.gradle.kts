
// region [[Andro App Build Imports and Plugs]]

import com.android.build.api.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun")
  plugAll(
    plugs.AndroAppNoVer,
    plugs.VannikPublish,
  )
}

// endregion [[Andro App Build Imports and Plugs]]

// Compose is dropped here: this app declares no @Composable of its own, it only calls
// setMyHelloContent() from :template-full-lib, so it needs no compose compiler plugin.
val lib = myLib(adjustInfo = { it.copy(namespace = "pl.mareklangiewicz.templatefull.androapp") })
  .copy(compose = null) // this app does not use compose directly

defaultBuildTemplateForAndroApp(lib) {
  implementation(project(":template-full-lib"))
}


// TODO_later: better defaults for versions - algo from (major, minor, path) to code;
// Very important: default synchronization between app version and Lib
// I have to have one source of truth!! But carefully select defaults propagation!
// Also use new libs properties in compose.desktop.application...
