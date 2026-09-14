
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

// Which Kotlin compiled each side? Read the metadata stamp off real bytecode: an
// anonymous object here is compiled by the SCRIPT compiler, LibMarker by whatever
// builds template-logic. Gemini's flattened-reference example assumed these differ
// (2.1.20 vs 2.4.20); this asserts whether they actually do.
val scriptMetadataVersion = object {}.javaClass
  .getAnnotation(Metadata::class.java)?.metadataVersion?.joinToString(".") ?: "unknown"

tasks.register("probes") {
  group = "verification"
  description = "Assert the context-parameter claims in template-logic/migration-status.md"
  val projectName = project.name
  val libName = probeDetails.name
  val details = probeDetails
  val log = logger
  val embedded = embeddedKotlinVersion
  val kmpPluginVersion = getKotlinPluginVersion()
  // Read the module compile tasks' actual args rather than assuming: defaultCompiler()
  // in template-logic adds the flag for module sources too, which is easy to miss.
  val moduleArgs = provider {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
      .flatMap { it.compilerOptions.freeCompilerArgs.get() }.distinct().sorted()
  }
  doLast {
    var passed = 0
    val failed = mutableListOf<String>()
    fun check(claim: String, actual: Any?, expected: Any?) {
      if (actual == expected) { log.lifecycle("  PASS  $claim"); passed++ }
      else { log.lifecycle("  FAIL  $claim\n        expected <$expected> but got <$actual>"); failed += claim }
    }

    // Three compilers are actually in play here, which is easy to conflate:
    log.lifecycle("  Kotlin per side:")
    log.lifecycle("    lib      template-logic sources (WITH flag) : metadata ${probeLibMetadataVersion()}, stdlib ${probeLibStdlibVersion()}")
    log.lifecycle("    consumer build.gradle.kts    (flagless)     : metadata $scriptMetadataVersion")
    log.lifecycle("    ^ both of the above are Gradle's embedded Kotlin: $embedded")
    // Careful: this reads TASK-level args. defaultCompiler() would add the flag, but it
    // is only called from the raw template, so module tasks do NOT carry it -- and yet
    // context parameters still compile in module sources, because Kotlin 2.4.x enables
    // them by default. Only Gradle's SCRIPT compiler still demands the flag.
    val moduleFlagged = moduleArgs.get().contains("-Xcontext-parameters")
    log.lifecycle("    module   kgroundx-experiments/src/**.kt      : Kotlin plugin $kmpPluginVersion, -Xcontext-parameters=$moduleFlagged (not needed there)")
    // The metadata stamp is the BINARY FORMAT version, not the compiler version -- it is
    // evidence the two sides agree, not evidence of which release built them.
    check(
      "lib and consumer share one Kotlin metadata format (no version split)",
      scriptMetadataVersion, probeLibMetadataVersion(),
    )
    check(
      "module compile tasks do NOT carry the flag (defaultCompiler is raw-template only)",
      moduleFlagged, false,
    )

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
