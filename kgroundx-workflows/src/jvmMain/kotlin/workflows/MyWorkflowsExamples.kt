@file:Suppress("unused")

package pl.mareklangiewicz.kgroundx.workflows

import pl.mareklangiewicz.annotations.ExampleApi


@ExampleApi
object MyWorkflowsExamples {

  suspend fun checkAllMDW() = checkMyDWorkflowsInMyProjects(onlyPublic = false)

  suspend fun injectMDWToMyProjects() = injectMyDWorkflowsToMyProjects(onlyPublic = false)

  suspend fun injectDWToExampleProject() = injectDWorkflowsToKotlinProject(projectName = "AbcdK")

  suspend fun injectGenerateDepsWToRefreshDepsRepo() = injectHackyGenerateDepsWorkflowToRefreshDepsRepo()

  suspend fun injectUpdateGeneratedDepsToDepsKtRepo() = injectUpdateGeneratedDepsWorkflowToDepsKtRepo()
}
