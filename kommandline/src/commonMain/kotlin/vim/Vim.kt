package pl.mareklangiewicz.kommand.vim

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.chk
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.vim.XVim.Option.*
import pl.mareklangiewicz.kommand.vim.XVim.Option.Companion.KeysScriptStdInForVim
import pl.mareklangiewicz.kommand.vim.XVim.Option.Companion.VimRcNONE

/**
 * When opening stdin content, vim expects commands from stderr.
 *
 * BTW: When launching vim from terminal, all (0, 1, 2) streams are connected to the same tty device by default,
 * so in that case, it's great default behavior, that vim tries to use stderr as input when stdin is used for content.
 */
@DelicateApi("When opening stdin content, vim expects commands from (redirected) stderr!")
fun vimStdIn(init: XVim.() -> Unit = {}): XVim = vim("-", init = init)

fun gvimStdIn(init: XVim.() -> Unit = {}): XVim = gvim("-", init = init)

fun nvimStdIn(init: XVim.() -> Unit = {}): XVim = nvim("-", init = init)

fun nvimMan(manpage: String, section: ManSection? = null): XVim = nvim {
  val cmd = section?.number?.let { "hid Man $it $manpage" } ?: "hid Man $manpage"
  -ExCmd(cmd)
}

/** GVim can display 'reading from stdin...' until it reads full [inLineS] flow and only then show the full content */
fun gvimLineS(inLineS: Flow<String>, init: XVim.() -> Unit = {}) = ReducedScript {
  gvimStdIn(init).ax(inLineS = inLineS)
}

fun gvimLines(inLines: List<String>, init: XVim.() -> Unit = {}) = gvimLineS(inLines.asFlow(), init)

fun gvimContent(inContent: String, init: XVim.() -> Unit = {}) = gvimLines(inContent.lines(), init)


fun vim(vararg files: String, init: XVim.() -> Unit = {}) = XVim(XVim.Type.vim, files.toMutableList()).apply(init)

fun nvim(vararg files: String, init: XVim.() -> Unit = {}) = XVim(XVim.Type.nvim, files.toMutableList()).apply(init)

// FIXME NOW: use Path everywhere
fun gvim(vararg files: String, init: XVim.() -> Unit = {}) = XVim(XVim.Type.gvim, files.toMutableList()).apply(init)
// TODO NOW: nvim, opening specific lines, opening in existing editor (is servername same as in vim?),
//  combine with Ide as in kolib openInIdeOrGVim but better selecting (with nvim too)

/**
 * Useful for playing with ex-mode interactively before writing script for one of:
 * [vimExScriptStdIn] or [vimExScriptContent] or [vimExScriptFile]
 * Note: there is :vi (:visual) command available to switch to normal (visual) mode.
 */
fun vimEx(vararg files: String, init: XVim.() -> Unit = {}): XVim = vim(*files) { -ExMode; init() }

/**
 * Useful for playing with ex-mode interactively before writing script for one of:
 * [vimExScriptStdIn] or [vimExScriptContent] or [vimExScriptFile]
 * This "Improved" flavor differs mostly (only??) in interactive features, so it's nicer,
 * but still can be useful playground before creating ex-mode script.
 * Note: there is :vi (:visual) command available to switch to normal (visual) mode.
 */
fun vimExIm(vararg files: String, init: XVim.() -> Unit = {}): XVim = vim(*files) { -ExImMode; init() }

fun vimExScriptStdIn(
  vararg files: String,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW nvim is always nocompatible
  if (isCleanMode) -CleanMode
  -ExScriptMode // BTW initialization should be skipped in this mode
}

/**
 * Normally should not be needed, but in case of problems it can be useful to experiment with these settings
 * Proposed settings are inspired by answers/flags from SO (which can be incorrect or redundant):
 * https://stackoverflow.com/questions/18860020/executing-vim-commands-in-a-shell-script
 */
@OptIn(NotPortableApi::class)
fun vimExScriptStdInWithExplicitSettings(
  vararg files: String,
  isViCompat: Boolean = false,
  isDebugMode: Boolean = false,
  isCleanMode: Boolean = true,
  isNoPluginMode: Boolean = false,
  isVimRcNONE: Boolean = true,
  isSwapNONE: Boolean = false, // in nvim swap is disabled in ExScriptMode anyway (not sure about original vim)
  isSetNoMore: Boolean = false, // I guess it shouldn't be needed because ExScriptMode is not TUI, but I'm not sure.
  isTermDumb: Boolean = false, // I guess it shouldn't be needed because ExScriptMode is not TUI, but I'm not sure.
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW nvim is always nocompatible
  if (isDebugMode) -DebugMode
  if (isCleanMode) -CleanMode
  if (isNoPluginMode) -NoPluginMode
  if (isVimRcNONE) -VimRcNONE // although initialization should be skipped anyway in ExScriptMode
  if (isSwapNONE) -SwapNONE
  if (isSetNoMore) -ExCmd("set nomore") // avoids blocking/pausing when some output/listing fills whole screen
  if (isTermDumb) -TermName("dumb") // BTW nvim does not support it
  -ExScriptMode
}

fun vimExScriptContent(
  exScriptContent: String,
  vararg files: String,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
): ReducedKommand<List<String>> = vimExScriptStdIn(files = files, isViCompat = isViCompat, isCleanMode = isCleanMode)
  .reducedManually {
    stdin.collect(exScriptContent.lineSequence().asFlow())
    val out = stdout.toList() // ex-commands can print stuff (like :list, :number, :print, :set, also see :verbose)
    awaitAndChkExit(firstCollectErr = true)
    out
  }

/**
 * This version uses [XVim.Option.Session] for [exScriptFile].
 * @param exScriptFile can not start with "-" (or be empty).
 */
fun vimExScriptFile(
  exScriptFile: String = "Session.vim",
  vararg files: String,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW nvim is always nocompatible
  if (isCleanMode) -CleanMode
  -ExScriptMode // still needed (even though Session below) because we want silent mode prepared for usage without TTY
  -Session(exScriptFile)
} // BTW ex-commands can print stuff (like :list, :number, :print, :set, also see :verbose)


@OptIn(NotPortableApi::class)
fun vimKeysScriptFile(
  keysScriptFile: String,
  vararg files: String,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
  isSwapNONE: Boolean = true,
  isSetNoMore: Boolean = true,
  isTermDumb: Boolean = true,
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW nvim is always nocompatible
  if (isCleanMode) -CleanMode
  if (isSwapNONE) -SwapNONE
  if (isSetNoMore) -ExCmd("set nomore") // avoids blocking/pausing when some output/listing fills whole screen
  if (isTermDumb) -TermName("dumb") // BTW nvim does not support it
  -KeysScriptIn(keysScriptFile)
} // BTW stdout should not be used as vim will unfortunately print screen content there

/**
 * Note: current impl adds \n at the end of the keys script.
 * It's because internal details of [StdinCollector.collect], [Kommand.ax], etc..
 * generally KommandLine currently treats input as flow of lines.
 */
@OptIn(NotPortableApi::class, DelicateApi::class)
fun vimKeysScriptStdIn(
  vararg files: String,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
  isSwapNONE: Boolean = true,
  isSetNoMore: Boolean = true,
  isTermDumb: Boolean = true,
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW nvim is always nocompatible
  if (isCleanMode) -CleanMode
  if (isSwapNONE) -SwapNONE
  if (isSetNoMore) -ExCmd("set nomore") // avoids blocking/pausing when some output/listing fills whole screen
  if (isTermDumb) -TermName("dumb") // BTW nvim does not support it
  -KeysScriptStdInForVim // BTW waiting for answer if it's a good approach: https://github.com/vim/vim/discussions/15315
} // BTW stdout should not be used as vim will unfortunately print screen content there

/**
 * Note: current impl adds \n at the end of the [keysScriptContent].
 * It's because internal details of [StdinCollector.collect], [Kommand.ax], etc..
 * generally KommandLine currently treats input as flow of lines.
 */
@OptIn(NotPortableApi::class, DelicateApi::class)
fun vimKeysScriptContent(
  keysScriptContent: String,
  vararg files: String,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
  isSwapNONE: Boolean = true,
  isSetNoMore: Boolean = true,
  isTermDumb: Boolean = true,
): ReducedKommand<Int> = vimKeysScriptStdIn(
  files = files,
  isViCompat = isViCompat,
  isCleanMode = isCleanMode,
  isSwapNONE = isSwapNONE,
  isSetNoMore = isSetNoMore,
  isTermDumb = isTermDumb,
).reducedManually {
  stdin.collect(keysScriptContent.lineSequence().asFlow())
  awaitAndChkExit(firstCollectErr = true)
}



// Note: I could add all script flavored wrappers here for nvim as well, but let's not do it.
// Vim is more portable (github actions etc), and user can wrap nvim himself if needed.


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
     * Graphical mode of [vim]. Starts new GUI window. Can also be done with "-g" argument [Option.GuiMode]
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
     * Can also be done with the "-E" argument [Option.ExImMode]. See also [Type.ex]
     */
    @DelicateApi("Normally not installed. Use vim -E instead. See [Option.ExImMode].")
    exim,

    /**
     * The [vim], but started in read-only mode. You will be protected from writing the files.
     * Can also be done with the "-R" argument [Option.ReadOnly].
     */
    view,

    /** Graphical mode of [view], so also read-only. */
    gview,

    /** Graphical mode of [ex]. */
    @DelicateApi("Usually not installed. Use vim -e -g instead. See [Option.ExMode] [Option.GuiMode].")
    gex,

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

    /** Start [vim] in diff mode. Can also be done with the "-d" argument [Option.DiffMode]. */
    vimdiff,

    /** Start [gvim] in diff mode. Can also be done with the "-d" argument [Option.DiffMode]. */
    gvimdiff,
  }

  override val name get() = type.name
  override val args get() = options.flatMap { it.str } + files

  // TODO_someday: Go through all :h starting.txt in NVim, and make sure, we have all options here
  // (I skipped some for now) (annotate which are NotPortableApi)

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
     * The [sessionFile] will be sourced after the first file has been read.
     * This is equivalent to ExCmd("source [sessionFile]").
     * The [sessionFile] cannot start with '-' (or be empty).
     */
    data class Session(val sessionFile: String = "Session.vim") : Option("-S", sessionFile) {
      init {
        chk(sessionFile.isNotEmpty()) { "The sessionFile can NOT be empty."}
        chk(sessionFile[0] != '-') { "The sessionFile can NOT start with the \"-\"."}
      }
    }

    /** Binary mode.  A few options will be set that makes it possible to edit a binary or executable file. */
    data object Binary : Option("-b")

    /**
     * Set 'compatible' / 'nocompatible' option.
     * Without [ViCompat] vim should set compatible/nocompatible according .vimrc absence/presence.
     * Warning: nvim doesn't support it and is always nocompatible.
     * @param compatible sets vim option 'compatible' if true, or 'nocompatible' if false.
     *   true will This will make Vim behave mostly like Vi (even if a .vimrc file exists).
     *   false will make Vim behave a bit better, but less Vi compatible (even if a .vimrc file does not exist).
     */
    data class ViCompat(val compatible: Boolean) : Option(if (compatible) "-C" else "-N")

    /**
     * Start in diff mode. There should between two to eight file name arguments.
     * Vim will open all the files and show differences between them. Works like vimdiff
     */
    data object DiffMode : Option("-d")

    /** Go to debugging mode when executing the first command from a script. */
    data object DebugMode : Option("-D")

    /** Start in Ex-mode. Just like executable was called "ex" [Type.ex]. See also [ExImMode] */
    data object ExMode : Option("-e")

    /**
     * Improved Ex-mode, just like the executable was called "exim" [Type.exim]. See also [ExMode].
     * Allows for more advanced commands than the Ex-mode, and behaves more like typing :commands in Vim.
     * All command line editing, completion etc. is available.
     */
    data object ExImMode : Option("-E")


    // FIXME NOW: check what if I put filename afterwards. Will it misinterpret it as KeysScriptIn?? (check both vim and nvim)
    /**
     * Ex Script (aka Ex Silent) mode, aka "batch mode". No UI, disables most prompts and messages.
     * Remember to quit vim at the end of the Ex-mode script (which is read from stdin).
     * BTW if you forget in nvim, then it will add for you sth like "-c qa!", but vim WILL NOT!
     * So don't relay on it, especially if running scripts on CI where there's no nvim installed.
     * Note: If Vim appears to be stuck try typing "qa!<Enter>".
     * You don't get a prompt thus you can't see Vim is waiting for you to type something.
     * Initializations are skipped (except the ones given with the "-u" argument).
     */
    data object ExScriptMode : Option("-es")

    // FIXME NOW: check what if I put filename afterwards. Will it misinterpret it as ScriptIn?? (check both vim and nvim)
    /**
     * Ex Im Script (aka Ex Im Silent) mode. Delicate because tricky differences between vim an nvim. Use [ExScriptMode] instead.
     * See :h -s-ex in both vim and nvim, also see :h vim-differences /Startup in nvim.
     * See also: [ExScriptMode]; [ExImMode]
     */
    @DelicateApi("Tricky to get always right. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
    @NotPortableApi("Supported differently in vim and nvim. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
    data object ExImScriptMode : Option("-Es")

    /**
     * Executes Lua {script} non-interactively (no UI) with optional args after processing any preceding cli-arguments,
     * then exits. Exits 1 on Lua error. See -S to run multiple Lua scripts without args, with a UI.
     */
    @NotPortableApi("Only in nvim")
    data class LuaScriptMode(val luaFile: String) : Option("-l", luaFile)

    /**
     * Execute a Lua script, similarly to -l, but the editor is not initialized.
     * This gives a Lua environment similar to a worker thread. See :h lua-loop-threading.
     * Unlike -l no prior arguments are allowed.
     */
    @NotPortableApi("Only in nvim")
    data class LuaWorkerMode(val luaFile: String) : Option("-ll", luaFile)

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

    @NotPortableApi("NVim only.")
    data object EmbedMode : Option("--embed")

    @NotPortableApi("NVim only.")
    data object HeadlessMode : Option("--headless")

    /** Skip loading plugins. Implied by -u NONE [VimRcNONE]. */
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
    data object GuiMode : Option("-g")

    data object Help : Option("-h")

    /**
     * Specifies  the filename to use when reading or writing the viminfo file, instead of the default "~/.viminfo".
     * This can also be used to skip the use of the .viminfo file, by giving the name "NONE" [VimInfoNONE].
     */
    data class VimInfo(val vimInfoFile: String) : Option("-i", vimInfoFile)

    /** Special case of [VimInfo] */
    data object VimInfoNONE : Option("-i", "NONE")

    /**
     * Use the commands in the [vimRcFile] for initializations.
     * All the other initializations are skipped.
     * Use this to edit a special kind of files.
     * It can also be used to skip all initializations by giving the name "NONE" [VimRcNONE].
     * See ":help initialization" within vim for more details.
     */
    data class VimRc(val vimRcFile: String) : Option("-u", vimRcFile)

    /**
     * Use  the commands in the [gvimRcFile] for GUI initializations.
     * All the other GUI initializations are skipped.
     * It can also be used to skip all GUI initializations by giving the name "NONE" [GVimRcNONE].
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
    data object SwapNONE : Option("-n")

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
     * The -R option also implies the -n option [SwapNONE].
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

    /**
     * Script (aka Silent) mode. Delicate because tricky differences between vim an nvim. Use [ExScriptMode] instead.
     * See :h -s-ex in both vim and nvim, also see :h vim-differences /Startup in nvim.
     * Only when started as "ex" (or "exim") or when the "-e" (or "-E") option was given BEFORE the "-s" option.
     * See also: [Type.ex]; [Type.exim]; [ExMode]; [ExImMode]
     */
    @DelicateApi("Tricky to get always right with other options. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
    @NotPortableApi("Supported differently in vim and nvim. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
    data object ScriptMode : Option("-s")

    /**
     * The script file [inKeysFile] is read.
     * The characters in the file are interpreted as if you had typed them.
     * The same can be done with the command ":source! [inKeysFile]".
     * If the end of the file is reached before the editor exits, further characters are read from the keyboard.
     * It might be good idea to use [CleanMode] too. Does NOT work with -es or -Es ([ExScriptMode] or [ExImScriptMode]).
     */
    data class KeysScriptIn(val inKeysFile: String) : Option("-s", inKeysFile)
    // yes, the same letter "-s" as ScriptMode, but with argument.

    /**
     * All the characters that you type are recorded in the [outKeysFile], until you exit Vim.
     * This is useful if you want to create a script file to be used with "vim -s" or ":source!" [KeysScriptIn].
     *
     * Warning: Different settings, plugins etc. may mess up with recorded keys.
     * It's best to always use [CleanMode] with [KeysScriptOut], and it looks like nvim records better for me,
     * so prefer nvim (even if later "replaying" [KeysScriptIn] with normal vim (like on CI or sth))
     * It can also be good idea to review recorded file and clean it up by hand??
     * And try to use basic keys while recording.
     *
     * If the [outKeysFile] file exists, characters are either appended (default),
     * or file is overwritten (when [overwrite] == true).
     */
    @DelicateApi("To avoid garbage it's best to use CleanMode (and nvim works better even if then replaying with vim)")
    data class KeysScriptOut(val outKeysFile: String, val overwrite: Boolean = false)
      : Option(if (overwrite) "-W" else "-w", outKeysFile) {
      val append: Boolean get() = !overwrite
    }

    /** Use encryption when writing files. Will prompt for a crypt key. */
    data object Encrypt : Option("-x")

    /**
     * Tells Vim the name of the terminal you are using. Only required when the automatic way doesn't work.
     * Should be a terminal known to Vim (builtin) or defined in the termcap or terminfo file.
     */
    @NotPortableApi("NVim doesn't support this.")
    data class TermName(val term: String) : Option("-T", term)

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

    companion object {

      /** Special case of [VimRc] */
      val VimRcNONE = VimRc("NONE")

      /** Special case of [GVimRc] */
      val GVimRcNONE = GVimRc("NONE")

      // Some modes are known by names like "silent-mode", so let's add aliases for users knowing these names.

      // Note: This name for ScriptMode is here to stay mostly as some form of a documentation.
      /** Same as [ScriptMode] emphasizing that it's silencing behavior */
      @DelicateApi("Tricky to get always right with other options. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
      @NotPortableApi("Supported differently in vim and nvim. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
      val SilentMode = ScriptMode

      // Note: this second name for ScriptMode is here to stay mostly as some form of a documentation.
      /** Same as [ExScriptMode] emphasizing that it's silencing behavior */
      val ExSilentMode = ExScriptMode

      // Note: this second name for ExImScriptMode is here to stay mostly as some form of a documentation.
      /** Same as [ExImScriptMode] emphasizing that it's silencing behavior */
      @DelicateApi("Tricky to get always right. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
      @NotPortableApi("Supported differently in vim and nvim. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
      val ExImSilentMode = ExImScriptMode


      /**
       * NVim interprets the "-" as stdin. Also in case of KeysScriptIn("-"), as in [KeysScriptStdInForNVim],
       * after stdin finishes, it starts getting actual keyboard from user (reopens tty?), so that's very useful.
       * On the other hand if we were using "-s /dev/stdin", as in [KeysScriptStdInForVim],
       * it could freeze, and the nvim process would have to be killed.
       * It did freeze when I tried experiment like this: printf 'iBLA\e:wq\n' | nvim -s /dev/stdin bla.txt
       * BTW looks like in that (incorrect) use-case, the nvim doesn't even run any given keys from /dev/stdin,
       * maybe it first try to read whole stdin "file", but /dev/stdin is never closing/ending?
       */
      @NotPortableApi("NVim interprets the \"-\" as stdin, but Vim doesn't and tries to open the \"-\" file.")
      val KeysScriptStdInForNVim = KeysScriptIn("-")


        /**
         * Note: (if don't have nvim available) it's better to start experimenting with this using [GuiMode],
         * because gvim nicely allows user to continue using keyboard, and inspect what has happened.
         * Then when the keys are all good, add the :wq (or sth) to MAKE SURE it automatically quits,
         * and then maybe switch from gvim/[GuiMode] to just vim.
         */
        @DelicateApi("Provided keys have to quit vim at the end (if no GuiMode). Or Vim will do sth, show error and output some additional garbage.")
        @NotPortableApi("NVim interprets the \"-\" as stdin, but Vim doesn't. So /dev/stdin might work better in Vim.")
      val KeysScriptStdInForVim = KeysScriptIn("/dev/stdin")
    }
  }

  operator fun Option.unaryMinus() = options.add(this)
}
