@file:Suppress("unused")

package pl.mareklangiewicz.kommand.find

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.FindExpr.*
import pl.mareklangiewicz.kommand.find.FindOpt.*
import pl.mareklangiewicz.kommand.samples.*
import kotlin.reflect.*

private val myHomePath = "/home/marek"
private val myTmpPath = "$myHomePath/tmp"
private val myKotlinPath = "$myHomePath/code/kotlin"
private val myDepsKtPath = "$myKotlinPath/DepsKt"
private val myKommandLinePath = "$myKotlinPath/KommandLine"

object FindSamples {
    val findAbcIgnoreCase =
        find(".", BaseName("*abc*", ignoreCase = true)) s "find . -iname *abc*"
    val findAbcWithFollowSymLinksAndOptimisation2 =
        find(".", BaseName("*abc*")) { -SymLinkFollowAlways; -Optimisation(2) } s
                "find -L -O2 . -name *abc*"
    val findSomeSamples = findRegularBaseName(myKommandLinePath, "*Samples.kt") s
            "find $myKommandLinePath -name *Samples.kt -type f"
    val findBigFiles = find(".", FileSize(NumArg.MoreThan(100), 'M')) s
            "find . -size +100M"
    val findAndPrint0AbcFilesAndTheirSizes =
        findTypeBaseName(".", "f", "*abc*", whenFoundPrintF = "%p\\0%s\\0") s
                "find . -name *abc* -type f -printf %p\\0%s\\0"
    val findSymLinksToKtsFilesInDepsKt = find(myDepsKtPath, SymLinkTo("*.kts")) s
            "find $myDepsKtPath -lname *.kts"
    val findDepthMax2FilesInDepsKtAndRunFileOnEach =
        find(myDepsKtPath, DepthMax(2), ActExec(kommand("file", "{}"))) s
                "find $myDepsKtPath -maxdepth 2 -execdir file {} ;"
    // WARNING: Dangerous sample! If executed, it can automatically delete a lot of files!! (but it's just tmp dir)
    val findAndDeleteAllBigFiles =
        find(myTmpPath, FileSize(NumArg.MoreThan(100), 'M'), ActPrint, ActDelete) s
                "find $myTmpPath -size +100M -print -delete"
    val findInKotlinKtFilesModifiedIn24h =
        find(
            myKotlinPath,
            OpParent(BaseName("build"), FileType("d"), ActPrune, AlwaysFalse),
            OpOr,
            OpParent(BaseName("*.kt"), ModifTime24h(NumArg.Exactly(0)), ActPrint)
        ) s "find $myKotlinPath ( -name build -type d -prune -false ) -o ( -name *.kt -mtime 0 -print )"
    val findInKotlinDirBuildDirs =
        findDirBaseName(myKotlinPath, "build", whenFoundPrune = true) s
                "find $myKotlinPath -name build -type d -print -prune"
    val findInKotlinDirNodeModulesDirs =
        findDirBaseName(myKotlinPath, "node_modules", whenFoundPrune = true) s
                "find $myKotlinPath -name node_modules -type d -print -prune"

    // TODO_someday: browser+executor UI for execs/wrappers; then add a similar list to other samples
    val execs: List<KFunction<*>> = listOf(
        CliPlatform::findExec,
        CliPlatform::findDetailsTableExec,
        CliPlatform::findTypicalDetailsTableExec,
    )
}

