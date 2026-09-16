// A convergence oracle for template-raw vs template-full.
//
// Both templates build green while their source-set graphs differ -- that is how ComposeJb.uiUtil
// stayed jvmMain-only in RawLibBuildTemplates.kt without anything going red. "Both are green" cannot
// tell converged from not-converged, so this dumps the thing that actually has to match: every
// source set, its dependsOn edges, and its declared dependencies.
//
// Usage (or just run ./kgroundx-maintenance/converge-diff):
//
//   ./gradlew --init-script ../kgroundx-maintenance/dump-source-sets.init.gradle.kts -q dumpSourceSets
//
// Applied as an init script so nothing diagnostic has to be committed into the templates. That also
// means the Kotlin Gradle plugin is NOT on this script's compile classpath -- an init script gets
// only Gradle's own API -- so the KMP extension is read reflectively. Reflection is the point here,
// not a shortcut: it keeps the probe working across KGP versions without pinning one.
//
// Project and module names are normalized (template-raw/template-full -> TPL) so the two dumps are
// directly diffable.

fun String.normalizeNames(): String = this
  .replace("template-raw", "TPL")
  .replace("template-full", "TPL")
  .replace("templateraw", "TPLPKG")
  .replace("templatefull", "TPLPKG")
  .replace("TemplateRaw", "TPLNAME")
  .replace("TemplateFull", "TPLNAME")

fun Any.call(method: String): Any? = javaClass.getMethod(method).invoke(this)

allprojects {
  tasks.register("dumpSourceSets") {
    notCompatibleWithConfigurationCache("diagnostic probe")
    val label = project.path.normalizeNames()
    val proj = project
    // The two templates are versioned independently by design (raw 0.0.35, full 0.0.33), so their
    // own version is not a convergence signal. Every OTHER version stays visible.
    val ownVer = project.version.toString()
    doLast {
      // Not a KMP module (jvm-cli-app, andro-app, root) -- nothing to compare here.
      val kmp = proj.extensions.findByName("kotlin") ?: return@doLast
      @Suppress("UNCHECKED_CAST")
      val sourceSets = (kmp.call("getSourceSets") as? Iterable<Any>) ?: return@doLast

      // Sorted everywhere: Gradle's iteration order is not part of what is being compared, and an
      // unsorted dump produces diff noise that hides the real deltas.
      sourceSets.sortedBy { it.call("getName") as String }.forEach { ss ->
        val name = ss.call("getName") as String
        val edges = (ss.call("getDependsOn") as Iterable<Any>)
          .map { it.call("getName") as String }.sorted()
        println("$label  $name  dependsOn=$edges")

        // Declared coordinates only, NOT a resolved graph: resolving would drag in transitives and
        // make every compose version bump look like a convergence regression.
        val deps = listOf(
          "api" to "getApiConfigurationName",
          "implementation" to "getImplementationConfigurationName",
          "compileOnly" to "getCompileOnlyConfigurationName",
          "runtimeOnly" to "getRuntimeOnlyConfigurationName",
        ).flatMap { (kind, getter) ->
          val confName = ss.call(getter) as? String ?: return@flatMap emptyList()
          val conf = proj.configurations.findByName(confName) ?: return@flatMap emptyList()
          conf.dependencies.map { d ->
            // Version included on purpose: a version skew between the templates is a real
            // divergence, and both resolve versions from the same DepsKt, so a match is expected.
            "$kind ${d.group}:${d.name}:${d.version ?: "-"}"
              .replace(ownVer, "TPLVER").normalizeNames()
          }
        }.sorted()

        if (deps.isEmpty()) println("$label  $name    (none)")
        else deps.forEach { println("$label  $name    $it") }
      }
    }
  }
}
