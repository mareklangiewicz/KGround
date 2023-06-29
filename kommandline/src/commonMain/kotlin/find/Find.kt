@file:Suppress("unused")

package pl.mareklangiewicz.kommand.find

// TODO_later: add commands: xargs, locate, to the same "find" package (it's all part of "findutils")
// https://www.gnu.org/software/findutils/
// https://savannah.gnu.org/projects/findutils/

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.find.FindExpr.*

// In all shortcut fun here, the first mandatory parameter will always be path.
// It's better to be explicit and just use ".", when needed, instead of relaying on implicit default behavior.

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
    whenFoundPrune: Boolean = false,
    whenFoundFirstQuit: Boolean = false,
) = findTypeBaseName(path, "d", pattern, ignoreCase, whenFoundPrune, whenFoundFirstQuit)

fun findTypeBaseName(
    path: String,
    fileType: String,
    pattern: String,
    ignoreCase: Boolean = false,
    whenFoundPrune: Boolean = false,
    whenFoundFirstQuit: Boolean = false,
) =
    find(path, FileType(fileType), BaseName(pattern, ignoreCase)) {
        check(!whenFoundFirstQuit || !whenFoundPrune)
        !whenFoundPrune && !whenFoundFirstQuit && return@find // it will print anyway as default action
        expr.add(ActPrint)
        expr.add(if (whenFoundFirstQuit) ActQuit else ActPrune)
    }

fun find(path: String, vararg ex: FindExpr, init: Find.() -> Unit = {}) = find {
    +path
    for (e in ex) expr.add(e)
    init()
}

fun find(init: Find.() -> Unit = {}) = Find().apply(init)


/**
 * [gnu findutils](https://www.gnu.org/software/findutils/)
 * [gnu projects findutils](https://savannah.gnu.org/projects/findutils/)
 * [online findutils docs](https://www.gnu.org/software/findutils/manual/html_mono/find.html)
 */
data class Find(
    val opts: MutableList<FindOpt> = mutableListOf(),
    val paths: MutableList<String> = mutableListOf(),
    val expr: MutableList<FindExpr> = mutableListOf(),
) : Kommand {
    override val name get() = "find"
    override val args get() = opts.flatMap { it.args } + paths + expr.flatMap { it.args }
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
    object DayStart: KOptS("daystart"), FindExpr

    /**
     * Deprecated; use the -L option instead.  Dereference symbolic links.  Implies -noleaf.
     * The -follow option affects only those tests which appear after it on the command line.
     */
    @Deprecated("Use SymLinkFollowAlways")
    object Follow: KOptS("follow"), FindExpr

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

    object Help : KOptL("help"), FindExpr
    object Version : KOptL("version"), FindExpr

    /**
     * Process each directory's contents before the directory itself.
     * The "-delete" action also implies "-d"
     * I use the "-d" form because it's more portable, but it works the same as "-depth"
     */
    object DepthFirst: KOptS("d"), FindExpr

    data class DepthMax(val levels: Int): KOptS("maxdepth", levels.toString()), FindExpr
    data class DepthMin(val levels: Int): KOptS("mindepth", levels.toString()), FindExpr

    data class Paths0From(val file: String): KOptS("files0-from"), FindExpr

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
    object NoLeaf: KOptS("noleaf"), FindExpr

    /** Don't descend directories on other filesystems. */
    object XDev: KOptS("mount"), FindExpr
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
    object IsEmpty: KOptS("empty"), FindExpr

    /**
     * Matches files which are executable and directories which are searchable
     * (in a file name resolution sense) by the current user.
     */
    object IsExecutable: KOptS("executable"), FindExpr

    object AlwaysFalse: KOptS("false"), FindExpr
    object AlwaysTrue: KOptS("true"), FindExpr

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
    object NoGroup: KOptS("nogroup"), FindExpr

    /** No user corresponds to file's numeric user ID. */
    object NoUser: KOptS("nouser"), FindExpr


    data class PermAllOf(val mode: String): KOptS("perm", "-$mode"), FindExpr
    data class PermAnyOf(val mode: String): KOptS("perm", "/$mode"), FindExpr
    data class PermExactly(val mode: String): KOptS("perm", mode), FindExpr

    /** Matches files which are readable by the current user. */
    object Readable: KOptS("readable"), FindExpr

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
    object ActDelete: KOptS("delete"), FindExpr

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
    data class ActExecIn(
        val kommand: Kommand,
        val inContainingDir: Boolean = true,
        val askUserFirst: Boolean = false,
    ): FindExpr {
        override val name = (if (askUserFirst)"ok" else "exec") + "dir".iff(inContainingDir)
        override val value get() = error("No one value for ActExecIn. The args list is based on provided kommand")
        override val prefix = "-"
        override val separator = " " // not really used, because the rest is always in separate args
        override val args: List<String>
            get() {
                check(!kommand.args.any { it.startsWith(";") }) {
                    "Find can't correctly execute any kommand with any argument starting with the ';'"
                }
                return listOf("$prefix$name") + kommand.name + kommand.args + ";"
            }
    }

    // TODO_someday_maybe: The "-exec command {} +", and the "-execdir command {} +" variant.


    object ActPrint: KOptS("print"), FindExpr

    object ActPrune: KOptS("prune"), FindExpr

    object ActQuit: KOptS("quit"), FindExpr

    // TODO NOW CONTINUE -fls -fprint...

// endregion Find Expression Category: ACTIONS

}

interface FindOpt: KOpt {

    // Note: Help and Version are inside FindExpr (formally these are expressions for find)

    /** Never follow symbolic links.  This is the default behavior. */
    object SymLinkFollowNever : KOptS("P"), FindOpt
    /** Follow symbolic links. */
    object SymLinkFollowAlways : KOptS("L"), FindOpt
    /** Do not follow symbolic links, except while processing the command line arguments. */
    object SymLinkFollowCmdArg : KOptS("H"), FindOpt

    data class Debug(val dopts: List<String>): KOptS("D", dopts.joinToString(",")), FindOpt {
        constructor(vararg o: String): this(o.toList())
        init { check(dopts.all { it.all { it.isLetter() } }) }
    }

    data class Optimisation(val level: Int): KOptS("O", level.toString(), separator = ""), FindOpt {
        init { check(level in 0..3) }
    }
}
