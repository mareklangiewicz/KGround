@file:Suppress("unused")

package pl.mareklangiewicz.kommand.find

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.FindExpr.*
import pl.mareklangiewicz.kommand.find.FindOpt.*
import pl.mareklangiewicz.kommand.samples.*

val myHomePath = "/home/marek"
val myTmpPath = "$myHomePath/tmp"
val myKotlinPath = "$myHomePath/code/kotlin"
val myDepsKtPath = "$myKotlinPath/DepsKt"
val myKommandLinePath = "$myKotlinPath/KommandLine"

@OptIn(DelicateKommandApi::class)
data object FindSamples {

    val findAbcIgnoreCase =
        find(".", NameBase("*abc*", ignoreCase = true)) s
                "find . -iname *abc*"

    val findAbcWithFollowSymLinksAndOptimisation2 =
        find(".", NameBase("*abc*")) { -SymLinkFollowAlways; -Optimisation(2) } s
                "find -L -O2 . -name *abc*"

    val findSomeSamples =
        findRegularNameBase(myKommandLinePath, "*Samples.kt") s
                "find $myKommandLinePath -name *Samples.kt -type f"

    val findBigFiles =
        find(".", FileSize(NumArg.MoreThan(100), 'M')) s
                "find . -size +100M"

    val findAndPrint0AbcFilesAndTheirSizes =
        findTypeNameBase(".", "f", "*abc*", whenFoundPrintF = "%p\\0%s\\0") s
                "find . -name *abc* -type f -printf %p\\0%s\\0"

    val findSymLinksToKtsFilesInDepsKt =
        find(myDepsKtPath, SymLinkTo("*.kts")) s
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
            OpParent(NameBase("build"), FileType("d"), ActPrune, AlwaysFalse),
            OpOr,
            OpParent(NameBase("*.kt"), ModifTime24h(NumArg.Exactly(0)), ActPrint)
        ) s
                "find $myKotlinPath ( -name build -type d -prune -false ) -o ( -name *.kt -mtime 0 -print )"

    val findInKotlinDirBuildDirs =
        findDirNameBase(myKotlinPath, "build", whenFoundPrune = true) s
                "find $myKotlinPath -name build -type d -print -prune"

    val findInKotlinDirNodeModulesDirs =
        findDirNameBase(myKotlinPath, "node_modules", whenFoundPrune = true) s
                "find $myKotlinPath -name node_modules -type d -print -prune"

    val findInKotlinDirBoringDirs =
        findBoringCodeDirs(myKotlinPath) s
                "find $myKotlinPath -regex .*/\\(build\\|node_modules\\|\\.gradle\\) -type d -print -prune"

    val findMyLastWeekKotlinCode =
        findMyKotlinCode(withModifTime24h = NumArg.LessThan(8)) s
                "find $myKotlinPath ( ( -name build -type d -prune -false ) -o ( -path */src/*/kotlin/* -name *.kt -type f -mtime -8 -print ) )"

    val findTypicalDetailsTableInParentDir =
        findTypicalDetailsTable("..") ts
                """find .. -name * -type f -printf """ +
                """%A+\0\0%C+\0\0%T+\0\0%B+\0\0%d\0\0%s\0\0%h\0\0%f\0\0%p\0\0%g\0\0%u\0\0%m\0\0%M\0\n"""
                    // Note: this expected lineRaw was just copied and pasted from actual result (somewhat bad practice),
                    // but the point is to have it here as kinda "screenshot" and to be noticed when sth changes.

}

private val boringCodeDirRegexes = listOf("build", "node_modules", "\\.gradle")
// order is important build before node_modules, because usually node_modules are inside build,
// so no need to search for it it as it will be marked as boring anyway (whole build pruned) in such case

/** Usually to exclude from some indexing. */
fun findBoringCodeDirs(path: String) = findDirRegex(path,
    regexName = boringCodeDirRegexes.joinToString("\\|", prefix = ".*/\\(", postfix = "\\)"),
    whenFoundPrune = true,
)

fun findBoringCodeDirsAndReduceAsExcludedFoldersXml(
    path: String,
    indent: String = "      ",
    urlPrefix: String = "file://\$MODULE_DIR\$",
    withOnEachLog: Boolean = false,
) =
    findBoringCodeDirs(path).reduced {
        stdout
            .map {
                check(it.startsWith(myKotlinPath))
                it.removePrefix(myKotlinPath)
            }
            .map { "$indent<excludeFolder url=\"$urlPrefix$it\" />" }
            .let { if (withOnEachLog) it.onEachLog() else it }
            .toList().sorted().joinToString("\n")
    }

/**
 * Note: null means do not use given test/filter/limit at all.
 * @param withModifTime24h
 *   Exactly(0) will return files modified within last 24h,
 *   LessThan(7) for files modified in last few days.
 */
@OptIn(DelicateKommandApi::class)
fun findMyKotlinCode(
    kotlinCodePath: String = myKotlinPath,
    vararg useNamedArgs: Unit,
    withGrepRE: String? = null,
    withNameRegex: String? = null,
    withNameBase: String? = "*.kt",
    withNameFull: String? = "*/src/*/kotlin/*",
    withPruneBuildDirsNamed: String? = "build",
    withModifTime24h: NumArg? = null,
) = find(
    kotlinCodePath,
    fexprWithPrunedDirs(
        withPruneBuildDirsNamed,
        withNameRegex?.let(::NameRegex),
        withNameFull?.let(::NameFull),
        withNameBase?.let(::NameBase),
        FileType("f"),
        withModifTime24h?.let(::ModifTime24h),
        fexprActExecGrepPrintIfMatched(withGrepRE),
    )
)

private fun fexprActExecGrepPrintIfMatched(grepRE: String?) =
    if (grepRE == null) ActPrint
    else OpParent(
        ActExec(grepQuietly(grepRE, "{}")), ActPrint
    )

/**
 * @param prunedDirsNamed null means do not prune anything at all
 * A lot of OpParent here, but it's necessary until I have better operators wrappers
 * (see fixme_comment above Find.kt:operator fun FindExpr.not)
 * @param expr Expression (joined by "and" by default), null elements are ignored
 */
private fun fexprWithPrunedDirs(prunedDirsNamed: String?, vararg expr: FindExpr?) =  OpParent(
    prunedDirsNamed?.let {
        OpParent(
            NameBase(it), FileType("d"),
            ActPrune, AlwaysFalse
        )
    } ?: AlwaysFalse,
    OpOr, OpParent(expr.filterNotNull()),
)

// TODO_later: full grep kommand wrapper class+funs.
private fun grepQuietly(regexp: String, vararg files: String) =
    kommand("grep", "-q", regexp, *files)

private fun grepWithDetails(regexp: String, vararg files: String) =
    kommand("grep", "-H", "-n", "-T", "-e", regexp, *files)

