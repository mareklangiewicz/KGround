package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.core.LsOpt.ColorType.*
import pl.mareklangiewicz.kommand.core.LsOpt.IndicatorStyle.*

fun CliPlatform.lsExec(vararg paths: String, withHidden: Boolean = false, style: IndicatorStyle = NONE) =
    ls(*paths, withHidden = withHidden, style = style).exec()

fun CliPlatform.lsRegFilesExec(dir: String, withHidden: Boolean = false) =
    lsExec(dir, withHidden = withHidden, style = SLASH).filter { !it.endsWith('/') }

fun CliPlatform.lsSubDirsExec(dir: String, withHidden: Boolean = false) =
    lsExec(dir, withHidden = withHidden, style = SLASH).filter { it.endsWith('/') }.map { it.dropLast(1) }

fun ls(vararg paths: String, withHidden: Boolean = false, style: IndicatorStyle = NONE) =
    lsPredictable(*paths, withHidden = withHidden, style = style)

/** lsPredictable is better to get a more predictable output format, especially for parsing. */
fun lsPredictable(vararg paths: String, withHidden: Boolean = false, style: IndicatorStyle = NONE) =
    ls { for (p in paths) +p; -One; -DirsFirst; -Color(NEVER); -Escape; -Indicator(style); if (withHidden) -AlmostAll }

/**
 * lsDefault is ls without any options; uses default settings on given CliPlatform.
 * lsPredictable is better to get a more predictable output format, especially for parsing.
 */
fun lsDefault(vararg files: String) = ls { for (f in files) +f }

fun ls(init: Ls.() -> Unit = {}) = Ls().apply(init)


/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
data class Ls(
    val opts: MutableList<LsOpt> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {
    override val name get() = "ls"
    override val args get() = opts.flatMap { it.toArgs() } + files
    operator fun String.unaryPlus() = files.add(this)
    operator fun LsOpt.unaryMinus() = opts.add(this)
}

interface LsOpt: KOpt {

    /** List one file per line.  You can avoid '\n' by adding options: hideControlChars or escape */
    object One: KOptS("1"), LsOpt

    /** do not ignore entries starting with "." */
    object All : KOptS("a"), LsOpt
    /** do not list implied "." and ".." */
    object AlmostAll : KOptS("A"), LsOpt

    object Author : KOptL("author"), LsOpt

    object Escape : KOptL("escape"), LsOpt

    data class BlockSize(val size: String): KOptL("block-size", size), LsOpt

    object IgnoreBackups : KOptL("ignore-backups"), LsOpt

    object ListByColumns : KOptS("C"), LsOpt
    object ListByLines : KOptS("x"), LsOpt

    data class Color(val type: ColorType): KOptL("color", "$type"), LsOpt
    enum class ColorType { ALWAYS, AUTO, NEVER;
        override fun toString() = super.toString().lowercase()
    }

    object Directory : KOptS("d"), LsOpt

    object Dired : KOptL("dired"), LsOpt

    /** do not sort, enable -aU, disable -ls --color */
    object Raw : KOptS("f"), LsOpt

    /** append indicator (one of *=/>@|) to entries */
    object Classify : KOptS("F"), LsOpt

    /** like classify, except do not add * symbol */
    object ClassifyFileType : KOptL("file-type"), LsOpt

    data class Format(val type: FormatType): KOptL("--format=$type"), LsOpt
    enum class FormatType { ACROSS, COMMAS, HORIZONTAL, LONG, SINGLECOLUMN, VERBOSE, VERTICAL;
        override fun toString() = super.toString().lowercase()
    }

    /** like -l --time-style=full-iso */
    object FullTime : KOptL("full-time"), LsOpt

    object DirsFirst : KOptL("group-directories-first"), LsOpt

    object NoGroup : KOptS("G"), LsOpt

    object LongFormat : KOptS("l"), LsOpt // option name is not just Long to avoid clash with Long data type
    object LongWithoutOwner : KOptS("g"), LsOpt
    object LongWithoutGroup : KOptS("o"), LsOpt

    object HumanReadable : KOptS("h"), LsOpt
    object HumanReadableSI : KOptL("si"), LsOpt

    object Dereference : KOptL("dereference"), LsOpt
    object DereferenceCommandLine : KOptL("dereference-command-line"), LsOpt
    object DereferenceCommandLineSymlinkToDir : KOptL("dereference-command-line-symlink-to-dir"), LsOpt

    data class Hide(val pattern: String): KOptL("hide", pattern), LsOpt
    /** print ? instead of nongraphic characters */
    object HideControlChars : KOptL("hide-control-chars"), LsOpt
    /** show nongraphic characters as-is (the default, unless program is 'ls' and output is a terminal) */
    object ShowControlChars : KOptL("show-control-chars"), LsOpt

    data class Hyperlink(val type: HyperlinkType): KOptL("hyperlink", "$type"), LsOpt
    enum class HyperlinkType { ALWAYS, AUTO, NEVER;
        override fun toString() = super.toString().lowercase()
    }

    data class Indicator(val style: IndicatorStyle): KOptL("indicator-style", "$style"), LsOpt
    enum class IndicatorStyle { NONE, SLASH, FILETYPE, CLASSIFY;
        override fun toString() = if (this == FILETYPE) "file-type" else super.toString().lowercase()
    }
    object IndicatorSlash : KOptS("p"), LsOpt

    object INode : KOptL("inode"), LsOpt

    data class Ignore(val pattern: String): KOptL("ignore", pattern), LsOpt

    object Kibibytes : KOptL("kibibytes"), LsOpt

    object Commas : KOptS("m"), LsOpt

    object NumericUidGid : KOptL("numeric-uid-gid"), LsOpt

    object Literal : KOptL("literal"), LsOpt

    object QuoteName : KOptL("quote-name"), LsOpt

    data class Quoting(val style: QuotingStyle): KOptL("quoting-style", "$style"), LsOpt
    enum class QuotingStyle { LITERAL, LOCALE, SHELL, SHELLALWAYS, SHELLESCAPE, SHELLESCAPEALWAYS, C, ESCAPE;
        override fun toString() = when (this) {
            SHELLALWAYS -> "shell-always"
            SHELLESCAPE -> "shell-escape"
            SHELLESCAPEALWAYS -> "shell-escape-always"
            else -> super.toString().lowercase()
        }
    }

    object Reverse : KOptS("r"), LsOpt

    object Recursive : KOptS("R"), LsOpt

    object Size : KOptS("s"), LsOpt

    data class Sort(val type: SortType): KOptL("sort", "$type"), LsOpt
    enum class SortType { NONE, SIZE, TIME, VERSION, EXTENSION;
        override fun toString() = super.toString().lowercase()
    }
    /** largest first */
    object SortBySize : KOptS("S"), LsOpt
    object SortByTime : KOptS("t"), LsOpt
    /** do not sort */
    object SortByNothing : KOptS("U"), LsOpt
    /** FIXME_later: what does it mean: natural sort of (version) numbers within text */
    object SortByNatural : KOptS("v"), LsOpt
    object SortByExtension : KOptS("X"), LsOpt

    data class Time(val type: TimeType): KOptL("time", "$type"), LsOpt

    /**
     * There are duplicates:
     * last access time: ATIME, ACCESS, USE
     * last change time: CTIME, STATUS
     * creation time: BIRTH, CREATION
     */
    enum class TimeType { ATIME, ACCESS, USE, CTIME, STATUS, BIRTH, CREATION;
        override fun toString() = super.toString().lowercase()
    }
    object TimeOfAccess : KOptS("u"), LsOpt
    object TimeOfChange : KOptS("c"), LsOpt
    object TimeOfBirth : KOptL("time", "birth"), LsOpt

    data class TimeStyle(val style: String): KOptL("time-style", style), LsOpt

    data class TabSize(val size: Int): KOptL("tabsize", "$size"), LsOpt

    data class Width(val columns: Int): KOptL("width", "$columns"), LsOpt

    object PrintContext : KOptL("context"), LsOpt

    object Help : KOptL("help"), LsOpt
    object Version : KOptL("version"), LsOpt
}
