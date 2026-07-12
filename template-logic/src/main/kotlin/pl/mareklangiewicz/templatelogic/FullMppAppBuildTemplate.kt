package pl.mareklangiewicz.templatelogic

import org.gradle.api.*
import org.jetbrains.kotlin.gradle.plugin.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.defaults.*

// region [[Full MPP App Build Template]]

fun Project.defaultBuildTemplateForFullMppApp(
  details: LibDetails = rootExtLibDetails,
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
  defaultBuildTemplateForComposeMppApp(
    details = details,
    ignoreAndroConfig = true,
    addCommonMainDependencies = addCommonMainDependencies,
  )
}

// endregion [[Full MPP App Build Template]]
