@file:Suppress("unused")

package pl.mareklangiewicz.kommand.find

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.FindExpr.*
import pl.mareklangiewicz.kommand.samples.*
import kotlin.reflect.*

private val depsKtPath = "/home/marek/code/kotlin/DepsKt"

object FindSamples {
    val findAbcIgnoreCase = find(".", BaseName("*abc*", ignoreCase = true)) s
            "find . -iname *abc*"
    val findSomeSamples = findRegularBaseName("/home/marek/code/kotlin/KommandLine", "*Samples.kt") s
            "find /home/marek/code/kotlin/KommandLine -type f -name *Samples.kt"
    val findBigFiles = find(".", FileSize(NumArg.MoreThan(100), 'M')) s
            "find . -size +100M"
    val findSymLinksToKtsFiles = find(depsKtPath, SymLinkTo("*.kts")) s
            "find /home/marek/code/kotlin/DepsKt -lname *.kts"
    // WARNING: Dangerous sample! If executed, it can automatically delete a lot of files!! (but it's just tmp dir)
    val findAndDeleteAllBigFiles = find("/home/marek/tmp", FileSize(NumArg.MoreThan(100), 'M'), ActPrint, ActDelete) s
            "find /home/marek/tmp -size +100M -print -delete"
    val findBuildDirs = findDirBaseName("/home/marek/code/kotlin", "build", whenFoundPrune = true) s
            "find /home/marek/code/kotlin -type d -name build -print -prune"
    val findNodeModulesDirs = findDirBaseName("/home/marek/code/kotlin", "node_modules", whenFoundPrune = true) s
            "find /home/marek/code/kotlin -type d -name node_modules -print -prune"

    // TODO_someday: browser+executor UI for execs/wrappers; then add a similar list to other samples
    val execs: List<KFunction<*>> = listOf(
        CliPlatform::findExec
    )
}
