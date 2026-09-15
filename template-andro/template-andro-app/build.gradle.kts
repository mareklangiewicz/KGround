
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
    // This app uses Jetpack Compose directly (not compose-multiplatform), so it needs the
    // compose COMPILER plugin. template-raw-andro-app does not: its UI lives in the shared lib.
    plugs.KotlinMultiCompose,
    plugs.VannikPublish,
  )
}

// endregion [[Andro App Build Imports and Plugs]]

// No KMP plugin here on purpose: since AGP 9 'com.android.application' cannot be combined
// with 'org.jetbrains.kotlin.multiplatform', and there is no KMP application plugin. The
// shared multiplatform code lives in :template-andro-lib, which this app depends on.
//
// Unlike template-raw-andro-app, compose is NOT set to null here: this app uses Jetpack
// Compose directly (MainActivity and its own theme/ package), which is the point of an
// android-only template. Keeping the compose settings is what makes defaultAndroDeps add
// the androidx compose artifacts.
// One flat copy of the ONE sibling that changes, with the root named once.
val lib = myLib(adjustInfo = { it.copy(namespace = "pl.mareklangiewicz.templateandro.androapp") })

defaultBuildTemplateForAndroApp(lib) {
  implementation(project(":template-andro-lib"))
}


// TODO_later: better defaults for versions - algo from (major, minor, path) to code;
// Very important: default synchronization between app version and LibDetails
// I have to have one source of truth!! But carefully select defaults propagation!
// Also use new libs properties in compose.desktop.application...
