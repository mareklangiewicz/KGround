package pl.mareklangiewicz.templatelogic

import org.gradle.api.*
import org.jetbrains.kotlin.gradle.plugin.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.defaults.*

// region [[Full MPP App Build Template]]

fun Project.defaultBuildTemplateForFullMppApp(
  lib: LibTMP = gradle.extLibTMP,
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
) {
  defaultBuildTemplateForComposeMppApp(
    lib = lib,
    ignoreAndroConfig = true,
    addCommonMainDependencies = addCommonMainDependencies,
  )
}
/** Nested-model compat shim: un-nest ONCE at the top, siblings below. No default for [details] (finding 7). */
fun Project.defaultBuildTemplateForFullMppApp(
  details: LibDetails,
  addCommonMainDependencies: KotlinDependencyHandler.() -> Unit = {},
): Unit = defaultBuildTemplateForFullMppApp(
  lib = details.toTMP(),
  addCommonMainDependencies = addCommonMainDependencies,
)


// endregion [[Full MPP App Build Template]]
