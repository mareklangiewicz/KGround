package pl.mareklangiewicz.kgroundx.bindings

import io.github.typesafegithub.workflows.actionbindinggenerator.domain.ActionCoords
import io.github.typesafegithub.workflows.actionbindinggenerator.domain.NewestForVersion
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.kgroundx.maintenance.PProjKGround
import pl.mareklangiewicz.udata.LO

@OptIn(DelicateApi::class)
@ExampleApi
object MyBindingsExamples {
  suspend fun tryGenerateOwnBindings() = LO(
    ActionCoords("actions", "checkout", "v7"), // https://github.com/actions/checkout
    ActionCoords("actions", "setup-java", "v5"), // https://github.com/actions/setup-java
    ActionCoords("EndBug", "add-and-commit", "v10"), // https://github.com/EndBug/add-and-commit
    ActionCoords("gradle", "actions/setup-gradle", "v6"), // https://github.com/gradle/actions
    ActionCoords("gradle", "actions/dependency-submission", "v6"),
    ActionCoords("gradle", "actions/wrapper-validation", "v6"), // usually not needed - setup-gradle does it too
    ActionCoords("actions", "upload-artifact", "v7"), // https://github.com/actions/upload-artifact
    ActionCoords("actions", "download-artifact", "v8"), // https://github.com/actions/download-artifact
    ActionCoords("softprops", "action-gh-release", "v3"), // https://github.com/softprops/action-gh-release
  ).forEach { coords ->
    val outputDir = PProjKGround / "kgroundx-workflows/src/jvmMain/kotlin/bindings/generated"
    coords.tryGenerateBindingsToDir(NewestForVersion, outputDir)
  }
}
