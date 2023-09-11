@file:OptIn(ExperimentalKommandApi::class)

package pl.mareklangiewicz.kommand.playground

import kotlinx.coroutines.*
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.debian.*
import pl.mareklangiewicz.kommand.find.*
import pl.mareklangiewicz.kommand.git.*
import pl.mareklangiewicz.kommand.github.*
import pl.mareklangiewicz.kommand.gnome.*
import pl.mareklangiewicz.upue.*


/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this file is to experiment here, with better IDE support, and then C&P working code snippets into notebooks.
 * Do not commit changes in this file. The kommandjupyter:run task is set up to run the main fun here.
 */

fun main() {
    runBlocking {
        playground()
    }
}

@OptIn(DelicateKommandApi::class)
suspend fun playground() = withPrintingBadStreams {
    println("Let's play with kommand integration...")
    ls { -LsOpt.LongFormat; -LsOpt.All }.x {
        println("out line: $it")
    }
    SYS.startInTermIfUserConfirms(EchoSamples.echoTwoParagraphsWithEscapes.kommand)
//    gitStatus().x().logEach()
//    searchCommandScript("pip").x()?.logEach()
//    SshSamples.sshPimInTermGnome.x()
//    SshSamples.sshPimLsInTermKitty.x()
//    SshSamples.sshPimLsLAH.x(errToOut = true).logEach()
//    prepareMyExcludeFolderInKotlinMultiProject()
//    showLangaraRepoMarkdownListInIdeaP()
//    readFileHead("/home/marek/non-existent-file-46578563").x() // should print BadExitStateErr.stderr
//    readFileHead("/home/marek/.vimrc").x().logEach()
}

@OptIn(DelicateKommandApi::class)
suspend fun prepareMyExcludeFolderInKotlinMultiProject() {
    val out = findBoringCodeDirsAndReduceAsExcludedFoldersXml(myKotlinPath, withOnEachLog = true).x()
    writeToFileAndOpenInIdeaP(out)

    // TODO_someday: use URE to inject it into /code/kotlin/kotlin.iml (and/or: /code/kotlin/.idea/kotlin.iml)
    ideap("/home/marek/code/kotlin/kotlin.iml").x()
    ideap("/home/marek/code/kotlin/.idea/kotlin.iml").x()
}



@OptIn(DelicateKommandApi::class)
suspend fun showLangaraRepoMarkdownListInIdeaP() {
    val reposMdContent = GhSamples.langaraPublicRepoMarkdownList.reducedKommand.x()
    println(reposMdContent)
    val tmpReposFileMd = SYS.pathToUserTmp + "/tmp.repos.md"
    writeToFileAndOpenInIdeaP(reposMdContent, tmpReposFileMd)
}

@DelicateKommandApi
suspend fun writeToFileAndOpenInIdeaP(content: String, filePath: String = SYS.pathToUserTmp + "/tmp.notes") {
    echo(content).x(outFile = filePath)
    ideap(filePath).x()
}
