package pl.mareklangiewicz.kommand.coreutils

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.coreutils.Ls.Option.*
import pl.mareklangiewicz.kommand.coreutils.Ls.Option.colorType.*
import pl.mareklangiewicz.kommand.coreutils.Ls.Option.indicatorStyle.*

fun CliPlatform.lsExec(dir: String, withHidden: Boolean = false, style: indicatorStyle = NONE) =
    ls { +dir; -one; -dirsFirst; -color(NEVER); -escape; -indicator(style); if (withHidden) -almostAll }.exec()

fun CliPlatform.lsRegFiles(dir: String, withHidden: Boolean = false) =
    lsExec(dir, withHidden, SLASH).filter { !it.endsWith('/') }
fun CliPlatform.lsSubDirs(dir: String, withHidden: Boolean = false) =
    lsExec(dir, withHidden, SLASH).filter { it.endsWith('/') }.map { it.dropLast(1) }

// TODO NOW: add versions of above without exec + samples

fun ls(init: Ls.() -> Unit = {}) = Ls().apply(init)
/** [linux man](https://man7.org/linux/man-pages/man1/ls.1.html) */
data class Ls(
    val options: MutableList<Option> = mutableListOf(),
    val files: MutableList<String> = mutableListOf()
) : Kommand {

    override val name get() = "ls"
    override val args get() = options.map { it.str } + files

    sealed class Option(val str: String) {

        /** List one file per line.  You can avoid '\n' by adding options: hideControlChars or escape */
        object one: Option("-1")

        /** do not ignore entries starting with . */
        object all : Option("-a")
        /** do not list implied . and .. */
        object almostAll : Option("-A")

        object author : Option("--author")

        object escape : Option("--escape")

        data class blockSize(val size: String): Option("--block-size=$size")

        object ignoreBackups : Option("--ignore-backups")

        object listByColumns : Option("-C")
        object listByLines : Option("-x")

        data class color(val type: colorType): Option("--color=$type")
        enum class colorType { ALWAYS, AUTO, NEVER;
            override fun toString() = super.toString().lowercase()
        }

        object directory : Option("-d")

        object dired : Option("--dired")

        /** do not sort, enable -aU, disable -ls --color */
        object raw : Option("-f")

        /** append indicator (one of *=/>@|) to entries */
        object classify : Option("-F")
        /** like classify, except do not add * symbol */
        object classifyFileType : Option("--file-type")

        data class format(val type: formatType): Option("--format=$type")
        enum class formatType { ACROSS, COMMAS, HORIZONTAL, LONG, SINGLECOLUMN, VERBOSE, VERTICAL;
            override fun toString() = super.toString().lowercase()
        }

        /** like -l --time-style=full-iso */
        object fullTime : Option("--full-time")

        object dirsFirst : Option("--group-directories-first")

        object noGroup : Option("-G")

        object long : Option("-l")
        object longWithoutOwner : Option("-g")
        object longWithoutGroup : Option("-o")

        object humanReadable : Option("-h")
        object humanReadableSI : Option("--si")

        object dereference : Option("--dereference")
        object dereferenceCommandLine : Option("--dereference-command-line")
        object dereferenceCommandLineSymlinkToDir : Option("--dereference-command-line-symlink-to-dir")

        data class hide(val pattern: String): Option("--hide=$pattern")
        /** print ? instead of nongraphic characters */
        object hideControlChars : Option("--hide-control-chars")
        /** show nongraphic characters as-is (the default, unless program is 'ls' and output is a terminal) */
        object showControlChars : Option("--show-control-chars")

        data class hyperlink(val type: hyperlinkType): Option("--hyperlink=$type")
        enum class hyperlinkType { ALWAYS, AUTO, NEVER;
            override fun toString() = super.toString().lowercase()
        }

        data class indicator(val style: indicatorStyle): Option("--indicator-style=$style")
        enum class indicatorStyle { NONE, SLASH, FILETYPE, CLASSIFY;
            override fun toString() = if (this == FILETYPE) "file-type" else super.toString().lowercase()
        }
        object indicatorSlash : Option("-p")

        object inode : Option("--inode")

        data class ignore(val pattern: String): Option("--ignore=$pattern")

        object kibibytes : Option("--kibibytes")

        object commas : Option("-m")

        object numericUidGid : Option("--numeric-uid-gid")

        object literal : Option("--literal")

        object quoteName : Option("--quote-name")

        data class quoting(val style: quotingStyle): Option("--quoting-style=$style")
        enum class quotingStyle { LITERAL, LOCALE, SHELL, SHELLALWAYS, SHELLESCAPE, SHELLESCAPEALWAYS, C, ESCAPE;
            override fun toString() = when (this) {
                SHELLALWAYS -> "shell-always"
                SHELLESCAPE -> "shell-escape"
                SHELLESCAPEALWAYS -> "shell-escape-always"
                else -> super.toString().lowercase()
            }
        }

        object reverse : Option("-r")

        object recursive : Option("-R")

        object size : Option("-s")

        data class sort(val type: sortType): Option("--sort=$type")
        enum class sortType { NONE, SIZE, TIME, VERSION, EXTENSION;
            override fun toString() = super.toString().lowercase()
        }
        /** largest first */
        object sortBySize : Option("-S")
        object sortByTime : Option("-t")
        /** do not sort */
        object sortByNothing : Option("-U")
        /** FIXME_later: what does it mean: natural sort of (version) numbers within text */
        object sortByNatural : Option("-v")
        object sortByExtension : Option("-X")

        data class time(val type: timeType): Option("--time=$type")

        /**
         * There are duplicates:
         * last access time: ATIME, ACCESS, USE
         * last change time: CTIME, STATUS
         * creation time: BIRTH, CREATION
         */
        enum class timeType { ATIME, ACCESS, USE, CTIME, STATUS, BIRTH, CREATION;
            override fun toString() = super.toString().lowercase()
        }
        object timeOfAccess : Option("-u")
        object timeOfChange : Option("-c")
        object timeOfBirth : Option("--time=birth")

        data class timeStyle(val style: String): Option("--time-style=$style")

        data class tabSize(val size: Int): Option("--tabsize=$size")

        data class width(val columns: Int): Option("--width=$columns")

        object printContext : Option("--context")

        object help : Option("--help")
        object version : Option("--version")
    }

    operator fun String.unaryPlus() = files.add(this)

    operator fun Option.unaryMinus() = options.add(this)
}
