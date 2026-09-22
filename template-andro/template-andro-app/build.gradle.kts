
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
    // The compose COMPILER plugin, even though this app declares no @Composable today.
    // App modules are meant to stay thin, but someone using this template should be able to
    // drop a quick @Composable in here before deciding to lift it into a lib module.
    plugs.KotlinMultiCompose,
    // plugs.VannikPublish is deliberately ABSENT. This app publishes nothing: template-full sets no
    // android publishVariant, so no component was ever registered and the plugin produced exactly
    // zero publication tasks -- measured, before the DepsKt 0.4.63 migration. Since 0.4.63 that
    // combination (publish plugin applied, no LibPublish passed) is a configuration ERROR rather
    // than a silent no-op, and the honest fix is to drop the plugin, not to invent an opt-in.
    // :template-full-app next door already had no publish plugin, so the two app modules now agree.
    // To publish this app, add the plugin back AND pass
    // publish = LibPublish(androVariant = "release") -- see :template-andro-app for the shape.
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
