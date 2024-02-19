@file:OptIn(ExperimentalApi::class)
@file:Suppress("unused")

package pl.mareklangiewicz.kommand.jupyter

import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.kommand.gnome.*

@ExampleApi
object MainExamples {

    @OptIn(DelicateApi::class)
    suspend fun examplesToRefactor() = withPrintingBadStreams {
        println("Let's play with kommand integration...")
        ls { -LsOpt.LongFormat; -LsOpt.All }.x {
            println("out line: $it")
        }
        SYS.startInTermIfUserConfirms(EchoSamples.echoTwoParagraphsWithEscapes.kommand)
        // gitStatus().x().logEach()
        // searchCommandScript("pip").x()?.logEach()
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
        val out = findBoringCodeDirsAndReduceAsExcludedFoldersXml(myKotlinPath, withOnEachLog = true).x()
        writeToFileAndOpenInGVim(out)

        // TODO_someday: use URE to inject it into /code/kotlin/kotlin.iml (and/or: /code/kotlin/.idea/kotlin.iml)
        gvim("/home/marek/code/kotlin/kotlin.iml").x()
        gvim("/home/marek/code/kotlin/.idea/kotlin.iml").x()
    }

    @OptIn(DelicateApi::class)
    suspend fun showMarekLangiewiczRepoMarkdownListInGVim() {
        val reposMdContent = GhSamples.mareklangiewiczPublicRepoMarkdownList.reducedKommand.x()
        println(reposMdContent)
        val tmpReposFileMd = SYS.pathToUserTmp + "/tmp.repos.md"
        writeToFileAndOpenInGVim(reposMdContent, tmpReposFileMd)
    }

    @DelicateApi
    suspend fun writeToFileAndOpenInGVim(content: String, filePath: String = SYS.pathToUserTmp + "/tmp.notes") {
        echo(content).x(outFile = filePath)
        gvim(filePath).x()
    }
}



