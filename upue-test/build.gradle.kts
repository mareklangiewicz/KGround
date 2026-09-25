
// region [[Basic MPP Lib Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  id("pl.mareklangiewicz.templatefun")
  plugAll(plugs.KotlinMulti, plugs.VannikPublish)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

// TODO_later: probably deprecate or rewrite into nicer multiplatform dsl?
// upue-test is old jvm / google-truth based assertion DSL used only in upue
// I'll probably deprecate it, let's stop publishing it - upue should be micro.
//
// "Published, but not to Central" is a FIRST-CLASS state since DepsKt 0.4.63: LibPublish() with
// toCentral left false. This module is the case that argued for it -- saying it used to need a
// two-level copy dance through the repo-wide Lib
// (`gradle.extLib.run { copy(flags = flags.copy(withCentralPublish = false)) }`, and before the
// de-nesting, `rootExtLibDetails.copy(settings = settings.copy(..))`), purely to turn one flag OFF
// that this module never wanted on. Now it just says what it is.
defaultBuildTemplateForBasicMppLib(publish = LibPublish())

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        api(Com.Google.Truth.truth)
      }
    }
  }
}
