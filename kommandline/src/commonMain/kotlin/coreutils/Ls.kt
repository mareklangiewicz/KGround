package pl.mareklangiewicz.kommand.coreutils

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.coreutils.LsOpt.*
import pl.mareklangiewicz.kommand.coreutils.LsOpt.ColorType.*
import pl.mareklangiewicz.kommand.coreutils.LsOpt.indicatorStyle.*

fun CliPlatform.lsExec(dir: String, withHidden: Boolean = false, style: indicatorStyle = NONE) =
    ls(dir, withHidden, style).exec()

fun CliPlatform.lsRegFilesExec(dir: String, withHidden: Boolean = false) =
    lsExec(dir, withHidden, SLASH).filter { !it.endsWith('/') }

fun CliPlatform.lsSubDirsExec(dir: String, withHidden: Boolean = false) =
    lsExec(dir, withHidden, SLASH).filter { it.endsWith('/') }.map { it.dropLast(1) }

fun ls(dir: String, withHidden: Boolean = false, style: indicatorStyle = NONE) =
    ls { +dir; -One; -DirsFirst; -Color(NEVER); -Escape; -indicator(style); if (withHidden) -AlmostAll }

fun ls(init: Ls.() -> Unit = {}) = Ls().apply(init)


/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
data class Ls(
    val opts: MutableList<LsOpt> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {
    override val name get() = "ls"
    override val args get() = opts.flatMap { it.args } + files
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

    object long : KOptS("l"), LsOpt
    object longWithoutOwner : KOptS("g"), LsOpt
    object longWithoutGroup : KOptS("o"), LsOpt

    object humanReadable : KOptS("h"), LsOpt
    object humanReadableSI : KOptL("si"), LsOpt

    object dereference : KOptL("dereference"), LsOpt
    object dereferenceCommandLine : KOptL("dereference-command-line"), LsOpt
    object dereferenceCommandLineSymlinkToDir : KOptL("dereference-command-line-symlink-to-dir"), LsOpt

    data class hide(val pattern: String): KOptL("hide", pattern), LsOpt
    /** print ? instead of nongraphic characters */
    object hideControlChars : KOptL("hide-control-chars"), LsOpt
    /** show nongraphic characters as-is (the default, unless program is 'ls' and output is a terminal) */
    object showControlChars : KOptL("show-control-chars"), LsOpt

    data class hyperlink(val type: hyperlinkType): KOptL("hyperlink", "$type"), LsOpt
    enum class hyperlinkType { ALWAYS, AUTO, NEVER;
        override fun toString() = super.toString().lowercase()
    }

    data class indicator(val style: indicatorStyle): KOptL("indicator-style", "$style"), LsOpt
    enum class indicatorStyle { NONE, SLASH, FILETYPE, CLASSIFY;
        override fun toString() = if (this == FILETYPE) "file-type" else super.toString().lowercase()
    }
    object indicatorSlash : KOptS("p"), LsOpt

    object inode : KOptL("inode"), LsOpt

    data class ignore(val pattern: String): KOptL("ignore", pattern), LsOpt

    object kibibytes : KOptL("kibibytes"), LsOpt

    object commas : KOptS("m"), LsOpt

    object numericUidGid : KOptL("numeric-uid-gid"), LsOpt

    object literal : KOptL("literal"), LsOpt

    object quoteName : KOptL("quote-name"), LsOpt

    data class quoting(val style: quotingStyle): KOptL("quoting-style", "$style"), LsOpt
    enum class quotingStyle { LITERAL, LOCALE, SHELL, SHELLALWAYS, SHELLESCAPE, SHELLESCAPEALWAYS, C, ESCAPE;
        override fun toString() = when (this) {
            SHELLALWAYS -> "shell-always"
            SHELLESCAPE -> "shell-escape"
            SHELLESCAPEALWAYS -> "shell-escape-always"
            else -> super.toString().lowercase()
        }
    }

    object reverse : KOptS("r"), LsOpt

    object recursive : KOptS("R"), LsOpt

    object size : KOptS("s"), LsOpt

    data class sort(val type: sortType): KOptL("sort", "$type"), LsOpt
    enum class sortType { NONE, SIZE, TIME, VERSION, EXTENSION;
        override fun toString() = super.toString().lowercase()
    }
    /** largest first */
    object sortBySize : KOptS("S"), LsOpt
    object sortByTime : KOptS("t"), LsOpt
    /** do not sort */
    object sortByNothing : KOptS("U"), LsOpt
    /** FIXME_later: what does it mean: natural sort of (version) numbers within text */
    object sortByNatural : KOptS("v"), LsOpt
    object sortByExtension : KOptS("X"), LsOpt

    data class time(val type: timeType): KOptL("time", "$type"), LsOpt

    /**
     * There are duplicates:
     * last access time: ATIME, ACCESS, USE
     * last change time: CTIME, STATUS
     * creation time: BIRTH, CREATION
     */
    enum class timeType { ATIME, ACCESS, USE, CTIME, STATUS, BIRTH, CREATION;
        override fun toString() = super.toString().lowercase()
    }
    object timeOfAccess : KOptS("u"), LsOpt
    object timeOfChange : KOptS("c"), LsOpt
    object timeOfBirth : KOptL("time", "birth"), LsOpt

    data class timeStyle(val style: String): KOptL("time-style", style), LsOpt

    data class tabSize(val size: Int): KOptL("tabsize", "$size"), LsOpt

    data class width(val columns: Int): KOptL("width", "$columns"), LsOpt

    object printContext : KOptL("context"), LsOpt

    object help : KOptL("help"), LsOpt
    object version : KOptL("version"), LsOpt
}
