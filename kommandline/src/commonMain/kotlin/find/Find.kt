@file:Suppress("unused", "UNUSED_PARAMETER")

package pl.mareklangiewicz.kommand.find

// TODO_later: add commands: xargs, locate, to the same "find" package (it's all part of "findutils")
// https://www.gnu.org/software/findutils/
// https://savannah.gnu.org/projects/findutils/

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.FindExpr.*


/**
 * It's best to find the format in console: "man find" -> "/-printf format", but there are also online docs:
 * [online docs](https://www.gnu.org/software/findutils/manual/html_mono/find.html#Print-File-Information)
 * TODO_someday: actual types/structures representing print format.
 */
typealias FindPrintFormat = String
typealias FindColumnName = String
typealias FindDetailsDef = Collection<Pair<FindColumnName, FindPrintFormat>>


// In all shortcut fun here, the first mandatory parameter will always be path.
// It's better to be explicit and just use ".", when needed, instead of relaying on implicit default behavior.

@OptIn(DelicateKommandApi::class)
fun findDetailsTable(
    path: String,
    vararg useNamedArgs: Unit,
    details: FindDetailsDef,
    baseNamePattern: String = "*",
    ignoreCase: Boolean = false,
) = find(
    path = path,
    baseNamePattern = baseNamePattern,
    ignoreCase = ignoreCase,
    whenFoundPrintF = details.detailsPrintFormat()
).typed {
    map { details.detailsParseLine(it) }
}

private fun FindDetailsDef.detailsPrintFormat(): FindPrintFormat =
    joinToString("\\0\\0", postfix = "\\0\\n") { it.second }

private fun FindDetailsDef.detailsParseLine(line: String): List<String> {
    check(line.endsWith("\u0000")) { "Looks like there was some file with forbidden character (line break)" }
    // Not actually forbidden in unix but it's weird and dangerous to have multiline file names, so better fail fast.
    val list = line.removeSuffix("\u0000").split("\u0000\u0000")
    check(list.size == size) { "Wrong number of columns found: ${list.size} (expected: ${size})" }
    return list
}

// TODO: decide on some good typical details - headers names and formats
//  (need clear unambiguous data useful for postprocessing in dataframes, for charts, etc)
private val typicalDetails: FindDetailsDef = listOf(
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

fun findTypicalDetailsTable(path: String) =
    findDetailsTable(path, details = typicalDetails)

fun findTypicalDetailsTableToList(path: String) =
    findDetailsTable(path, details = typicalDetails).reduced { stdout.toList() }


/**
 * Most typical find invocation with default param values.
 * @param useNamedArgs requires named args if nondefault used; avoids name clash with base / low level find fun.
 * @param whenFoundPrintF null means using ActPrint, non-null means ActPrintF(whenFoundPrintF!!)
 */
fun find(
    path: String,
    vararg useNamedArgs: Unit,
    fileType: String = "f",
    baseNamePattern: String = "*",
    ignoreCase: Boolean = false,
    whenFoundPrintF: FindPrintFormat? = null,
    whenFoundPrune: Boolean = false,
    whenFoundFirstQuit: Boolean = false,
) = findTypeBaseName(
    path, fileType, baseNamePattern, ignoreCase, whenFoundPrintF, whenFoundPrune, whenFoundFirstQuit
)

fun findWholeName(path: String, pattern: String, ignoreCase: Boolean = false) =
    find(path, WholeName(pattern, ignoreCase))

fun findBaseName(path: String, pattern: String, ignoreCase: Boolean = false) =
    find(path, BaseName(pattern, ignoreCase))

fun findRegularBaseName(path: String, pattern: String, ignoreCase: Boolean = false) =
    findTypeBaseName(path, "f", pattern, ignoreCase)

fun findDirBaseName(
    path: String,
    pattern: String,
    ignoreCase: Boolean = false,
    whenFoundPrintF: FindPrintFormat? = null,
    whenFoundPrune: Boolean = false,
    whenFoundFirstQuit: Boolean = false,
) = findTypeBaseName(path, "d", pattern, ignoreCase, whenFoundPrintF, whenFoundPrune, whenFoundFirstQuit)

@OptIn(DelicateKommandApi::class)
fun findTypeBaseName(
    path: String,
    fileType: String,
    pattern: String,
    ignoreCase: Boolean = false,
    whenFoundPrintF: FindPrintFormat? = null,
    whenFoundPrune: Boolean = false,
    whenFoundFirstQuit: Boolean = false,
) =
    find(path, BaseName(pattern, ignoreCase), FileType(fileType)) {
        // BaseName is first, before FileType, as optimisation to avoid having to call stat(2) on every filename
        when {
            whenFoundFirstQuit && whenFoundPrune -> error("Can't quit and also prune")
            whenFoundPrintF != null -> expr.add(ActPrintF(whenFoundPrintF))
            whenFoundFirstQuit || whenFoundPrune -> expr.add(ActPrint)
            // or else find will perform default printing
        }
        when {
            whenFoundFirstQuit -> expr.add(ActQuit)
            whenFoundPrune -> expr.add(ActPrune)
        }
    }

@DelicateKommandApi
fun find(path: String, vararg ex: FindExpr, init: Find.() -> Unit = {}) = find {
    +path
    for (e in ex) expr.add(e)
    init()
}

@DelicateKommandApi
fun find(init: Find.() -> Unit = {}) = Find().apply(init)


/**
 * [gnu findutils](https://www.gnu.org/software/findutils/)
 * [gnu projects findutils](https://savannah.gnu.org/projects/findutils/)
 * [online findutils docs](https://www.gnu.org/software/findutils/manual/html_mono/find.html)
 */
@DelicateKommandApi
data class Find(
    val opts: MutableList<FindOpt> = mutableListOf(),
    val paths: MutableList<String> = mutableListOf(),
    val expr: MutableList<FindExpr> = mutableListOf(),
) : Kommand {
    override val name get() = "find"
    override val args get() = opts.toArgsFlat() + paths + expr.toArgsFlat()
    operator fun FindOpt.unaryMinus() = opts.add(this)
    operator fun String.unaryPlus() = paths.add(this)
}

private fun String.iff(condition: Boolean) = if (condition) this else ""

interface FindExpr: KOpt {

// region Find Expression Category: POSITIONAL OPTIONS

    /**
     * Measure times (for -amin, -atime, -cmin, -ctime, -mmin, and -mtime) from
     * the beginning of today rather than from 24 hours ago.
     */
    data object DayStart: KOptS("daystart"), FindExpr

    /**
     * Deprecated; use the -L option instead.  Dereference symbolic links.  Implies -noleaf.
     * The -follow option affects only those tests which appear after it on the command line.
     */
    @Deprecated("Use SymLinkFollowAlways")
    data object Follow: KOptS("follow"), FindExpr

    /**
     * Changes the regular expression syntax understood by "-regex" and "-iregex" tests
     * which occur later on the command line.
     * To see which regular expression types are known, use RegExType("help").
     */
    data class RegExType(val type: String): KOptS("regextype"), FindExpr

    /**
     * Turn warning messages on or off.  These warnings apply only to the command line usage,
     * not to any conditions that find might encounter when it searches directories.
     * The default behavior corresponds to "-warn" if standard input is a tty, and to "-nowarn" otherwise.
     */
    data class Warn(val enabled: Boolean = true): KOptS("no".iff(!enabled) + "warn"), FindExpr


// endregion Find Expression Category: POSITIONAL OPTIONS

// region Find Expression Category: GLOBAL OPTIONS

    data object Help : KOptL("help"), FindExpr
    data object Version : KOptL("version"), FindExpr

    /**
     * Process each directory's contents before the directory itself.
     * The "-delete" action also implies "-depth"
     * I use the "-depth" form by default because it's recommended on my system (man find) and POSIX compliant,
     * but "-d" should also do the same and additionally is supported on FreeBSD, NetBSD, Mac OS X and OpenBSD.
     */
    data class DepthFirst(val posix: Boolean = true): KOptS("d" + "epth".iff(posix)), FindExpr

    data class DepthMax(val levels: Int): KOptS("maxdepth", levels.toString()), FindExpr
    data class DepthMin(val levels: Int): KOptS("mindepth", levels.toString()), FindExpr

    /**
     * @param file default "-" is stdin
     */
    data class Paths0From(val file: String = "-"): KOptS("files0-from"), FindExpr

    /**
     * Normally, find will emit an error message when it fails to stat a file.
     * If you set ReadDirRace(ignore = true), and a file is deleted
     * between the time find reads the name of the file from the directory
     * and the time it tries to stat the file,
     * then no error message will be issued.
     */
    data class ReadDirRace(val ignore: Boolean = false):
        KOptS("no".iff(!ignore) + "ignore_readdir_race"), FindExpr

    /**
     * Do not optimize by assuming that directories contain 2 fewer subdirectories than
     * their hard link count. This option is needed when searching filesystems
     * that do not follow the Unix directory link convention,
     * such as CD-ROM or MS-DOS filesystems or AFS volume mount points.
     */
    data object NoLeaf: KOptS("noleaf"), FindExpr

    /** Don't descend directories on other filesystems. */
    data object XDev: KOptS("mount"), FindExpr
    // I use "-mount" instead of "-xdev" because it's more portable (it does the same thing)

// endregion Find Expression Category: GLOBAL OPTIONS

// region Find Expression Category: TESTS

    sealed class NumArg(val n: Int) {
        init { check(n >= 0) }
        abstract val arg: String
        override fun toString() = arg
        class Exactly(n: Int): NumArg(n) { override val arg get() = "$n" }
        class LessThan(n: Int): NumArg(n) { override val arg get() = "-$n" }
        class MoreThan(n: Int): NumArg(n) { override val arg get() = "+$n" }
    }

    /** The file was last accessed less than, more than or exactly n minutes ago. */
    data class AccessMinutes(val minutes: NumArg): KOptS("amin", minutes.arg), FindExpr

    data class AccessNewerThanModifOf(val referenceFile: String):
        KOptS("anewer", referenceFile), FindExpr

    /** The file was last accessed less than, more than or exactly n*24 hours ago. */
    data class AccessTime24h(val time24h: NumArg): KOptS("atime", time24h.arg), FindExpr

    /** The file status was last changed less than, more than or exactly n minutes ago. */
    data class ChangeMinutes(val minutes: NumArg): KOptS("cmin", minutes.arg), FindExpr

    data class ChangeNewerThanModifOf(val referenceFile: String):
        KOptS("cnewer", referenceFile), FindExpr

    /** The file status was changed less than, more than or exactly n*24 hours ago. */
    data class ChangeTime24h(val time24h: NumArg): KOptS("ctime", time24h.arg), FindExpr

    /** The file data was last modified less than, more than or exactly n minutes ago. */
    data class ModifMinutes(val minutes: NumArg): KOptS("mmin", minutes.arg), FindExpr

    data class ModifNewerThanModifOf(val referenceFile: String):
        KOptS("newer", referenceFile), FindExpr

    /** The file data was last modified less than, more than or exactly n*24 hours ago. */
    data class ModifTime24h(val time24h: NumArg): KOptS("mtime", time24h.arg), FindExpr

    /**
     * Succeeds if timestamp X of the file being considered is newer than timestamp Y
     * of the file reference. The letters X and Y can be any of the following letters:
     * a - The access time of the file reference
     * B - The birth time of the file reference
     * c - The inode status change time of reference
     * m - The modification time of the file reference
     * t - reference is interpreted directly as a time
     */
    data class TimeXNewerThanYOf(val xy: String, val referenceFile: String):
        KOptS("newer$xy", referenceFile), FindExpr
    // TODO_someday: better typed structure for -newerXY;
    // and generally more structured data classes for all time related tests
    // (enum for type of timestamp and collapse all those tests above to less cases with more params)


    /** The file is empty and is either a regular file or a directory. */
    data object IsEmpty: KOptS("empty"), FindExpr

    /**
     * Matches files which are executable and directories which are searchable
     * (in a file name resolution sense) by the current user.
     */
    data object IsExecutable: KOptS("executable"), FindExpr

    data object AlwaysFalse: KOptS("false"), FindExpr
    data object AlwaysTrue: KOptS("true"), FindExpr

    data class OnFileSystemType(val type: String): KOptS("fstype", type), FindExpr

    /** File's numeric group ID is less than, more than or exactly n. */
    data class GroupID(val n: NumArg): KOptS("gid", n.arg), FindExpr

    /** The file belongs to group gname (numeric group ID allowed). */
    data class GroupName(val gname: String): KOptS("group", gname), FindExpr

    /**
     * The file has inode number smaller than, greater than or exactly n.
     * It is normally easier to use the -samefile test instead.
     */
    data class INodeNum(val num: NumArg): KOptS("inum", num.arg), FindExpr

    /** The file has less than, more than or exactly n hard links. */
    data class HardLinksNum(val num: NumArg): KOptS("links", num.arg), FindExpr



    /** The file is a symbolic link whose contents match a given shell pattern. */
    data class SymLinkTo(val pattern: String, val ignoreCase: Boolean = false):
        KOptS("i".iff(ignoreCase) + "lname", pattern), FindExpr

    /** Base of file name (leading directories removed) matches the given shell pattern */
    data class BaseName(val pattern: String, val ignoreCase: Boolean = false):
        KOptS("i".iff(ignoreCase) + "name", pattern), FindExpr

    /**
     * The file name matches the given shell pattern.
     * Note that the pattern match test applies to the whole file name,
     * starting from one of the start points named on the command line.
     */
    data class WholeName(val pattern: String, val ignoreCase: Boolean = false):
        KOptS("i".iff(ignoreCase) + "path", pattern), FindExpr
        // I use the "-path" notation which is more portable than "-wholename", but does the same thing.

    /**
     * The file name matches a regular expression pattern.
     * This is a match on the whole path, not a search.
     */
    data class WholeNameRegEx(val regex: String, val ignoreCase: Boolean = false):
        KOptS("i".iff(ignoreCase) + "regex", regex), FindExpr

    /** No group corresponds to file's numeric group ID. */
    data object NoGroup: KOptS("nogroup"), FindExpr

    /** No user corresponds to file's numeric user ID. */
    data object NoUser: KOptS("nouser"), FindExpr


    data class PermAllOf(val mode: String): KOptS("perm", "-$mode"), FindExpr
    data class PermAnyOf(val mode: String): KOptS("perm", "/$mode"), FindExpr
    data class PermExactly(val mode: String): KOptS("perm", mode), FindExpr

    /** Matches files which are readable by the current user. */
    data object Readable: KOptS("readable"), FindExpr

    /** The file refers to the same inode as name. */
    data class SameFileAs(val referenceFile: String): KOptS("samefile", referenceFile), FindExpr

    /**
     * The file uses less than, more than or exactly n units of space, rounding up.
     * The following suffixes can be used:
     * b - for 512-byte blocks (this is the default if no suffix is used)
     * c - for bytes
     * w - for two-byte words
     * k - for kibibytes (KiB, units of 1024 bytes)
     * M - for mebibytes (MiB, units of 1024 * 1024 = 1048576 bytes)
     * G - for gibibytes (GiB, units of 1024 * 1024 * 1024 = 1073741824 bytes)
     */
    data class FileSize(val n: NumArg, val unit: Char): KOptS("size", "${n.arg}$unit"), FindExpr

    /**
     * Matches files of a specified type.
     * Note: it always calls system stat(2), which can be a bit expensive,
     * so usually it's faster to filter by name first ([BaseName]/[WholeName]).
     *
     * @param type one of:
     * - b - block (buffered) special
     * - c - character (unbuffered) special
     * - d - directory
     * - p - named pipe (FIFO)
     * - f - regular file
     * - l - symbolic link
     * - s - socket
     * - D - door (Solaris)
     * - Additionally on GNU systems: To search for more than one type at once,
     * you can supply the combined list of type letters separated by a comma.
     *
     * @param x When true: Change to alternative symbolic links matching strategy.
     * See "man find" for some crazy details.
     */
    data class FileType(val type: String, val x: Boolean = false): KOptS("x".iff(x) + "type", type), FindExpr

    /** (SELinux only) Security context of the file matches the glob pattern. */
    data class SEContext(val pattern: String): KOptS("context", pattern), FindExpr

// endregion Find Expression Category: TESTS

// region Find Expression Category: ACTIONS

    /**
     * Delete files or directories; true if removal succeeded. If the removal failed, an error message is issued
     * and find's exit status will be nonzero (when it eventually exits).
     * WARNING: Don't forget that find evaluates the command line as an expression, so putting ActDelete first
     * will make find try to delete everything below the starting points you specified.
     * The use of the ActDelete action on the command line automatically turns on the DepthFirst option.
     * As in turn, DepthFirst makes ActPrune ineffective, the ActDelete cannot usefully be combined with ActPrune.
     * Often, the user might want to test a find command line with ActPrint prior to adding ActDelete
     * for the actual removal run. To avoid surprising results, it is usually best to remember
     * to use DepthFirst explicitly during those earlier test runs.
     * The ActDelete will fail to remove a directory unless it is empty.
     * Together with the ReadDirRace(ignore = true), find will ignore errors of the ActDelete
     * in case the file has disappeared since the parent directory was read:
     * it will not output an error diagnostic, not change the exit code to nonzero,
     * and the return code of the ActDelete will be true.
     */
    data object ActDelete: KOptS("delete"), FindExpr

    /**
     * Execute kommand; true if 0 status is returned. Special arg ";" is used after all kommand args,
     * so no kommand arg can start with ";" Any kommand argument containing string `{}' is replaced
     * by the current file name being processed everywhere it occurs in the arguments to the kommand,
     * not just in arguments where it is alone, as in some versions of find. The specified kommand
     * is run once for each matched file.
     * @param inContainingDir
     * * true: The kommand is executed in dir containing matched file. (safer, especially regarding race conditions)
     * * false: The kommand is executed in the starting directory. (unavoidable security problems - see "man find")
     */
    data class ActExec(
        val kommand: Kommand,
        val inContainingDir: Boolean = true,
        val askUserFirst: Boolean = false,
    ): KOptS(name = (if (askUserFirst)"ok" else "exec") + "dir".iff(inContainingDir)), FindExpr {
        override fun toArgs(): List<String> {
            val kta = kommand.toArgs()
            check(!kta.any { it.startsWith(";") }) {
                "Find can't correctly execute any kommand with any argument starting with the ';'"
            }
            return listOf("-$name") + kta + ";"
        }
    }

    data object ActPrint: KOptS("print"), FindExpr

    data class ActPrintF(val format: FindPrintFormat): KOptS("printf", format), FindExpr
    // TODO_someday:
    //  One ActPrint with flags deciding which version of -(f)print(0/f) to use and optional format and/or file
    //  Also ActLs for -(f)ls

    data object ActPrune: KOptS("prune"), FindExpr

    data object ActQuit: KOptS("quit"), FindExpr

// endregion Find Expression Category: ACTIONS

// region Find Expression Category: OPERATORS

    data class OpParent(val children: List<FindExpr>): FindExpr {
        constructor(vararg ex: FindExpr): this(ex.toList())
        override fun toArgs(): List<String> = listOf("(") + children.flatMap { it.toArgs() } + ")"
    }

    data object OpAnd: KOptS("a"), FindExpr
    data object OpOr: KOptS("o"), FindExpr
    data object OpNot: FindExpr { override fun toArgs() = listOf("!") }
    data object OpComma: FindExpr { override fun toArgs() = listOf(",") }

    // FIXME_someday: rethink all grouping expressions and precedence,
    // so I don't need so many OpParent to make it always correct.
    // It would be better for FindExpr to always represent ONE expression, and not arbitrary list.
    // (then make Find hold ONE FindExpr expr instead of list)
    // (also make OpXXX stuff more private and expose only koltliny operator funs)
    operator fun FindExpr.not() = OpParent(OpNot, OpParent(this))
    infix fun FindExpr.and(other: FindExpr) = OpParent(OpParent(this), OpAnd, OpParent(other))
    infix fun FindExpr.or(other: FindExpr) = OpParent(OpParent(this), OpOr, OpParent(other))
// endregion Find Expression Category: OPERATORS

}


interface FindOpt: KOpt {

    // Note: Help and Version are inside FindExpr (formally these are expressions for find)

    /** Never follow symbolic links.  This is the default behavior. */
    data object SymLinkFollowNever : KOptS("P"), FindOpt
    /** Follow symbolic links. */
    data object SymLinkFollowAlways : KOptS("L"), FindOpt
    /** Do not follow symbolic links, except while processing the command line arguments. */
    data object SymLinkFollowCmdArg : KOptS("H"), FindOpt

    data class Debug(val dopts: List<String>): KOptS("D", dopts.joinToString(",")), FindOpt {
        constructor(vararg o: String): this(o.toList())
        init { check(dopts.all { it.all { it.isLetter() } }) }
    }

    data class Optimisation(val level: Int): KOptS("O", level.toString(), nameSeparator = ""), FindOpt {
        init { check(level in 0..3) }
    }
}
