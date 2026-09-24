
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
    plugs.TemplateFun,
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

// Compose is intentionally kept (not nulled out): this app has no @Composable of its own yet,
// but keeping the compose settings is what makes defaultAndroDeps supply the compose artifacts,
// so adding one here is a one-liner rather than a build-script change.
val lib = myLib(adjustInfo = { it.copy(namespace = "pl.mareklangiewicz.templatefull.androapp") })

defaultBuildTemplateForAndroApp(lib) {
  implementation(project(":template-full-lib"))
}


// TODO_later: better defaults for versions - algo from (major, minor, path) to code;
// Very important: default synchronization between app version and Lib
// I have to have one source of truth!! But carefully select defaults propagation!
// Also use new libs properties in compose.desktop.application...
