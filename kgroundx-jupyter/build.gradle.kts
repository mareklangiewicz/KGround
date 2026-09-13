
// region [Custom MPP Lib Build Imports and Plugs]

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
    plugs.KotlinMulti,
    plugs.VannikPublishNoVer,
    // plugs.KotlinJupyter,
  )
}

// endregion [Custom MPP Lib Build Imports and Plugs]

// FIXME: had to disable plugs.KotlinJupyter and it's configuration,
//   because looks like it makes plugs.VannikPublish fail.
//   Try again later.. or maybe first wait until jupyter actually supports kotlin 2.1!

val settings = gradle.extLibDetails.settings.copy(
  withJs = false,
  withLinuxX64 = false,
)

val details = gradle.extLibDetails.copy(settings = settings)

// Note: I tried to use Jvm only templates for kground-jupyter module, but it's way worse approach.
// I'd have to use java plugin for source jar generation (and had problems with that; sources are required by sonatype),
// also it's better to rely on modern kotlin mpp plugin (even if only jvm target is enabled),
// to generate all needed gradle metadata so it's all compatible when other mpp projects depend on this module.
defaultBuildTemplateForBasicMppLib(details) {
  api(project(":kgroundx-maintenance"))
}

// tasks.processJupyterApiResources {
//   libraryProducers = listOf("pl.mareklangiewicz.kgroundx.jupyter.Integration")
// }
