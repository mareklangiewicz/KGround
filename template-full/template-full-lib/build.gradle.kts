
// region [[Full MPP Lib Build Imports and Plugs]]

import com.android.build.api.dsl.*
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.compose.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun")
  plugAll(
    plugs.KotlinMulti,
    plugs.KotlinMultiCompose,
    plugs.ComposeJbNoVer,
    plugs.VannikPublish,
  )
  plug(plugs.AndroKmpNoVer) apply false // applied conditionally by defaultBuildTemplateForFullMppLib
}

// endregion [[Full MPP Lib Build Imports and Plugs]]


defaultBuildTemplateForFullMppLib()

// This lib has full Compose UI (not just compose-html) on js, so Skiko has to be bundled by webpack
// or `checkComposeUiTestConfigurationForJs` fails. See https://youtrack.jetbrains.com/issue/CMP-4906
// template-raw-lib avoids this by keeping js on compose-html only (its `composeUiTest` source set is
// not in jsTest's dependsOn chain). The durable fix belongs in DepsKt's
// `defaultBuildTemplateForFullMppLib`, which -- unlike the App templates -- never declares js binaries.
kotlin { js { binaries.executable() } }
