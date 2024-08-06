package pl.mareklangiewicz.kommand.vim

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.chk
import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kground.io.pth
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.vim.XVimType.*
import pl.mareklangiewicz.kommand.vim.XVimOpt.*
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.CursorPos
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.KeysScriptStdInForVim
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.VimRcNONE
import pl.mareklangiewicz.udata.strf



@OptIn(DelicateApi::class)
fun gvimOpen(
  path1: Path? = null,
  path2: Path? = null,
  path3: Path? = null,
  vararg useNamedArgs: Unit,
  line: Int? = null,
  column: Int? = null,
): XVim = gvim(path1, path2, path3) {
  when {
    column != null -> -CursorPos(line ?: 1, column)
    line != null -> -CursorLine(line) // column IS null in this case
  }
}

/**
 * When opening stdin content, Vim expects commands from stderr.
 *
 * BTW: When launching Vim from terminal, all (0, 1, 2) streams are connected to the same tty device by default,
 * so in that case, it's great default behavior, that Vim tries to use stderr as input when stdin is used for content.
 */
@DelicateApi("When opening stdin content, Vim expects commands from (redirected) stderr!")
fun vimStdIn(init: XVim.() -> Unit = {}): XVim = vim("-".pth, init = init)

@DelicateApi
fun gvimStdIn(init: XVim.() -> Unit = {}): XVim = gvim("-".pth, init = init)

@DelicateApi
fun nvimStdIn(init: XVim.() -> Unit = {}): XVim = nvim("-".pth, init = init)

@OptIn(DelicateApi::class)
fun nvimMan(manpage: String, section: ManSection? = null): XVim = nvim {
  val cmd = section?.number?.let { "hid Man $it $manpage" } ?: "hid Man $manpage"
  -ExCmd(cmd)
}

/** GVim can display 'reading from stdin...' until it reads full [inLineS] flow and only then show the full content */
@DelicateApi
// TODO_later: Use reducedManually instead, so it's ReducedKommand, and expected lines can be tested in samples (rs)
fun gvimLineS(inLineS: Flow<String>, init: XVim.() -> Unit = {}) = ReducedScript {
  gvimStdIn(init).ax(inLineS = inLineS)
}

@DelicateApi
fun gvimLines(inLines: List<String>, init: XVim.() -> Unit = {}): ReducedScript<List<String>> =
  gvimLineS(inLines.asFlow(), init)

@DelicateApi
fun gvimContent(inContent: String, init: XVim.() -> Unit = {}): ReducedScript<List<String>> =
  gvimLines(inContent.lines(), init)

@DelicateApi
fun xvim(type: XVimType, vararg files: Path?, init: XVim.() -> Unit = {}) =
  XVim(type, nonopts = files.mapNotNull { it?.strf }.toMutableList()).apply(init)

@DelicateApi
fun vim(vararg files: Path?, init: XVim.() -> Unit = {}) = xvim(Vim, *files, init = init)

@DelicateApi
fun nvim(vararg files: Path?, init: XVim.() -> Unit = {}) = xvim(NVim, *files, init = init)

@DelicateApi
fun gvim(vararg files: Path?, init: XVim.() -> Unit = {}) = xvim(GVim, *files, init = init)
// TODO NOW: NVim, opening specific lines, opening in existing editor (is servername same as in Vim?),
//  combine with Ide as in kolib openInIdeOrGVim but better selecting (with NVim too)

/**
 * Useful for playing with ex-mode interactively before writing script for one of:
 * [vimExScriptStdIn] or [vimExScriptContent] or [vimExScriptFile]
 * Note: there is :vi (:visual) command available to switch to normal (visual) mode.
 */
@DelicateApi
fun vimEx(vararg files: Path, init: XVim.() -> Unit = {}): XVim = vim(*files) { -ExMode; init() }

/**
 * Useful for playing with ex-mode interactively before writing script for one of:
 * [vimExScriptStdIn] or [vimExScriptContent] or [vimExScriptFile]
 * This "Improved" flavor differs mostly (only??) in interactive features, so it's nicer,
 * but still can be useful playground before creating ex-mode script.
 * Note: there is :vi (:visual) command available to switch to normal (visual) mode.
 */
@DelicateApi
fun vimExIm(vararg files: Path, init: XVim.() -> Unit = {}): XVim = vim(*files) { -ExImMode; init() }

@OptIn(DelicateApi::class)
fun vimExScriptStdIn(
  vararg files: Path,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW NVim is always nocompatible
  if (isCleanMode) -CleanMode
  -ExScriptMode // BTW initialization should be skipped in this mode
}

/**
 * Normally should not be needed, but in case of problems it can be useful to experiment with these settings
 * Proposed settings are inspired by answers/flags from SO (which can be incorrect or redundant):
 * https://stackoverflow.com/questions/18860020/executing-vim-commands-in-a-shell-script
 */
@OptIn(NotPortableApi::class, DelicateApi::class)
fun vimExScriptStdInWithExplicitSettings(
  vararg files: Path,
  isViCompat: Boolean = false,
  isDebugMode: Boolean = false,
  isCleanMode: Boolean = true,
  isNoPluginMode: Boolean = false,
  isVimRcNONE: Boolean = true,
  isSwapNONE: Boolean = false, // in NVim swap is disabled in ExScriptMode anyway (not sure about original Vim)
  isSetNoMore: Boolean = false, // I guess it shouldn't be needed because ExScriptMode is not TUI, but I'm not sure.
  isTermDumb: Boolean = false, // I guess it shouldn't be needed because ExScriptMode is not TUI, but I'm not sure.
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW NVim is always nocompatible
  if (isDebugMode) -DebugMode
  if (isCleanMode) -CleanMode
  if (isNoPluginMode) -NoPluginMode
  if (isVimRcNONE) -VimRcNONE // although initialization should be skipped anyway in ExScriptMode
  if (isSwapNONE) -SwapNONE
  if (isSetNoMore) -ExCmd("set nomore") // avoids blocking/pausing when some output/listing fills whole screen
  if (isTermDumb) -TermName("dumb") // BTW NVim does not support it
  -ExScriptMode
}

@OptIn(DelicateApi::class)
fun vimExScriptContent(
  exScriptContent: String,
  vararg files: Path,
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
 * This version uses [Session] for [exScriptFile].
 * @param exScriptFile can not start with "-" (or be empty).
 */
@OptIn(DelicateApi::class)
fun vimExScriptFile(
  exScriptFile: String = "Session.vim",
  vararg files: Path,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW NVim is always nocompatible
  if (isCleanMode) -CleanMode
  -ExScriptMode // still needed (even though Session below) because we want silent mode prepared for usage without TTY
  -Session(exScriptFile)
} // BTW ex-commands can print stuff (like :list, :number, :print, :set, also see :verbose)


@OptIn(NotPortableApi::class, DelicateApi::class)
fun vimKeysScriptFile(
  keysScriptFile: Path,
  vararg files: Path,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
  isSwapNONE: Boolean = true,
  isSetNoMore: Boolean = true,
  isTermDumb: Boolean = true,
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW NVim is always nocompatible
  if (isCleanMode) -CleanMode
  if (isSwapNONE) -SwapNONE
  if (isSetNoMore) -ExCmd("set nomore") // avoids blocking/pausing when some output/listing fills whole screen
  if (isTermDumb) -TermName("dumb") // BTW NVim does not support it
  -KeysScriptIn(keysScriptFile)
} // BTW stdout should not be used as Vim will unfortunately print screen content there

/**
 * Note: current impl adds \n at the end of the keys script.
 * It's because internal details of [StdinCollector.collect], [Kommand.ax], etc..
 * generally KommandLine currently treats input as flow of lines.
 */
@OptIn(NotPortableApi::class, DelicateApi::class)
fun vimKeysScriptStdIn(
  vararg files: Path,
  isViCompat: Boolean = false,
  isCleanMode: Boolean = true,
  isSwapNONE: Boolean = true,
  isSetNoMore: Boolean = true,
  isTermDumb: Boolean = true,
): XVim = vim(*files) {
  -ViCompat(isViCompat) // setting explicitly in scripts (not rely on .vimrc presence). BTW NVim is always nocompatible
  if (isCleanMode) -CleanMode
  if (isSwapNONE) -SwapNONE
  if (isSetNoMore) -ExCmd("set nomore") // avoids blocking/pausing when some output/listing fills whole screen
  if (isTermDumb) -TermName("dumb") // BTW NVim does not support it
  -KeysScriptStdInForVim // BTW waiting for answer if it's a good approach: https://github.com/vim/vim/discussions/15315
} // BTW stdout should not be used as Vim will unfortunately print screen content there

/**
 * Note: current impl adds \n at the end of the [keysScriptContent].
 * It's because internal details of [StdinCollector.collect], [Kommand.ax], etc..
 * generally KommandLine currently treats input as flow of lines.
 */
@OptIn(NotPortableApi::class, DelicateApi::class)
fun vimKeysScriptContent(
  keysScriptContent: String,
  vararg files: Path,
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



// Note: I could add all script flavored wrappers here for NVim as well, but let's not do it.
// Vim is more portable (github actions etc), and user can wrap NVim himself if needed.


@DelicateApi
@Suppress("unused")
data class XVim(
  val type: XVimType = Vim,
  override val opts: MutableList<XVimOpt> = mutableListOf(),
  override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<XVimOpt> {
  override val name get() = type.namelowords("")
}


/** Type of Vim binary. Names (when lowercased) are actual binary file/kommand names. */
enum class XVimType {

  /** Neo Vim */
  NVim,

  /** Base/official/original Vim. Started in normal way. */
  Vim,

  /**
   * Graphical mode of [Vim]. Starts new GUI window. Can also be done with "-g" argument [GuiMode]
   * Note1: it does NOT use [NVim] - which doesn't have official gui support.
   * Note2: It's usually better to start [NVim] inside kitty terminal - better nerd fonts support and everything.
   */
  GVim,


  /** The [Vim], but might be acting more like Vi (But it depends on .vimrc and the 'compatible' option) */
  Vi,

  /**
   * The [Vim], but started in traditional vi-compatible Ex mode. Go to Normal mode with the ":vi" command.
   * Can also be done with the "-e" argument [ExMode]. See also [ExIm]
   */
  Ex,

  /**
   * The [Vim], but started in improved Ex mode. Go to Normal mode with the ":vi" command.
   * Allows for more advanced commands than the vi-compatible Ex mode, and behaves more like typing :commands in Vim.
   * Can also be done with the "-E" argument [ExImMode]. See also [Ex]
   */
  @DelicateApi("Normally not installed. Use vim -E instead. See [Option.ExImMode].")
  ExIm,

  /**
   * The [Vim], but started in read-only mode. You will be protected from writing the files.
   * Can also be done with the "-R" argument [ReadOnly].
   */
  View,

  /** Graphical mode of [View], so also read-only. */
  GView,

  /** Graphical mode of [Ex]. */
  @DelicateApi("Usually not installed. Use vim -e -g instead. See [Option.ExMode] [Option.GuiMode].")
  GEx,

  /**
   * Easy mode of [GVim]. Graphical, starts new window, behave like click-and-type editor.
   * Can also be done with the "-y" argument [EasyMode].
   */
  EVim,

  /**
   * Easy mode of [GView]. Graphical, starts new window, behave like click-and-type editor, read-only
   * Can also be done with the "-y" argument [EasyMode].
   */
  EView,

  /** [Vim] with restrictions. Can also be done with the "-Z" argument [RestrictedMode]. */
  RVim,

  /** [View] with restrictions. Can also be done with the "-Z" argument [RestrictedMode]. */
  RView,

  /** [GVim] with restrictions. Can also be done with the "-Z" argument [RestrictedMode]. */
  RGVim,

  /** [GView] with restrictions. Can also be done with the "-Z" argument [RestrictedMode]. */
  RGView,

  /** Start [Vim] in diff mode. Can also be done with the "-d" argument [DiffMode]. */
  VimDiff,

  /** Start [GVim] in diff mode. Can also be done with the "-d" argument [DiffMode]. */
  GVimDiff,
}


@DelicateApi
interface XVimOpt : KOptTypical {

  // TODO_someday: Go through all :h starting.txt in NVim, and make sure, we have all options here
  // (I skipped some for now) (annotate which are NotPortableApi)

  // region [GNU Common Opts]
  // https://www.gnu.org/software/coreutils/manual/html_node/Common-options.html
  data object Help : KOptLN(), XVimOpt // Don't risk short -h (ambiguity: sudo -h host; ls -h (human-readable), etc.)
  data object Version : KOptLN(), XVimOpt // Don't risk short -v (ambiguity with "verbose" for many commands)
  data object EOOpt : KOptL(""), XVimOpt
  // endregion [GNU Common Opts]

  /**
   * For the first file the cursor will be positioned on given line.
   * Lines are numbered from 1. Null means last line.
   * See also [CursorLineFind], [CursorLineLast], [CursorPos]
   */
  data class CursorLine(val line: Int?) : XVimOpt, KOptS(line.strfoe, namePrefix = "+")

  /**
   * For the first file the cursor will be positioned in the line with the first occurrence of pattern.
   * See also :help search-pattern
   * See also [CursorLine], [CursorLineLast], [CursorPos]
   */
  data class CursorLineFind(val pattern: String) : XVimOpt, KOptS(pattern, namePrefix = "+/")


  /** Ex [cmd] will be executed after the first file has been read. Can be used up to 10 times. */
  data class ExCmd(val cmd: String) : XVimOpt, KOptS("c", cmd)

  /** Ex [cmd] will be executed BEFORE processing any vimrc file. Can be used up to 10 times. */
  data class ExCmdBeforeRc(val cmd: String) : XVimOpt, KOptL("cmd", cmd, nameSeparator = " ")

  /**
   * The [sessionFile] will be sourced after the first file has been read.
   * This is equivalent to ExCmd("source [sessionFile]").
   * The [sessionFile] cannot start with '-' (or be empty).
   */
  @Suppress("GrazieInspection")
  data class Session(val sessionFile: String = "Session.vim") : XVimOpt, KOptS("S", sessionFile) {
    init {
      chk(sessionFile.isNotEmpty()) { "The sessionFile can NOT be empty."}
      chk(sessionFile[0] != '-') { "The sessionFile can NOT start with the \"-\"."}
    }
  }

  /** Binary mode. A few options will be set that makes it possible to edit a binary or executable file. */
  data object Binary : XVimOpt, KOptS("b")

  /**
   * Set 'compatible' / 'nocompatible' option.
   * Without [ViCompat] Vim should set compatible/nocompatible according .vimrc absence/presence.
   * Warning: NVim doesn't support it and is always nocompatible.
   * @param compatible sets Vim option 'compatible' if true, or 'nocompatible' if false.
   *   true will This will make Vim behave mostly like Vi (even if a .vimrc file exists).
   *   false will make Vim behave a bit better, but less Vi compatible (even if a .vimrc file does not exist).
   */
  data class ViCompat(val compatible: Boolean) : XVimOpt, KOptS(if (compatible) "C" else "N")

  /**
   * Start in diff mode. There should between two and eight file name arguments.
   * Vim will open all the files and show differences between them. Works like vimdiff
   */
  data object DiffMode : XVimOpt, KOptS("d")

  /** Go to debugging mode when executing the first command from a script. */
  data object DebugMode : XVimOpt, KOptS("D")

  /** Start in Ex-mode. Just like executable was called "ex" [Ex]. See also [ExImMode] */
  data object ExMode : XVimOpt, KOptS("e")

  /**
   * Improved Ex-mode, just like the executable was called "exim" [ExIm]. See also [ExMode].
   * Allows for more advanced commands than the Ex-mode, and behaves more like typing :commands in Vim.
   * All command line editing, completion etc. is available.
   */
  data object ExImMode : XVimOpt, KOptS("E")


  /**
   * Ex Script (aka Ex Silent) mode, aka "batch mode". No UI, disables most prompts and messages.
   * Remember to quit Vim at the end of the Ex-mode script (which is read from stdin).
   * BTW if you forget in NVim, then it will add for you sth like "-c qa!", but Vim WILL NOT!
   * So don't relay on it, especially if running scripts on CI where there's no NVim installed.
   * Note: If Vim appears to be stuck try typing "qa!<Enter>".
   * You don't get a prompt thus you can't see Vim is waiting for you to type something.
   * Initializations are skipped (except the ones given with the "-u" argument).
   */
  data object ExScriptMode : XVimOpt, KOptS("es")

  /**
   * Ex Im Script (aka Ex Im Silent) mode. Delicate because tricky differences between Vim an NVim. Use [ExScriptMode] instead.
   * See :h -s-ex in both Vim and NVim, also see :h vim-differences /Startup in NVim.
   * See also: [ExScriptMode]; [ExImMode]
   */
  @DelicateApi("Tricky to get always right. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
  @NotPortableApi("Supported differently in Vim and NVim. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
  data object ExImScriptMode : XVimOpt, KOptS("Es")

  /**
   * Executes Lua {script} non-interactively (no UI) with optional args after processing any preceding cli-arguments,
   * then exits. Exits 1 on Lua error. See -S to run multiple Lua scripts without args, with a UI.
   */
  @NotPortableApi("Only in NVim")
  data class LuaScriptMode(val luaFile: String) : XVimOpt, KOptS("l", luaFile)

  /**
   * Execute a Lua script, similarly to -l, but the editor is not initialized.
   * This gives a Lua environment similar to a worker thread. See :h lua-loop-threading.
   * Unlike -l no prior arguments are allowed.
   */
  @NotPortableApi("Only in NVim")
  data class LuaWorkerMode(val luaFile: String) : XVimOpt, KOptS("ll", luaFile)

  /**
   * Start in vi-compatible mode, just like the executable was called "vi" [Vi].
   * This only has effect when the executable is called "ex" [Ex].
   */
  data object ViMode : XVimOpt, KOptS("v")

  /**
   * Start Vim in easy mode, just like the executable was called "evim" [EVim] or "eview" [EView].
   * Makes Vim behave like a click-and-type editor.
   */
  data object EasyMode : XVimOpt, KOptS("y")

  data object RestrictedMode : XVimOpt, KOptS("Z")

  /**
   * Do not use any personal configuration (vimrc, plugins, etc.).
   * Useful to see if a problem reproduces with a clean Vim setup.
   */
  data object CleanMode : XVimOpt, KOptL("clean")

  @NotPortableApi("NVim only.")
  data object EmbedMode : XVimOpt, KOptL("embed")

  @NotPortableApi("NVim only.")
  data object HeadlessMode : XVimOpt, KOptL("headless")

  /** Skip loading plugins. Implied by -u NONE [VimRcNONE]. */
  data object NoPluginMode : XVimOpt, KOptL("noplugin")

  /**
   * Foreground. For the GUI version, Vim will NOT fork and detach from the shell it was started in.
   * Should be used when Vim is executed by a program that should wait for the edit session to finish.
   */
  @NotPortableApi("Vim only (not NVim).")
  data object Foreground : XVimOpt, KOptS("f")


  /**
   * The GUI mode. Supported only in original [Vim] and only if compiled with GUI support. Like [GVim]
   * Note: the [NVim] does NOT support it, but better solution is to run [NVim] inside kitty terminal.
   */
  @NotPortableApi("Vim only (not NVim).")
  data object GuiMode : XVimOpt, KOptS("g")

  /**
   * Specifies  the filename to use when reading or writing the viminfo file, instead of the default "~/.viminfo".
   * This can also be used to skip the use of the .viminfo file, by giving the name "NONE" [VimInfoNONE].
   */
  data class VimInfo(val vimInfoFile: String) : XVimOpt, KOptS("i", vimInfoFile)

  /**
   * Use the commands in the [vimRcFile] for initializations.
   * All the other initializations are skipped.
   * Use this to edit a special kind of files.
   * It can also be used to skip all initializations by giving the name "NONE" [VimRcNONE].
   * See ":help initialization" within Vim for more details.
   */
  data class VimRc(val vimRcFile: String) : XVimOpt, KOptS("u", vimRcFile)

  /**
   * Use  the commands in the [gvimRcFile] for GUI initializations.
   * All the other GUI initializations are skipped.
   * It can also be used to skip all GUI initializations by giving the name "NONE" [GVimRcNONE].
   * See ":help gui-init" within Vim for more details.
   */
  @NotPortableApi("Vim only (not NVim).")
  data class GVimRc(val gvimRcFile: String) : XVimOpt, KOptS("U", gvimRcFile)

  /**
   * Modifying files is disabled. Resets the 'write' option.
   * You can still modify the buffer, but writing a file is not possible.
   */
  data object NoWrite : XVimOpt, KOptS("m")

  /**
   * The 'modifiable' and 'write' options will be unset, so that changes are not allowed
   * and files can not be written. Note that these options can be set to enable making modifications,
   * but writing a file is not possible.
   */
  data object NoWriteOrModify : XVimOpt, KOptS("M")

  /**
   * No swap file will be used. Recovery after a crash will be impossible.
   * Handy if you want to edit a file on a very slow medium (e.g. floppy).
   * Can also be done with ":set uc=0". Can be undone with ":set uc=200".
   */
  data object SwapNONE : XVimOpt, KOptS("n")

  /**
   * Don't connect to the X server. Shortens startup time in a terminal,
   * but the window title and clipboard will not be used.
   */
  @NotPortableApi("Vim only (not NVim).") // I guess (TODO_someday: check)
  data object NoXServer : XVimOpt, KOptS("X")

  /** Become an editor server for NetBeans. See the docs for details. */
  @NotPortableApi("Vim only (not NVim).") // I guess (TODO_someday: check)
  data object NetBeansServer : XVimOpt, KOptS("nb")


  /** Open [nr] windows stacked. null means one window for each file. */
  data class WindowsStacked(val nr: Int? = null) : XVimOpt, KOptS("o" + nr.strfoe)

  /** Open [nr] windows side by side. null means one window for each file. */
  data class WindowsSideBySide(val nr: Int? = null) : XVimOpt, KOptS("O" + nr.strfoe)

  /** Open [nr] tab pages. null means one tab for each file. */
  data class Tabs(val nr: Int? = null) : XVimOpt, KOptS("p" + nr.strfoe)

  /**
   * Read-only  mode. The 'readonly' option will be set.
   * You can still edit the buffer, but will be prevented from accidentally overwriting a file.
   * If you do want to overwrite a file, add an exclamation mark to the Ex command, as in ":w!".
   * The -R option also implies the -n option [SwapNONE].
   * The 'readonly' option can be reset with ":set noro". See ":help 'readonly'".
   */
  data object ReadOnly : XVimOpt, KOptS("R")

  /**
   * Recovery mode. The swap file is used to recover a crashed editing session.
   * The swap file is a file with the same filename as the text file with ".swp" appended.
   * @param swapFile null means just list swap files, with information about using them for recovery.
   * See ":help recovery".
   */
  data class Recovery(val swapFile: String? = null) : XVimOpt, KOptS("r", swapFile)

  /**
   * Script (aka Silent) mode. Delicate because tricky differences between Vim an NVim. Use [ExScriptMode] instead.
   * See :h -s-ex in both Vim and NVim, also see :h vim-differences /Startup in NVim.
   * Only when started as "ex" (or "exim") or when the "-e" (or "-E") option was given BEFORE the "-s" option.
   * See also: [Ex]; [ExIm]; [ExMode]; [ExImMode]
   * Generally "vim -e -s" is kinda like poor man's "vim -es", so just use -es ([ExScriptMode])
   */
  @DelicateApi("Tricky to get always right with other options. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
  @NotPortableApi("Supported differently in Vim and NVim. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
  data object ScriptMode : XVimOpt, KOptS("s")

  /**
   * The script file [inKeysFile] is read.
   * The characters in the file are interpreted as if you had typed them.
   * The same can be done with the command ":source! [inKeysFile]".
   * If the end of the file is reached before the editor exits, further characters are read from the keyboard.
   * It might be good idea to use [CleanMode] too. Does NOT work with -es or -Es ([ExScriptMode] or [ExImScriptMode]).
   */
  data class KeysScriptIn(val inKeysFile: Path) : XVimOpt, KOptS("s", inKeysFile.strf)
  // yes, the same letter "-s" as ScriptMode, but with argument (same letter even though for totally different modes).

  /**
   * All the characters that you type are recorded in the [outKeysFile], until you exit Vim.
   * This is useful if you want to create a script file to be used with "vim -s" or ":source!" [KeysScriptIn].
   *
   * Warning: Different settings, plugins etc. may mess up with recorded keys.
   * It's best to always use [CleanMode] with [KeysScriptOut], and it looks like NVim records better for me,
   * so prefer NVim (even if later "replaying" [KeysScriptIn] with normal Vim (like on CI or sth))
   * It can also be good idea to review recorded file and clean it up by hand??
   * And try to use basic keys while recording.
   *
   * If the [outKeysFile] file exists, characters are either appended (default),
   * or file is overwritten (when [overwrite] == true).
   */
  @DelicateApi("To avoid garbage it's best to use CleanMode (and NVim works better even if then replaying with Vim)")
  data class KeysScriptOut(val outKeysFile: Path, val overwrite: Boolean = false)
    : XVimOpt, KOptS(if (overwrite) "W" else "w", outKeysFile.strf) {
    val append: Boolean get() = !overwrite
  }

  /** Use encryption when writing files. Will prompt for a crypt key. */
  data object Encrypt : XVimOpt, KOptS("x")

  /**
   * Tells Vim the name of the terminal you are using. Only required when the automatic way doesn't work.
   * Should be a terminal known to Vim (builtin) or defined in the termcap or terminfo file.
   */
  @NotPortableApi("Vim only (not NVim).")
  data class TermName(val term: String) : XVimOpt, KOptS("T", term)

  /**
   * Give messages about which files are sourced and for reading and writing a viminfo file.
   * The default [verbosity] (when null) is the is 10.
   */
  data class Verbose(val verbosity: Int? = null) : XVimOpt, KOptS("V" + verbosity.strfoe)

  /**
   * Connect to a Vim server and make it edit the files given in the rest of the arguments.
   * If no server is found a warning is given and the files are edited in the current Vim.
   * You can specify the server name to connect to with [ServerName].
   */
  @NotPortableApi("Vim only (not NVim).")
  data object Remote : XVimOpt, KOptL("remote")

  /** As --remote [Remote], but without the warning when no server is found. */
  @NotPortableApi("Vim only (not NVim).")
  data object RemoteSilent : XVimOpt, KOptL("remote-silent")

  /** As --remote [Remote], but Vim does not exit until the files have been edited. */
  @NotPortableApi("Vim only (not NVim).")
  data object RemoteWait : XVimOpt, KOptL("remote-wait")

  /** As --remote-wait [RemoteWait], but without the warning when no server is found. */
  @NotPortableApi("Vim only (not NVim).")
  data object RemoteWaitSilent : XVimOpt, KOptL("remote-wait-silent")

  /** Connect to a Vim server, evaluate [expr] in it and print the result on stdout. */
  @NotPortableApi("Vim only (not NVim).")
  data class RemoteExpr(val expr: String) : XVimOpt, KOptL("remote-expr", expr, nameSeparator = " ")

  /** Connect to a Vim server and send [keys] to it. */
  @NotPortableApi("Vim only (not NVim).")
  data class RemoteSend(val keys: String) : XVimOpt, KOptL("remote-send", keys, nameSeparator = " ")

  /** List the names of all Vim servers that can be found. */
  @NotPortableApi("Vim only (not NVim).")
  data object ServerList : XVimOpt, KOptL("serverlist")

  /**
   * Use [server] as the server name. Used for the current Vim, unless used with a --remote argument [Remote],
   * then it's the name of the server to connect to.
   */
  @NotPortableApi("Vim only (not NVim).")
  data class ServerName(val server: String) : XVimOpt, KOptL("servername", server, nameSeparator = " ")

  /** GTK GUI only: Use the GtkPlug mechanism to run GVim in another window. */
  @NotPortableApi("Vim only (not NVim).")
  data class SocketId(val id: String) : XVimOpt, KOptL("socketid", id, nameSeparator = " ")

  /** During startup write timing messages to the [file]. */
  data class StartupTime(val file: Path) : XVimOpt, KOptL("startuptime", file.strf, nameSeparator = " ")

  @Suppress("FunctionName")
  companion object {

    val CursorLineLast = CursorLine(null)

    /**
     * For the first file the cursor will be positioned in given line and column.
     * Lines and columns are numbered from 1.
     * See also [CursorLine], [CursorLineLast], [CursorLineFind]
     */
    fun CursorPos(line: Int, column: Int) = ExCmd("call cursor($line,$column)")


    /** Special case of [VimInfo] */
    val VimInfoNONE = VimInfo("NONE")


    /** Special case of [VimRc] */
    val VimRcNONE = VimRc("NONE")

    /** Special case of [GVimRc] */
    @NotPortableApi("Vim only (not NVim).")
    val GVimRcNONE = GVimRc("NONE")

    // Some modes are known by names like "silent-mode", so let's add aliases for users knowing these names.

    // Note: This name for ScriptMode is here to stay mostly as some form of a documentation.
    /** Same as [ScriptMode] emphasizing that it's silencing behavior */
    @DelicateApi("Tricky to get always right with other options. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
    @NotPortableApi("Supported differently in Vim and NVim. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
    val SilentMode = ScriptMode

    // Note: this second name for ScriptMode is here to stay mostly as some form of a documentation.
    /** Same as [ExScriptMode] emphasizing that it's silencing behavior */
    val ExSilentMode = ExScriptMode

    // Note: this second name for ExImScriptMode is here to stay mostly as some form of a documentation.
    /** Same as [ExImScriptMode] emphasizing that it's silencing behavior */
    @DelicateApi("Tricky to get always right. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
    @NotPortableApi("Supported differently in Vim and NVim. Use ExScriptMode instead.", ReplaceWith("ExScriptMode"))
    val ExImSilentMode = ExImScriptMode


    /**
     * NVim interprets the "-" as stdin. Also in case of KeysScriptIn("-"), as in [KeysScriptStdInForNVim],
     * after stdin finishes, it starts getting actual keyboard from user (reopens tty?), so that's very useful.
     * On the other hand if we were using "-s /dev/stdin", as in [KeysScriptStdInForVim],
     * it could freeze, and the NVim process would have to be killed.
     * It did freeze when I tried experiment like this: printf 'iBLA\e:wq\n' | nvim -s /dev/stdin bla.txt
     * BTW looks like in that (incorrect) use-case, the NVim doesn't even run any given keys from /dev/stdin,
     * maybe it first try to read whole stdin "file", but /dev/stdin is never closing/ending?
     */
    @NotPortableApi("NVim interprets the \"-\" as stdin, but Vim doesn't and tries to open the \"-\" file.")
    val KeysScriptStdInForNVim = KeysScriptIn("-".pth)


    /**
     * Note: (if don't have NVim available) it's better to start experimenting with this using [GuiMode],
     * because gvim nicely allows user to continue using keyboard, and inspect what has happened.
     * Then when the keys are all good, add the :wq (or sth) to MAKE SURE it automatically quits,
     * and then maybe switch from gvim/[GuiMode] to just Vim.
     */
    @DelicateApi("Provided keys have to quit Vim at the end (if no GuiMode). Or Vim will do sth, show error and output some additional garbage.")
    @NotPortableApi("NVim interprets the \"-\" as stdin, but Vim doesn't. So /dev/stdin might work better in Vim.")
    val KeysScriptStdInForVim = KeysScriptIn("/dev/stdin".pth)
  }
}
