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
import pl.mareklangiewicz.ure.*

@ExampleApi
object MyExamples {

    // TODO NOW: refactor it all - moved from kotlinx-jupyter:MainExamples

    suspend fun interplayKGroundAndKommand() {
        println("Let's play with kground and kommand integration...")
        ls { -LsOpt.LongFormat; -LsOpt.All }.ax().logEach()
    }

    suspend fun checkMyRegionsAndWorkflows(onlyPublic: Boolean = true) {
        "Check all known regions synced?" ifYesRun
                { checkAllKnownRegionsSynced(verbose = true) }
        "Check all known regions in ALL my projects? (onlyPublic = $onlyPublic)" ifYesRun
                { checkAllKnownRegionsInMyProjects(onlyPublic = onlyPublic) }
        "Check my dworkflows in ALL my projects? (onlyPublic = $onlyPublic)" ifYesRun
                { checkMyDWorkflowsInMyProjects(onlyPublic = onlyPublic) }
    }

    suspend fun dangerousInjectStuffToMyProjects(onlyPublic: Boolean = true, exampleProjName: String = "AbcdK") {
        "DANGEROUS! Inject all known regions to sync?" ifYesRun
                { injectAllKnownRegionsToSync() }
        "DANGEROUS! Inject all known regions to ALL my projects? (onlyPublic = $onlyPublic)" ifYesRun
                { injectAllKnownRegionsToMyProjects() }
        "DANGEROUS! Inject DWorkflows to $exampleProjName project?" ifYesRun
                { injectDWorkflowsToKotlinProject(exampleProjName) }
        "DANGEROUS! Inject my DWorkflows to ALL my projects? (onlyPublic = $onlyPublic)" ifYesRun
                { injectMyDWorkflowsToMyProjects(onlyPublic = onlyPublic) }
        "DANGEROUS! Inject hacky Generate Deps workflow to refreshDeps repo?" ifYesRun
                { injectHackyGenerateDepsWorkflowToRefreshDepsRepo() }
        "DANGEROUS! Inject Update Generated Deps workflow to DepsKt repo?" ifYesRun
                { injectUpdateGeneratedDepsWorkflowToDepsKtRepo() }
    }

    suspend fun dangerousUpdateStuffInMyProjects(onlyPublic: Boolean = true, exampleProjName: String = "AbcdK") {
        "DANGEROUS! Update KGround resources symlinks?" ifYesRun
                { updateKGroundResourcesSymLinks() }
        "DANGEROUS! Update GradleW files in $exampleProjName project?" ifYesRun
                { updateGradlewFilesInKotlinProject(exampleProjName) }
        "DANGEROUS! Update GradleW files in ALL my projects? (onlyPublic = $onlyPublic)" ifYesRun
                { updateGradlewFilesInMyProjects(onlyPublic = onlyPublic) }
    }

    suspend fun searchTextInKotlinCodeInMyProjects(text: String) {
        "Search for text: \"$text\" in kotlin code in ALL my projects?" ifYesRun
                { searchKotlinCodeInMyProjects(ureText(text).withOptWhatevaAroundInLine()) }
        // TODO: ask and put results into intellij
    }

    @OptIn(DelicateApi::class, NotPortableApi::class)
    suspend fun dirtyFixMyReposOrigins() {

        val kget = kommand("git", "remote", "get-url", "origin")
        fun kset(url: String) = kommand("git", "remote", "set-url", "origin", url)

        val ureRepoUrl = ure {
            + ureText("git@github.com:")
            + ureIdent().withName("user")
            + ch('/')
            + ureIdent(allowDashesInside = true).withName("project")
            + ureText(".git")
        }

        fetchMyProjectsNameS(onlyPublic = false)
            .map { PathToMyKotlinProjects / it }
            .collect { dir ->
                println(dir)
                val url = kget.ax(dir = dir.toString()).single()
                println(url)
                val result = ureRepoUrl.matchEntireOrThrow(url)
                val vals = result.namedValues
                val user = vals["user"]!!
                val project = vals["project"]!!
                if (user == "mareklangiewicz") {
                    println("User is already mareklangiewicz.")
                    return@collect
                }
                user chkEq "langara"
                val newUrl = "git@github.com:mareklangiewicz/$project.git"
                println("*** SETTING ORIGIN -> $newUrl ***")
                kset(newUrl).ax(dir = dir.toString())
            }
    }



    private suspend infix fun String.ifYesRun(code: suspend () -> Unit) {
        println("Question: $this")
        val yes = zenityAskIf(this).ax()
        println("Answer: " + if (yes) "Yes" else "No")
        if (yes) code()
    }
}