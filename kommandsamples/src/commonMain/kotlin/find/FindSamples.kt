@file:Suppress("unused")

package pl.mareklangiewicz.kommand.find

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.FindExpr.*
import pl.mareklangiewicz.kommand.find.FindOpt.*
import pl.mareklangiewicz.kommand.samples.*
import kotlin.reflect.*

private val depsKtPath = "/home/marek/code/kotlin/DepsKt"
private val myTmpPath = "/home/marek/tmp"

object FindSamples {
    val findAbcIgnoreCase =
        find(".", BaseName("*abc*", ignoreCase = true)) s "find . -iname *abc*"
    val findAbcWithFollowSymLinksAndOptimisation2 =
        find(".", BaseName("*abc*")) { -SymLinkFollowAlways; -Optimisation(2) } s
                "find -L -O2 . -name *abc*"
    val findSomeSamples = findRegularBaseName("/home/marek/code/kotlin/KommandLine", "*Samples.kt") s
            "find /home/marek/code/kotlin/KommandLine -name *Samples.kt -type f"
    val findBigFiles = find(".", FileSize(NumArg.MoreThan(100), 'M')) s
            "find . -size +100M"
    val findAndPrint0AbcFilesAndTheirSizes =
        findTypeBaseName(".", "f", "*abc*", whenFoundPrintF = "%p\\0%s\\0") s
                "find . -name *abc* -type f -printf %p\\0%s\\0"
    val findSymLinksToKtsFilesInDepsKt = find(depsKtPath, SymLinkTo("*.kts")) s
            "find $depsKtPath -lname *.kts"
    val findDepthMax2FilesInDepsKtAndRunFileOnEach =
        find(depsKtPath, DepthMax(2), ActExec(kommand("file", "{}"))) s
                "find $depsKtPath -maxdepth 2 -execdir file {} ;"
    // WARNING: Dangerous sample! If executed, it can automatically delete a lot of files!! (but it's just tmp dir)
    val findAndDeleteAllBigFiles =
        find(myTmpPath, FileSize(NumArg.MoreThan(100), 'M'), ActPrint, ActDelete) s
                "find $myTmpPath -size +100M -print -delete"
    val findBuildDirs =
        findDirBaseName("/home/marek/code/kotlin", "build", whenFoundPrune = true) s
                "find /home/marek/code/kotlin -name build -type d -print -prune"
    val findNodeModulesDirs =
        findDirBaseName("/home/marek/code/kotlin", "node_modules", whenFoundPrune = true) s
                "find /home/marek/code/kotlin -name node_modules -type d -print -prune"

    // TODO_someday: browser+executor UI for execs/wrappers; then add a similar list to other samples
    val execs: List<KFunction<*>> = listOf(
        CliPlatform::findExec,
        CliPlatform::findDetailsTableExec,
        CliPlatform::findTypicalDetailsTableExec,
    )
}

