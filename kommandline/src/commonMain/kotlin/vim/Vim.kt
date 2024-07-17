package pl.mareklangiewicz.kommand.vim

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*

@DelicateApi("When opening stdin content, vim expects commands from (redirected) stderr!")
fun vimOpenStdInContent(init: XVim.() -> Unit = {}) = vim("-", init = init)
fun vimStdIn(init: XVim.() -> Unit = {}) = vim("-", init = init)

fun gvimStdIn(init: XVim.() -> Unit = {}) = gvim("-", init = init)

fun gvimOpenStdInContent(init: XVim.() -> Unit = {}) = gvim("-", init = init)

fun gvimStdIn(inLineS: Flow<String>, init: XVim.() -> Unit = {}) = ReducedScript {
  gvimStdIn(init).ax(inLineS = inLineS)
}

fun gvimStdIn(inLines: List<String>, init: XVim.() -> Unit = {}) = gvimStdIn(inLineS = inLines.asFlow(), init)

fun gvimStdIn(inContent: String, init: XVim.() -> Unit = {}) = gvimStdIn(inLineS = inContent.lineSequence().asFlow(), init)


fun vim(vararg files: String, init: XVim.() -> Unit = {}) = XVim(XVim.Type.vim, files.toMutableList()).apply(init)

fun nvim(vararg files: String, init: XVim.() -> Unit = {}) = XVim(XVim.Type.nvim, files.toMutableList()).apply(init)

fun gvim(vararg files: String, init: XVim.() -> Unit = {}) = XVim(XVim.Type.gvim, files.toMutableList()).apply(init)


@Suppress("unused")
data class XVim(
  val type: Type = Type.vim,
  val files: MutableList<String> = mutableListOf(),
  val options: MutableList<Option> = mutableListOf(),
) : Kommand {

  /** Type of vim binary. Names are actual binary file/kommand names. */
  @Suppress("EnumEntryName")
  enum class Type {

    /** Neo Vim */
    nvim,

    /** Base/official/original Vim. Started in normal way. */
    vim,

    /**
     * Graphical mode of [vim]. Starts new GUI window. Can also be done with "-g" argument [Option.Gui]
     * Note1: it does NOT use [nvim] - which doesn't have official gui support.
     * Note2: It's usually better to start [nvim] inside kitty terminal - better nerd fonts support and everything.
     */
    gvim,


    /** The [vim], but might be acting more like Vi (But it depends on .vimrc and the 'compatible' option) */
    vi,

    /**
     * The [vim], but started in traditional vi-compatible Ex mode. Go to Normal mode with the ":vi" command.
     * Can also be done with the "-e" argument [Option.ExMode]. See also [Type.exim]
     */
    ex,

    /**
     * The [vim], but started in improved Ex mode. Go to Normal mode with the ":vi" command.
     * Allows for more advanced commands than the vi-compatible Ex mode, and behaves more like typing :commands in Vim.
     * Can also be done with the "-E" argument [Option.EximMode]. See also [Type.ex]
     */
    exim,

    /**
     * The [vim], but started in read-only mode. You will be protected from writing the files.
     * Can also be done with the "-R" argument [Option.ReadOnly].
     */
    view,

    /** Graphical mode of [view], so also read-only. */
    gview,

    /**
     * Easy mode of [gvim]. Graphical, starts new window, behave like click-and-type editor.
     * Can also be done with the "-y" argument [Option.EasyMode].
     */
    evim,

    /**
     * Easy mode of [gview]. Graphical, starts new window, behave like click-and-type editor, read-only
     * Can also be done with the "-y" argument [Option.EasyMode].
     */
    eview,

    /** [vim] with restrictions. Can also be done with the "-Z" argument [Option.RestrictedMode]. */
    rvim,

    /** [view] with restrictions. Can also be done with the "-Z" argument [Option.RestrictedMode]. */
    rview,

    /** [gvim] with restrictions. Can also be done with the "-Z" argument [Option.RestrictedMode]. */
    rgvim,

    /** [gview] with restrictions. Can also be done with the "-Z" argument [Option.RestrictedMode]. */
    rgview,

    /** Start [vim] in diff mode. Can also be done with the "-d" argument [Option.Diff]. */
    vimdiff,

    /** Start [gvim] in diff mode. Can also be done with the "-d" argument [Option.Diff]. */
    gvimdiff,
  }

  override val name get() = type.name
  override val args get() = options.flatMap { it.str } + files

  sealed class Option(val name: String, val arg: String? = null) {

    // important: name and arg has to be separate in XVim.args - for Kommand.ax to work correctly
    val str get() = listOf(name) plusIfNN arg

    /** For the first file the cursor will be positioned on given line. Lines are numbered from 1 */
    data class CursorLine(val line: Int) : Option("+$line")
    data object CursorLineLast : Option("+")

    /**
     * For the first file the cursor will be positioned in the line with the first occurrence of pattern.
     * see also :help search-pattern
     */
    data class CursorLineFind(val pattern: String) : Option("+/$pattern")


    /** Ex [cmd] will be executed after the first file has been read. Can be used up to 10 times. */
    data class ExCmd(val cmd: String) : Option("-c", cmd)

    /** Ex [cmd] will be executed BEFORE processing any vimrc file. Can be used up to 10 times. */
    data class ExCmdBeforeRc(val cmd: String) : Option("--cmd", cmd)

    /**
     * The [sourcedFile] will be sourced after the first file has been read.
     * This is equivalent to ExCmd("source [sourcedFile]").
     * The [sourcedFile] cannot start with '-'.
     * If null, then "Session.vim" is used (only works when -S is the last argument).
     */
    data class Source(val sourcedFile: String? = null) : Option("-S", sourcedFile)

    /** Binary mode.  A few options will be set that makes it possible to edit a binary or executable file. */
    data object Binary : Option("-b")

    /** Set the 'compatible' option.  This will make Vim behave mostly like Vi, even though a .vimrc file exists. */
    data object Compatible : Option("-C")

    /**
     * No-compatible mode. Resets the 'compatible' option. This will make Vim behave a bit better,
     * but less Vi compatible, even though a .vimrc file does not exist.
     */
    data object NoCompatible : Option("-N")

    /**
     * Start in diff mode. There should between two to eight file name arguments.
     * Vim will open all the files and show differences between them. Works like vimdiff
     */
    data object Diff : Option("-d")

    /** Go to debugging mode when executing the first command from a script. */
    data object Debug : Option("-D")

    /** Traditional vi-compatible Ex mode. Just like executable was called "ex" [Type.ex]. See also [EximMode] */
    data object ExMode : Option("-e")

    /**
     * Improved Ex mode, just like the executable was called "exim" [Type.exim]. See also [ExMode].
     * Allows for more advanced commands than the vi-compatible Ex mode, and behaves more like typing :commands in Vim.
     */
    data object EximMode : Option("-E")

    /**
     * Start in vi-compatible mode, just like the executable was called "vi" [Type.vi].
     * This only has effect when the executable is called "ex" [Type.ex].
     */
    data object ViMode : Option("-v")

    /**
     * Start Vim in easy mode, just like the executable was called "evim" [Type.evim] or "eview" [Type.eview].
     * Makes Vim behave like a click-and-type editor.
     */
    data object EasyMode : Option("-y")

    data object RestrictedMode : Option("-Z")

    /**
     * Do not use any personal configuration (vimrc, plugins, etc.).
     * Useful to see if a problem reproduces with a clean Vim setup.
     */
    data object CleanMode : Option("--clean")

    /** Skip loading plugins. Implied by -u NONE [VimRc]. */
    data object NoPluginMode : Option("--noplugin")

    /**
     * Foreground. For the GUI version, Vim will NOT fork and detach from the shell it was started in.
     * Should be used when Vim is executed by a program that should wait for the edit session to finish.
     */
    data object Foreground : Option("-f")


    /**
     * The GUI mode. Supported only in original [vim] and only if compiled with GUI support. Like [Type.gvim]
     * Note: the [Type.nvim] does NOT support it, but better solution is to run [Type.nvim] inside kitty terminal.
     */
    data object Gui : Option("-g")

    data object Help : Option("-h")

    /**
     * Specifies  the filename to use when reading or writing the viminfo file, instead of the default "~/.viminfo".
     * This can also be used to skip the use of the .viminfo file, by giving the name "NONE".
     */
    data class VimInfo(val vimInfoFile: String) : Option("-i", vimInfoFile)

    /**
     * Use the commands in the [vimRcFile] for initializations.
     * All the other initializations are skipped.
     * Use this to edit a special kind of files.
     * It can also be used to skip all initializations by giving the name "NONE".
     * See ":help initialization" within vim for more details.
     */
    data class VimRc(val vimRcFile: String) : Option("-u", vimRcFile)

    /**
     * Use  the commands in the [gvimRcFile] for GUI initializations.
     * All the other GUI initializations are skipped.
     * It can also be used to skip all GUI initializations by giving the name "NONE".
     * See ":help gui-init" within vim for more details.
     */
    data class GVimRc(val gvimRcFile: String) : Option("-U", gvimRcFile)

    /**
     * Modifying files is disabled. Resets the 'write' option.
     * You can still modify the buffer, but writing a file is not possible.
     */
    data object NoWrite : Option("-m")

    /**
     * The 'modifiable' and 'write' options will be unset, so that changes are not allowed
     * and files can not be written. Note that these options can be set to enable making modifications,
     * but writing a file is not possible.
     */
    data object NoWriteOrModify : Option("-M")

    /**
     * No swap file will be used. Recovery after a crash will be impossible.
     * Handy if you want to edit a file on a very slow medium (e.g. floppy).
     * Can also be done with ":set uc=0". Can be undone with ":set uc=200".
     */
    data object NoSwap : Option("-n")

    /**
     * Don't connect to the X server. Shortens startup time in a terminal,
     * but the window title and clipboard will not be used.
     */
    data object NoXServer : Option("-X")

    /** Become an editor server for NetBeans.  See the docs for details. */
    data object NetBeansServer : Option("-nb")


    /** Open [nr] windows stacked. null means one window for each file. */
    data class WindowsStacked(val nr: Int? = null) : Option("-o$nr")

    /** Open [nr] windows side by side. null means one window for each file. */
    data class WindowsSideBySide(val nr: Int? = null) : Option("-O$nr")

    /** Open [nr] tab pages. null means one tab for each file. */
    data class Tabs(val nr: Int? = null) : Option("-o$nr")

    /**
     * Read-only  mode. The 'readonly' option will be set.
     * You can still edit the buffer, but will be prevented from accidentally overwriting a file.
     * If you do want to overwrite a file, add an exclamation mark to the Ex command, as in ":w!".
     * The -R option also implies the -n option [NoSwap].
     * The 'readonly' option can be reset with ":set noro". See ":help 'readonly'".
     */
    data object ReadOnly : Option("-R")

    /**
     * Recovery mode. The swap file is used to recover a crashed editing session.
     * The swap file is a file with the same filename as the text file with ".swp" appended.
     * @param swapFile null means just list swap files, with information about using them for recovery.
     * See ":help recovery".
     */
    data class Recovery(val swapFile: String? = null) : Option("-r", swapFile)

    /** Silent mode.  Only when started as "Ex" or when the "-e" option was given before the "-s" option. */
    data object Silent : Option("-s")

    /**
     * The script file [inTypedCharsFile] is read.
     * The characters in the file are interpreted as if you had typed them.
     * The same can be done with the command ":source! [inTypedCharsFile]".
     * If the end of the file is reached before the editor exits,
     * further characters are read from the keyboard.
     */
    data class ScriptIn(val inTypedCharsFile: String) : Option("-s", inTypedCharsFile)

    /**
     * All the characters that you type are recorded in the [outTypedCharsFile], until you exit Vim.
     * This is useful if you want to create a script file to be used with "vim -s" or ":source!" [ScriptIn].
     * If the [outTypedCharsFile] file exists, characters are either appended (default),
     * or file is overwritten (when [overwrite] == true).
     */
    data class ScriptOut(val outTypedCharsFile: String, val overwrite: Boolean = false)
      : Option(if (overwrite) "-W" else "-w", outTypedCharsFile) {
      val append: Boolean get() = !overwrite
    }

    /** Use encryption when writing files. Will prompt for a crypt key. */
    data object Encrypt : Option("-x")

    /**
     * Tells Vim the name of the terminal you are using. Only required when the automatic way doesn't work.
     * Should be a terminal known to Vim (builtin) or defined in the termcap or terminfo file.
     */
    data class Terminal(val terminalName: String) : Option("-T", terminalName)

    /**
     * Give messages about which files are sourced and for reading and writing a viminfo file.
     * The default [verbosity] (when null) is the is 10.
     */
    data class Verbose(val verbosity: Int? = null) : Option("-V" + verbosity?.toString().orEmpty())

    /**
     * Connect to a Vim server and make it edit the files given in the rest of the arguments.
     * If no server is found a warning is given and the files are edited in the current Vim.
     * You can specify the server name to connect to with [ServerName].
     */
    data object Remote : Option("--remote")

    /** As --remote [Remote], but without the warning when no server is found. */
    data object RemoteSilent : Option("--remote-silent")

    /** As --remote [Remote], but Vim does not exit until the files have been edited. */
    data object RemoteWait : Option("--remote-wait")

    /** As --remote-wait [RemoteWait], but without the warning when no server is found. */
    data object RemoteWaitSilent : Option("--remote-wait-silent")

    /** Connect to a Vim server, evaluate [expr] in it and print the result on stdout. */
    data class RemoteExpr(val expr: String) : Option("--remote-expr", expr)

    /** Connect to a Vim server and send [keys] to it. */
    data class RemoteSend(val keys: String) : Option("--remote-send", keys)

    /** List the names of all Vim servers that can be found. */
    data object ServerList : Option("--serverlist")

    /**
     * Use [server] as the server name. Used for the current Vim, unless used with a --remote argument [Remote],
     * then it's the name of the server to connect to.
     */
    data class ServerName(val server: String) : Option("--servername", server)

    /** GTK GUI only: Use the GtkPlug mechanism to run gvim in another window. */
    data class SocketId(val id: String) : Option("--socketid", id)

    /** During startup write timing messages to the [file]. */
    data class StartupTime(val file: String) : Option("--startuptime", file)

    data object Version : Option("--version")

    /**
     * Denotes the end of the options. Arguments after this will be handled as a file name.
     * This can be used to edit a filename that starts with a '-'.
     */
    data object EOOpt : Option("--")
  }

  operator fun Option.unaryMinus() = options.add(this)
}
