
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
  plugAll(plugs.KotlinMulti, plugs.VannikPublishNoVer)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

defaultBuildTemplateForBasicMppLib(myLib { it.copy(withJs = false, withLinuxX64 = false) }, publish = LibPublish(toCentral = true)) {
  api(project(":kgroundx-maintenance"))
}

kotlin {
  sourceSets {
    jvmMain {
      dependencies {
        api(Io.GitHub.TypeSafeGitHub.github_workflows_kt)

        api(Io.GitHub.TypeSafeGitHub.action_binding_generator)
        api(Io.GitHub.TypeSafeGitHub.action_updates_checker)
        api(Io.GitHub.TypeSafeGitHub.shared_internal)
        // Note: I add these for easier experimenting in consuming code/libs. Not strictly needed here.
        // All: kotlinx-workflows, kotlinx-experiments and even kotlinx-maintenance are mostly for my own experiments,
        // so users won't be usually consuming any of that (kotlinx-workflows even less often than kotlinx-maintenance)
        // so it's fine that kotlinx-workflows comes with a "price" (kinda heavy dependencies)
      }
    }
  }
}
