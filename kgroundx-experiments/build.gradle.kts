
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

// Sibling model: one flat copy, root named once. The nested form this replaces was
//   val settings = gradle.extLibDetails.settings.copy(withJs = false, withLinuxX64 = false, withKotlinxHtml = true)
//   val details = gradle.extLibDetails.copy(settings = settings)
defaultBuildTemplateForBasicMppLib(myLib { it.copy(withJs = false, withLinuxX64 = false, withKotlinxHtml = true) }) {
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

// Does the same coercion survive the shape a de-nested ENTRY POINT needs: three sibling
// context params + the Project receiver + a value param? And with a trailing lambda?
// Order is context params, then extension receiver, then value params.
val probeSiblings: (LibInfo, LibFlags, LibRepos, Project, String) -> String =
  Project::probeSiblingEntryPoint
val probeSiblingsLambda: (LibInfo, LibFlags, Project, () -> String) -> String =
  Project::probeSiblingWithLambda

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
  // The real gate is the LANGUAGE version, not the compiler release: the compiler says
  // "The feature \"context parameters\" is only available since language version 2.4".
  val moduleLangVersion = provider {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>()
      .mapNotNull { it.compilerOptions.languageVersion.orNull?.version }.distinct().sorted()
  }
  doLast {
    var passed = 0
    val failed = mutableListOf<String>()
    fun check(claim: String, actual: Any?, expected: Any?) {
      if (actual == expected) { log.lifecycle("  PASS  $claim"); passed++ }
      else { log.lifecycle("  FAIL  $claim\n        expected <$expected> but got <$actual>"); failed += claim }
    }

    // Compiler version and LANGUAGE version are different knobs. The metadata stamp
    // tracks the language version (a module class here carries mv=[2,4,0]), so 2.2.0
    // below means Gradle compiles scripts at language version 2.2 -- with a 2.4.0
    // compiler. That is why they need -Xcontext-parameters and modules do not.
    log.lifecycle("  Kotlin per side (metadata stamp ~= language version):")
    log.lifecycle("    lib      template-logic sources (WITH flag) : metadata ${probeLibMetadataVersion()}, stdlib ${probeLibStdlibVersion()}")
    log.lifecycle("    consumer build.gradle.kts    (flagless)     : metadata $scriptMetadataVersion")
    log.lifecycle("    ^ both compiled by Gradle's embedded Kotlin $embedded, but at language version ~2.2")
    // Careful: this reads TASK-level args. defaultCompiler() would add the flag, but it
    // is only called from the raw template, so module tasks do NOT carry it -- and yet
    // context parameters still compile in module sources, because Kotlin 2.4.x enables
    // them by default. Only Gradle's SCRIPT compiler still demands the flag.
    val moduleFlagged = moduleArgs.get().contains("-Xcontext-parameters")
    val langVersions = moduleLangVersion.get()
    log.lifecycle("    module   kgroundx-experiments/src/**.kt      : Kotlin plugin $kmpPluginVersion, languageVersion=${langVersions.ifEmpty { listOf("(plugin default)") }}, -Xcontext-parameters=$moduleFlagged")
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
    // Context params need EITHER languageVersion >= 2.4 OR the flag. Modules get the
    // former, build scripts neither -- hence the flag in template-logic/build.gradle.kts.
    check(
      "modules do not pin languageVersion below 2.4 (which would need the flag back)",
      langVersions.filter { it < "2.4" }, emptyList<String>(),
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
    // -Xexplicit-context-arguments is a SEPARATE LanguageFeature from ContextParameters,
    // so it needs its own opt-in. Control: without the flag this call site fails with
    // "No parameter with name 'd' found." Both flags live in template-logic/build.gradle.kts.
    check(
      "a context argument can be passed by name (-Xexplicit-context-arguments)",
      probeExplicitContextArg(details), "ctx:$libName",
    )

    // ----- did converting THIS script change anything? probe 16 --------------
    // The conversion above is only safe if the sibling form rebuilds the exact same
    // LibDetails the nested dance produced -- data-class equality over every field,
    // including compose/andro/repos. If these ever diverge, the whole module config
    // diverges with them.
    val nestedDance = gradle.extLibDetails.let {
      it.copy(settings = it.settings.copy(withJs = false, withLinuxX64 = false, withKotlinxHtml = true))
    }
    val siblingForm = myLib { it.copy(withJs = false, withLinuxX64 = false, withKotlinxHtml = true) }.toNested()
    check(
      "converting this script to the sibling form rebuilds an identical LibDetails",
      siblingForm, nestedDance,
    )
    // Guard: the fixture must be able to FAIL, or the check above proves nothing.
    check(
      "that comparison can tell two different configs apart",
      myLib { it.copy(withJs = true) }.toNested() == nestedDance, false,
    )

    // ----- the details-adjusting form too (kommand-line/-samples), probe 18 ----
    // This one guards artifactId: those scripts rename the lib, and coordinates() publishes
    // under details.name. copy() does NOT recompute namespace/appId (constructor defaults
    // run at construction only), so both forms must carry the ORIGINAL namespace forward.
    val nestedRenamed = gradle.extLibDetails.copy(name = "Kommand Line", description = "Kotlin DSL for popular CLI commands.")
    val siblingRenamed = myLib(
      adjustInfo = { it.copy(name = "Kommand Line", description = "Kotlin DSL for popular CLI commands.") },
    ).toNested()
    check(
      "the details-adjusting form rebuilds an identical LibDetails (artifactId + namespace)",
      siblingRenamed, nestedRenamed,
    )
    check(
      "and it really did keep the ORIGINAL namespace, not one recomputed from the new name",
      siblingRenamed.namespace, gradle.extLibDetails.namespace,
    )

    // ----- can a FLAGLESS build script drive the sibling model? probes 14-15 ---
    // This is the question that decides whether de-nesting can reach the PUBLIC entry
    // points, or only template-logic's internals. Scripts have no context parameters
    // (Gradle pins script language version to 2.2), but probe 7 showed a flattened
    // reference gets past that. These ask whether it still works with THREE context
    // params + receiver + value param, and with a trailing lambda.
    val libSib = details.toLib()
    check(
      "a flagless script drives a 3-sibling context fun via the flattened coercion",
      probeSiblings(libSib.info, libSib.flags, libSib.repos, project, "!"),
      "$libName/${details.settings.withJvm}/${details.settings.repos.withMavenCentral}/$projectName!",
    )
    check(
      "the coercion still works when the entry point takes a trailing lambda",
      probeSiblingsLambda(libSib.info, libSib.flags, project) { "deps" },
      "$libName/${details.settings.withJvm}/$projectName/deps",
    )

    // ----- de-nesting prototype (Lib), probes 9-13 -------------------------
    // The headline claim -- presence becomes a compile-time scope check -- is NOT here:
    // it is a compile error, so it was proven by construction instead. See probeSdkFull.
    check(
      "un-nesting the real extLibDetails loses no field (adapter is total)",
      probeAdapterFidelity(details), 0,
    )
    check(
      "changing two flags: same result, one copy instead of two, root named once not twice",
      probeCopyDance(details), "false/false/${details.settings.withJvm}|false/false/${details.settings.withJvm}|2|1",
    )
    check(
      "cross-object compose defaults survive the move into a named function",
      probeDerivedDefaults(), "8/8",
    )
    check(
      "an andro scope is entered without any !! (and is absent when there is no andro)",
      probeAndroScope(details.toLib()), details.settings.andro
        ?.let { "${it.sdkCompile}.$AndroSdkCompileMinor" } ?: "no-andro-scope",
    )
    // Found while transcribing LibAndroSettings for the prototype, NOT a prototype feature.
    // Fixed in DepsKt 0.4.26, so this flipped from witnessing the bug to guarding against it:
    // both models must now agree, and both must be right.
    check(
      "publishOneVariant over \"\"/\"*\"/\"debug\" agrees in both models and is correct",
      probePublishVariantAgreement(), "false/false/true|false/false/true",
    )

    log.lifecycle("\n  $passed passed, ${failed.size} failed")
    if (failed.isNotEmpty()) throw GradleException("probes failed: ${failed.joinToString()}")
  }
}
