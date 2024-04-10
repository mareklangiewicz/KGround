@file:Suppress("unused")

package pl.mareklangiewicz.kgroundx.maintenance

import kotlinx.coroutines.flow.map
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.ExampleApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.kground.logEach
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.LsOpt
import pl.mareklangiewicz.kommand.core.ls
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.w
import pl.mareklangiewicz.ure.*

@ExampleApi
object MyTypicalExamples {

    // TODO NOW: refactor it all - moved from kotlinx-jupyter:MainExamples

  suspend fun interplayKGroundAndKommand() {
    ulog.w("Let's play with kground and kommand integration...")
    ls { -LsOpt.LongFormat; -LsOpt.All }.ax().logEach()
    ulog.w("Let's try some ideDiff...")
    ideDiff(
      "/home/marek/code/kotlin/KGround/template-mpp/build.gradle.kts",
      "/home/marek/code/kotlin/AbcdK/build.gradle.kts"
    ).ax()
  }

  suspend fun searchAllMyKotlinCode() {
    searchKotlinCodeInMyProjects(ureText("UReports"))
  }

  /**
   * Example how I updated my repos origins after changing username on GitHub
   * Now it does nothing, because all repos have already set remote url to:
   * git@github.com:mareklangiewicz/<project>.git instead of one with "langara"
   * but let's leave it here as an example.
   */
  @OptIn(DelicateApi::class, NotPortableApi::class)
  suspend fun tryToUpdateMyReposOrigins() {

    val kget = kommand("git", "remote", "get-url", "origin")
    fun kset(url: String) = kommand("git", "remote", "set-url", "origin", url)

    val ureRepoUrl = ure {
      +ureText("git@github.com:")
      +ureIdent().withName("user")
      +ch('/')
      +ureIdent(allowDashesInside = true).withName("project")
      +ureText(".git")
    }

    fetchMyProjectsNameS(onlyPublic = false)
      .map { PathToMyKotlinProjects / it }
      .collect { dir ->
        ulog.i(dir)
        val url = kget.ax(dir = dir.toString()).single()
        ulog.i(url)
        val result = ureRepoUrl.matchEntireOrThrow(url)
        val vals = result.namedValues
        val user = vals["user"]!!
        val project = vals["project"]!!
        if (user == "mareklangiewicz") {
          ulog.i("User is already mareklangiewicz.")
          return@collect
        }
        user chkEq "langara"
        val newUrl = "git@github.com:mareklangiewicz/$project.git"
        ulog.w("setting origin -> $newUrl")
        kset(newUrl).ax(dir = dir.toString())
      }
  }
}


@ExampleApi
object MyKnownRegionsExamples {

  suspend fun checkAllMKRSynced() = checkAllKnownRegionsSynced()

  suspend fun injectAllMKRToSync() = injectAllKnownRegionsToSync()

  suspend fun checkAllMKRInMyProjects() = checkAllKnownRegionsInMyProjects(onlyPublic = false)

  suspend fun injectAllMKRToMyProjects() = injectAllKnownRegionsToMyProjects(onlyPublic = false)
}

@ExampleApi
object MyWorkflowsExamples {

  suspend fun checkAllMDW() = checkMyDWorkflowsInMyProjects(onlyPublic = false)

  suspend fun injectMDWToMyProjects() = injectMyDWorkflowsToMyProjects(onlyPublic = false)

  suspend fun injectDWToExampleProject() = injectDWorkflowsToKotlinProject(projectName = "AbcdK")

  suspend fun injectGenerateDepsWToRefreshDepsRepo() = injectHackyGenerateDepsWorkflowToRefreshDepsRepo()

  suspend fun injectUpdateGeneratedDepsToDepsKtRepo() = injectUpdateGeneratedDepsWorkflowToDepsKtRepo()

  suspend fun updateKGroundResSLinks() = updateKGroundResourcesSymLinks()

  suspend fun updateGradlewInExampleProject() = updateGradlewFilesInKotlinProject(projectName = "AbcdK")

  suspend fun updateGradlewInMyProjects() = updateGradlewFilesInMyProjects(onlyPublic = false)
}
