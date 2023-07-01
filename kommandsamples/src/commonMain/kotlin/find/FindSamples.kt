@file:Suppress("unused")

package pl.mareklangiewicz.kommand.find

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.FindExpr.*
import pl.mareklangiewicz.kommand.find.FindOpt.*
import pl.mareklangiewicz.kommand.samples.*
import kotlin.reflect.*

private val depsKtPath = "/home/marek/code/kotlin/DepsKt"

object FindSamples {
    val findAbcIgnoreCase = find(".", BaseName("*abc*", ignoreCase = true)) s
            "find . -iname *abc*"
    val findAbcWithFollowSymLinksAndOptimisation2 = find(".", BaseName("*abc*")) {
        -SymLinkFollowAlways
        -Optimisation(2)
    } s "find -L -O2 . -name *abc*"
    val findSomeSamples = findRegularBaseName("/home/marek/code/kotlin/KommandLine", "*Samples.kt") s
            "find /home/marek/code/kotlin/KommandLine -type f -name *Samples.kt"
    val findBigFiles = find(".", FileSize(NumArg.MoreThan(100), 'M')) s
            "find . -size +100M"
    val findAndPrint0AbcFilesAndTheirSizes =
        findTypeBaseName(".", "f", "*abc*", whenFoundPrintF = "%p\\0%s\\0") s
            "find . -type f -name *abc* -printf %p\\0%s\\0"
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
        CliPlatform::findExec,
        CliPlatform::findDetailsTableExec,
    )
}

private val details = listOf(
    "access time" to "%A+",
    "status change time" to "%C+",
    "last modification time" to "%T+",
    "birth time" to "%B+",
    "depth" to "%d",
    "size" to "%s",
    "dir name" to "%h",
    "base name" to "%f",
    "full name" to "%p",
    "group name" to "%g",
    "user name" to "%u",
    "octal permissions" to "%m",
    "symbolic permissions" to "%M",
)
private val detailsHeadersRow = listOf(details.map { it.first })
private val detailsPrintFormat = details.joinToString("\\0", postfix = "\\0") { it.second }

fun CliPlatform.findDetailsTableExec(path: String = "."): List<List<String>> = detailsHeadersRow +
        findExec(path = path, whenFoundPrintF = detailsPrintFormat)
            .single()
            .split(Char(0))
            .windowed(details.size, details.size)

