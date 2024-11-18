@file:Suppress("unused")
@file:OptIn(DelicateApi::class)

package pl.mareklangiewicz.kgroundx.workflows

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kgroundx.maintenance.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.udata.tta

@ExampleApi
object MyWorkflowsExamples {
  suspend fun checkAllMDW() = checkMyDWorkflowsInMyProjects(onlyPublic = false)

  suspend fun injectMDWToMyProjects() = injectMyDWorkflowsToMyProjects(onlyPublic = false)

  suspend fun injectDWToExampleProject() = injectDWorkflowsToProject(PCodeKt / "AbcdK")

  suspend fun injectGenerateDepsWToRefreshDepsRepo() = injectHackyGenerateDepsWorkflowToRefreshDepsRepo()

  suspend fun injectUpdateGeneratedDepsToDepsKtRepo() = injectUpdateGeneratedDepsWorkflowToDepsKtRepo()

  suspend fun triggerGenerateDepsWorkflowDispatchInRefreshDepsRepoAndOpenWeb() {
    cd(PCodeKt / "refreshDeps") {
      val name = "Generate Deps"
      ghWorkflowRun(name).ax()
      ghWorkflowView(name, web = true).ax()
    }
  }

  suspend fun triggerUpdateGeneratedDepsWorkflowDispatchInDepsKtRepoAndOpenWeb() {
    cd(PCodeKt / "DepsKt") {
      val name = "Update Generated Deps"
      ghWorkflowRun(name).ax()
      ghWorkflowView(name, web = true).ax()
    }
  }
}

// TODO: implement it in kommandline; see "gh browse --help"; "gh workflow --help" and add more features/flags

fun ghBrowse(
  branch: String? = null,
  commit: String? = null,
  showReleases: Boolean = false,
  showSettings: Boolean = false,
  justPrintUrl: Boolean = false,
) = kommand("gh") {
  add("browse")
  branch?.let { add("-b"); add(it) }
  commit?.let { add("-c"); add(it) }
  if (showReleases) add("-r")
  if (showSettings) add("-s")
  if (justPrintUrl) add("-n")
  // there are more options but it will be rewritten in kommandline anyway, so I'm not adding all of it here.
}

fun ghWorkflowView(workflowNameOrId: String, web: Boolean = false, yaml: Boolean = false) =
  kommand("gh") {
    add("workflow")
    add("view")
    add(workflowNameOrId)
    if (web) add("--web")
    if (yaml) add("--yaml")
  }

/** Uses workflow_dispatch event to trigger workflow. */
fun ghWorkflowRun(workflowNameOrId: String) = kommand("gh", "workflow", "run", workflowNameOrId)

fun kommand(name: String, buildArgs: MutableList<String>.() -> Unit) =
  kommand(name, *buildList(buildArgs).tta)
