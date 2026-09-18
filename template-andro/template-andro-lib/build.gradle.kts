
// region [[Andro Lib Build Imports and Plugs]]

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
    plugs.KotlinMulti,
    plugs.KotlinMultiCompose,
    plugs.ComposeJbNoVer,
    plugs.AndroKmpNoVer,
    plugs.VannikPublish,
  )
}

// endregion [[Andro Lib Build Imports and Plugs]]

defaultBuildTemplateForAndroLib(publish = LibPublish())
