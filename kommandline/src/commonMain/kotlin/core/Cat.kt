package pl.mareklangiewicz.kommand.core

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kommand.*


// TODO_someday: move head/tail so separate files and create more specific kommand data classes

fun CliPlatform.readFileHeadExec(path: String, nrLines: Int = 10) =
    kommand("head", "-n", "$nrLines", path).execb(this)
fun CliPlatform.readFileFirstLineExec(path: String) =
    readFileHeadExec(path, 1).single()
fun CliPlatform.readFileTailExec(path: String, nrLines: Int = 10) =
    kommand("tail", "-n", "$nrLines", path).execb(this)
fun CliPlatform.readFileLastLineExec(path: String) =
    readFileTailExec(path, 1).single()



/**
 * If singleLine is true and the file contains more or less than one line, it throws runtime exception.
 * It should never return just part of the file.
 */
// FIXME NOW: remove all ...Exec versions
fun CliPlatform.readFileWithCatExec(file: String, singleLine: Boolean = false): String = cat { +file }.execb(this).run {
    if (singleLine) single() else joinToString("\n")
}

// it will stay here for users that try read... (will it actually suggest deprecated fun? TODO_later check)
@Deprecated("Use catReadTextFile", ReplaceWith("catReadTextFile(path)"))
fun readTextFileWithCat(path: String) = catReadTextFile(path)

@OptIn(DelicateKommandApi::class)
fun catReadTextFile(path: String) =
    cat { +path }.reduced { stdout.toList() }

@DelicateKommandApi
fun cat(init: Cat.() -> Unit = {}) = Cat().apply(init)
/**
 * [gnu coreutils cat](https://www.gnu.org/software/coreutils/manual/html_node/cat-invocation.html)
 * [linux man](https://man7.org/linux/man-pages/man1/cat.1.html)
 */
@DelicateKommandApi
data class Cat(
    override val opts: MutableList<CatOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<CatOpt> { override val name get() = "cat" }

@DelicateKommandApi
interface CatOpt: KOptTypical {

    /** Number all output lines, starting with 1. This option is ignored if -b is in effect. */
    data object NumberAll : KOptS("n"), CatOpt

    /** Number all nonempty output lines, starting with 1. */
    data object NumberNonBlank : KOptS("b"), CatOpt

    /** Suppress repeated adjacent blank lines; output just one empty line instead of several. */
    data object SqueezeBlank : KOptS("s"), CatOpt

    /** Display a ‘$’ after the end of each line. The \r\n combination is shown as ‘^M$’. */
    data object ShowLineEnds : KOptS("E"), CatOpt

    /** Display TAB characters as ‘^I’. */
    data object ShowTabs : KOptS("T"), CatOpt

    /**
     * Display control characters except for LFD and TAB using ‘^’ notation
     * and precede characters that have the high bit set with ‘M-’.
     */
    data object ShowNonPrinting : KOptS("v"), CatOpt

    /** Equivalent to -vET. */
    data object ShowAll : KOptS("A"), CatOpt

    /** Equivalent to -vE. */
    data object ShowNonPrintingAndLineEnds : KOptS("e"), CatOpt

    /** Equivalent to -vT. */
    data object ShowNonPrintingAndTabs : KOptS("t"), CatOpt

    data object Help : KOptL("help"), CatOpt

    data object Version : KOptL("--version"), CatOpt

    data object EOOpt : KOptL(""), CatOpt
}
