package pl.mareklangiewicz.templatelogic

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

// region [[My Mpp Lib Persona]]

// PROTOTYPE (branch only) -- roadmap idea #2. See template-logic/migration-status.md.
//
// A "persona" is a precompiled script plugin that carries a whole module shape, so a
// consumer build script is `plugins { id("my-mpp-lib") }` plus overrides. This class is
// the override surface: the consumer fills it in, and the plugin applies it afterwards.
//
// Why an object to collect overrides at all, rather than parameters: a `plugins {}` block
// cannot pass arguments, so everything a consumer wants to say has to arrive through an
// extension configured AFTER the plugin is applied -- hence deferred configuration.
open class MyMppLibPersona {

  /** Overrides [LibDetails.name], e.g. "Kommand Line". Null keeps the settings.gradle.kts value. */
  var name: String? = null

  /** Overrides [LibDetails.description]. Null keeps the settings.gradle.kts value. */
  var description: String? = null

  internal var tweakSettings: LibSettings.() -> LibSettings = { this }

  internal var addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {}

  /** Narrow the module's settings, e.g. `settings { copy(withJs = false) }`. */
  fun settings(tweak: LibSettings.() -> LibSettings) { tweakSettings = tweak }

  /** Same content as the trailing lambda of [defaultBuildTemplateForBasicMppLib]. */
  fun commonMainDependencies(deps: KotlinDependencyHandler.() -> Unit) { addCommonMainDependencies = deps }

  internal var addJvmMainDependencies: KotlinDependencyHandler.() -> Unit = {}

  /**
   * Dependencies for the jvmMain source set.
   *
   * This exists because a consumer CANNOT write its own `kotlin { sourceSets { jvmMain { } } }`
   * block alongside a persona: that block runs during evaluation, before the deferred template
   * call has created the jvm() target, and Kotlin then fails with "The compilation 'main' cannot
   * be created after the source set 'jvmMain'". So every source set a consumer wants to touch
   * has to be reachable through this extension.
   */
  fun jvmMainDependencies(deps: KotlinDependencyHandler.() -> Unit) { addJvmMainDependencies = deps }

  /** Everything the consumer said, folded onto the project-wide details. */
  internal fun detailsFrom(project: Project): LibDetails {
    val base = project.gradle.extLibDetails
    return base.copy(
      name = name ?: base.name,
      description = description ?: base.description,
      settings = base.settings.tweakSettings(),
    )
  }
}

// endregion [[My Mpp Lib Persona]]
