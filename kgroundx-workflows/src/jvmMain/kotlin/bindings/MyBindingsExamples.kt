package pl.mareklangiewicz.kgroundx.bindings

import io.github.typesafegithub.workflows.actionbindinggenerator.domain.ActionCoords
import io.github.typesafegithub.workflows.actionbindinggenerator.domain.NewestForVersion
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.kgroundx.maintenance.PProjKGround

@OptIn(DelicateApi::class)
@ExampleApi
object MyBindingsExamples {

  suspend fun tryGenerateOwnBindings() = listOf(
    ActionCoords("actions", "checkout", "v4"),
    ActionCoords("actions", "setup-java", "v4"),
    ActionCoords("EndBug", "add-and-commit", "v9"),
    ActionCoords("gradle", "actions/setup-gradle", "v4"),
    ActionCoords("gradle", "actions/dependency-submission", "v4"),
  ).forEach { coords ->
    val outputDir = PProjKGround / "kgroundx-workflows/src/jvmMain/kotlin/bindings/generated"
    coords.tryGenerateBindingsToDir(NewestForVersion, outputDir)
  }
}
