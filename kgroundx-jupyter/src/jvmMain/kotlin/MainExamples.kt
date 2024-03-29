@file:Suppress("unused")

package pl.mareklangiewicz.kgroundx.jupyter

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kgroundx.maintenance.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.ure.*


@ExampleApi
object MainExamples {

    @OptIn(DelicateApi::class)
    suspend fun interplayKGroundAndKommand() {
        println("Let's play with kground and kommand integration...")
        // ls { -LsOpt.LongFormat; -LsOpt.All }.ax {
        //     println("out line: $it")
        // }
        // TODO: sth more impressive but still safe as default call in Playground.play()
    }

    suspend fun checkMyRegionsAndWorkflows(onlyPublic: Boolean = true) {
        "Check all known regions synced?" ifYesRun
                { checkAllKnownRegionsSynced(verbose = true) }
        "Check all known regions in ALL my projects? (onlyPublic = $onlyPublic)" ifYesRun
                { checkAllKnownRegionsInMyProjects(onlyPublic = onlyPublic) }
        "Check my dworkflows in ALL my projects? (onlyPublic = $onlyPublic)" ifYesRun
                { checkMyDWorkflowsInMyProjects(onlyPublic = onlyPublic)
        }
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


    private suspend infix fun String.ifYesRun(code: suspend () -> Unit) {
        println("Question: $this")
        val yes = zenityAskIf(this).ax()
        println("Answer: " + if (yes) "Yes" else "No")
        if (yes) code()
    }
}
