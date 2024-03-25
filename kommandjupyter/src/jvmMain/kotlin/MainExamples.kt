@file:OptIn(ExperimentalApi::class)
@file:Suppress("unused")

package pl.mareklangiewicz.kommand.jupyter

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.ulog.i

@ExampleApi
object MainExamples {

    @OptIn(DelicateApi::class)
    suspend fun examplesToRefactor() = withLogBadStreams {
        readFileHead("/home/marek/non-existent-file-46578563").ax() // should print BadExitStateErr.stderr
        ls { -LsOpt.LongFormat; -LsOpt.All }.ax {
            ulog.i("out line: $it")
        }
        // EchoSamples.echoTwoParagraphsWithEscapes.kommand.startInTermIfUserConfirms(SYS)
        // MyDemoSamples.btopKitty.ax()
        // MyDemoSamples.ps1.ax()
        // gitStatus().ax().logEach()
        // searchCommand("pip").ax()?.logEach()
        // DpkgSamples.searchZenity.ax()?.logEach()
        // SshSamples.sshPimInTermGnome.ax()
        // SshSamples.sshPimLsInTermKitty.ax()
        // SshSamples.sshPimLsLAH.ax(errToOut = true).logEach()
        // prepareMyExcludeFolderInKotlinMultiProject()
        // showMarekLangiewiczRepoMarkdownListInGVim()
        // readFileHead("/home/marek/non-existent-file-46578563").ax() // should print BadExitStateErr.stderr
        // readFileHead("/home/marek/.vimrc").ax().logEach()
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
    suspend fun showMyRepoMarkdownListInGVim() {
        val reposMdContent = GhSamples.myPublicRepoMarkdownList.reducedKommand.ax()
        val tmpReposFileMd = SYS.pathToUserTmp + "/tmp.repos.md"
        writeToFileAndOpenInGVim(reposMdContent, tmpReposFileMd)
    }

    @DelicateApi
    suspend fun writeToFileAndOpenInGVim(content: String, filePath: String = SYS.pathToUserTmp + "/tmp.notes") {
        echo(content).ax(outFile = filePath)
        gvim(filePath).ax()
    }
}



