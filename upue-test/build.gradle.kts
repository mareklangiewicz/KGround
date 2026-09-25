// region [[Basic MPP Lib Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatefun.*

plugins {
  plugAll(plugs.TemplateFunNoVer, plugs.KotlinMulti, plugs.VannikPublish)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

// Old jvm / google-truth based assertion DSL, used only by :upue's jvm tests. Deliberately NOT
// published (no LibPublish): it will probably be rewritten into a nicer multiplatform DSL, and upue
// itself should stay micro, without dragging Truth into consumers.
defaultBuildTemplateForBasicMppLib(myLib { it.copy(withJs = false, withLinuxX64 = false) })

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        api(Com.Google.Truth.truth)
      }
    }
  }
}
