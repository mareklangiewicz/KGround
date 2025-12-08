
// region [[KMP Root Build Template]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.utils.*

plugins {
  plug(plugs.KotlinMulti) apply false
  plug(plugs.KotlinJvm) apply false
  plug(plugs.KotlinMultiCompose) apply false

  plug(plugs.ComposeJbStable) apply false // ComposeJbEdge can be very slow to sync, clean, build (jb dev repo issue)
  // id("org.jetbrains.compose") version "1.10.0-beta02" apply false
  // TODO_later: Check again after compose update, because now default version fails with:
  // Cannot determine the version of Skiko for Compose '1.10.0-rc01'

  // Temporary fix for sync error:
  // The project is using an incompatible version (AGP 9.0.0-beta04) of the Android Gradle plugin.
  // Latest supported version is AGP 9.0.0-beta03
  // plug(plugs.AndroKmp) apply false
  // plug(plugs.AndroApp) apply false
  id("com.android.kotlin.multiplatform.library") version "9.0.0-beta03" apply false
  id("com.android.application") version "9.0.0-beta03" apply false

  plug(plugs.VannikPublish) apply false
}

val details = gradle.extLibDetails

defaultGroupAndVerAndDescription(details)

// endregion [[KMP Root Build Template]]
