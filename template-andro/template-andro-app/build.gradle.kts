
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
  plugAll(
    plugs.TemplateFunNoVer, // version comes from the root: a versioned request here fails in composite builds
    plugs.AndroAppNoVer,
    // The compose COMPILER plugin, even though this app declares no @Composable today.
    // App modules are meant to stay thin, but someone using this template should be able to
    // drop a quick @Composable in here before deciding to lift it into a lib module.
    plugs.KotlinMultiCompose,
    // Plumbing only: publishing is decided by publish = LibPublish(..) at the build template call
    // below, and without one this plugin publishes nothing (DepsKt 0.4.65+). Kept unconditionally so
    // this region is identical in apps that publish and apps that do not.
    plugs.VannikPublish,
  )
}

// endregion [[Andro App Build Imports and Plugs]]

// No KMP plugin here on purpose: since AGP 9 'com.android.application' cannot be combined
// with 'org.jetbrains.kotlin.multiplatform', and there is no KMP application plugin. The
// shared multiplatform code lives in :template-andro-lib, which this app depends on.
//
// Compose settings are kept (not nulled out): this app uses Jetpack Compose directly
// (MainActivity and its own theme/ package), which is the point of an android-only template,
// and keeping them is what makes defaultAndroDeps add the androidx compose artifacts.
// One flat copy of the ONE sibling that changes, with the root named once.
val lib = myLib(adjustInfo = { it.copy(namespace = "pl.mareklangiewicz.templateandro.androapp") })

defaultBuildTemplateForAndroApp(lib, publish = LibPublish(androVariant = "debug")) {
  implementation(project(":template-andro-lib"))
}


// TODO_later: better defaults for versions - algo from (major, minor, path) to code;
// Very important: default synchronization between app version and Lib
// I have to have one source of truth!! But carefully select defaults propagation!
// Also use new libs properties in compose.desktop.application...
