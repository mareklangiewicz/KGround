
// region [[Basic MPP Lib Build Imports and Plugs]]

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import com.vanniktech.maven.publish.*
import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*
import pl.mareklangiewicz.templatelogic.*

plugins {
  id("my-convention")
  plugAll(plugs.KotlinMulti, plugs.VannikPublishNoVer)
}

// endregion [[Basic MPP Lib Build Imports and Plugs]]

val settings = gradle.extLibDetails.settings.copy(
  withJs = false,
  withLinuxX64 = false,
  withKotlinxHtml = true,
)
val details = gradle.extLibDetails.copy(settings = settings)
defaultBuildTemplateForBasicMppLib(details) {
  api(project(":kgroundx-io"))
  api(project(":kgroundx-maintenance"))
  implementation(Org.Hildan.Chrome.devtools_kotlin)
  implementation(Io.Ktor.client_cio)
  implementation(Org.Slf4j.simple)
  // implementation(KotlinX.serialization_json)
}

// ===== context-parameter probes (experimental, this branch only) =============
// See template-logic/migration-status.md. No scaffolding: THIS script is compiled
// WITHOUT -Xcontext-parameters and template-logic WITH it, which is exactly the
// setup the claims are about.  ./gradlew :kgroundx-experiments:probes
val probeDetails = gradle.extLibDetails

// A flagless script cannot call a context fun naturally, but CAN reach it by
// coercing a reference to the flattened type: context params come first, then the
// extension receiver, then value params.
val probeName: (LibDetails, Project) -> String = Project::probeNameIsProjectName
val probeCtx: (LibDetails) -> String = ::probeContextFun

tasks.register("probes") {
  group = "verification"
  description = "Assert the context-parameter claims in template-logic/migration-status.md"
  val projectName = project.name
  val libName = probeDetails.name
  val details = probeDetails
  val log = logger
  doLast {
    var passed = 0
    val failed = mutableListOf<String>()
    fun check(claim: String, actual: Any?, expected: Any?) {
      if (actual == expected) { log.lifecycle("  PASS  $claim"); passed++ }
      else { log.lifecycle("  FAIL  $claim\n        expected <$expected> but got <$actual>"); failed += claim }
    }

    // Guard: if these were ever equal the next probe would prove nothing.
    check("fixture can tell project name from lib name", projectName != libName, true)

    check(
      "context(x) does not shadow Project.name the way with(x) would",
      probeName(details, project), projectName,
    )
    check(
      "context(a,b) works and context(_) forwards without naming",
      probeMultiAndUnnamed(details), "$libName/${details.settings.withJvm}",
    )
    check(
      "a flagless script reaches a context fun via the flattened coercion",
      probeCtx(details), "ctx:$libName",
    )

    log.lifecycle("\n  $passed passed, ${failed.size} failed")
    if (failed.isNotEmpty()) throw GradleException("probes failed: ${failed.joinToString()}")
  }
}
