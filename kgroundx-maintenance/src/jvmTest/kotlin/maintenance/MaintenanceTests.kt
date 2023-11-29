@file:Suppress("UnusedImport")

package pl.mareklangiewicz.maintenance

import kotlinx.coroutines.*
import okio.Path.Companion.toPath
import org.junit.jupiter.api.*
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import pl.mareklangiewicz.ure.*
import pl.mareklangiewicz.uspek.*

class MaintenanceTests {

    // TODO: I have to run it from command line, due to issue (todo track it):
    // https://youtrack.jetbrains.com/issue/IDEA-320303
    // KGround$ ./gradlew cleanTest
    // KGround$ ./gradlew test --tests MaintenanceTests.maintenanceTestFactory
    @TestFactory
    fun maintenanceTestFactory() = uspekTestFactory {
       // "check all known regions synced" o { checkAllKnownRegionsSynced(verbose = true) }
       // "check all known regions in my kotlin projects" o { runBlocking { checkAllKnownRegionsInMyProjects() } }
       // "check my dworkflows in my projects" o { runBlocking { checkMyDWorkflowsInMyProjects(onlyPublic = true) } }

       // "DANGEROUS inject all known regions to sync" o { injectAllKnownRegionsToSync() }
       // "DANGEROUS inject all known regions to all my projects" o { runBlocking { injectAllKnownRegionsToMyProjects() } }
       // "DANGEROUS inject dworkflows to all my projects" o { runBlocking { injectMyDWorkflowsToMyProjects(onlyPublic = true) } }
       // "DANGEROUS inject dworkflows to Some Proj" o { injectDWorkflowsToKotlinProject("KommandLine") }

       // "DANGEROUS updateKGroundResourcesSymLinks" o { updateKGroundResourcesSymLinks() }
       // "DANGEROUS updateGradlewFilesInMyProjects" o { runBlocking { updateGradlewFilesInMyProjects(onlyPublic = false) } }

       // "DANGEROUS inject hacky workflow to refreshDeps repo" o { injectHackyGenerateDepsWorkflowToRefreshDepsRepo() }
       // "DANGEROUS inject updateGeneratedDeps workflow to DepsKt repo" o { injectUpdateGeneratedDepsWorkflowToDepsKtRepo() }

       // "search sth in my projects" o { runBlocking { searchKotlinCodeInMyProjects(ir("ontext recei").withOptWhatevaAroundInLine()) } }
    }
}