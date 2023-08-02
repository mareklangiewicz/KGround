package pl.mareklangiewicz.kommand.playground

import kotlinx.coroutines.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.github.*


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

suspend fun playground() {
    println("Let's play with kommand integration...")
    ls { -LsOpt.LongFormat; -LsOpt.All }.x {
        println("out line: $it")
    }
    showLangaraRepoMarkdownListInIdeaP()
}



suspend fun showLangaraRepoMarkdownListInIdeaP() {
    val reposMdContent = GhSamples.langaraPublicRepoMarkdownList.reducedKommand.x()
    println(reposMdContent)
    val tmpReposFileMd = SYS.pathToUserTmp + "/tmp.repos.md"
    echo(reposMdContent).x(outFile = tmpReposFileMd)
    ideap { +tmpReposFileMd }.x()
}
