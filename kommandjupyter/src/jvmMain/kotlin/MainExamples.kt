@file:OptIn(ExperimentalApi::class)
@file:Suppress("unused")

package pl.mareklangiewicz.kommand.jupyter

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.github.*

@ExampleApi
object MainExamples {

    @OptIn(DelicateApi::class)
    suspend fun examplesToRefactor() = withPrintingBadStreams {
        println("Let's play with kommand integration...")
        ls { -LsOpt.LongFormat; -LsOpt.All }.ax {
            println("out line: $it")
        }
        // EchoSamples.echoTwoParagraphsWithEscapes.kommand.startInTermIfUserConfirms(SYS)
        // MyDemoSamples.btopKitty.x()
        // MyDemoSamples.ps1.x()
        // gitStatus().x().logEach()
        // searchCommand("pip").x()?.logEach()
        // DpkgSamples.searchZenity.x()?.logEach()
        // SshSamples.sshPimInTermGnome.x()
        // SshSamples.sshPimLsInTermKitty.x()
        // SshSamples.sshPimLsLAH.x(errToOut = true).logEach()
        // prepareMyExcludeFolderInKotlinMultiProject()
        // showMarekLangiewiczRepoMarkdownListInGVim()
        // readFileHead("/home/marek/non-existent-file-46578563").x() // should print BadExitStateErr.stderr
        // readFileHead("/home/marek/.vimrc").x().logEach()
    }

    @OptIn(DelicateApi::class)
    suspend fun prepareMyExcludeFolderInKotlinMultiProject() {
        val out = findBoringCodeDirsAndReduceAsExcludedFoldersXml(myKotlinPath, withOnEachLog = true).ax()
        writeToFileAndOpenInGVim(out)

        // TODO_someday: use URE to inject it into /code/kotlin/kotlin.iml (and/or: /code/kotlin/.idea/kotlin.iml)
        gvim("/home/marek/code/kotlin/kotlin.iml").ax()
        gvim("/home/marek/code/kotlin/.idea/kotlin.iml").ax()
    }

    @OptIn(DelicateApi::class)
    suspend fun showMarekLangiewiczRepoMarkdownListInGVim() {
        val reposMdContent = GhSamples.mareklangiewiczPublicRepoMarkdownList.reducedKommand.ax()
        println(reposMdContent)
        val tmpReposFileMd = SYS.pathToUserTmp + "/tmp.repos.md"
        writeToFileAndOpenInGVim(reposMdContent, tmpReposFileMd)
    }

    @DelicateApi
    suspend fun writeToFileAndOpenInGVim(content: String, filePath: String = SYS.pathToUserTmp + "/tmp.notes") {
        echo(content).ax(outFile = filePath)
        gvim(filePath).ax()
    }
}



