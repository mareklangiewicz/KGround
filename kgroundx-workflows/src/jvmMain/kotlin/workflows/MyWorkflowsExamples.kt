@file:Suppress("unused")

package pl.mareklangiewicz.kgroundx.workflows

import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.kgroundx.maintenance.PCodeKt


@ExampleApi
object MyWorkflowsExamples {

  suspend fun checkAllMDW() = checkMyDWorkflowsInMyProjects(onlyPublic = false)

  suspend fun injectMDWToMyProjects() = injectMyDWorkflowsToMyProjects(onlyPublic = false)

  suspend fun injectDWToExampleProject() = injectDWorkflowsToProject(PCodeKt / "AbcdK")

  suspend fun injectGenerateDepsWToRefreshDepsRepo() = injectHackyGenerateDepsWorkflowToRefreshDepsRepo()

  suspend fun injectUpdateGeneratedDepsToDepsKtRepo() = injectUpdateGeneratedDepsWorkflowToDepsKtRepo()
}
