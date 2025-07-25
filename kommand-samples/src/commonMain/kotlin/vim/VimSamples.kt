package pl.mareklangiewicz.kommand.vim

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.echo
import pl.mareklangiewicz.kommand.find.myKGroundPath
import pl.mareklangiewicz.kommand.find.myTmpPath
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.shell.bashPipe
import pl.mareklangiewicz.kommand.term.TermKittyOpt.StartAsType
import pl.mareklangiewicz.kommand.term.inTermKitty
import pl.mareklangiewicz.kommand.vim.XVimOpt.*
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.CursorLineLast
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.CursorPos
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.KeysScriptStdInForNVim
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.KeysScriptStdInForVim
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.VimTermDumb

val blas = listOf("bla", "ble", "blu", "bli", "blo")
val blaS = blas.asFlow()
val blaSlowS = blaS.map { delay(1000); it }

@OptIn(DelicateApi::class)
data object VimBasicSamples {

  val vimHelp = vim { -Help } s "vim --help"

  val vimVersion = vim { -Version } s "vim --version"

  val nvimVersion = nvim { -Version } s "nvim --version"

  val nvimVerboseVersion = nvim { -Verbose(); -Version } s "nvim -V --version"

  val nvimManVim = nvimMan("vim") s "nvim -c hid Man vim"
    // Note: it looks like lacking additional quotes around "hid Man vim",
    // but the "hid Man vim" is passed as ONE argument to nvim so it's fine (no bash/shell is used here)

  val nvimManManInFullTermKitty = nvimMan("man").inTermKitty(startAs = StartAsType.FullScreen)
    // Note: here we're wrapping in kitty explicitly (in fullscreen, without hold),
    // but sample nvimManVim will also use kitty if run via Main.kt:main:try-code (by default, with hold = true)

  val gvimBlaContent = gvimContent(blas.joinToString("\n"))

  val gvimBlas = gvimLines(blas)

  val gvimBlaS = gvimLineS(blaS)

  /** GVim will display 'reading from stdin...' until it reads full flow and at the end show the full content */
  val gvimBlaSlowS = gvimLineS(blaSlowS)

  val gvimBlaSCursorLineBlu = gvimLineS(blaS) { -CursorLineFind("blu") }

  /** Lines are numbered from 1 */
  val gvimBlaSCursorLine2 = gvimLineS(blaS) { -CursorLine(2) }

  val gvimBlaSCursorLineLast = gvimLineS(blaS) { -CursorLineLast }

  /** Lines and columns are numbered from 1 */
  val gvimBlaSCursorPos32 = gvimLineS(blaS) { -CursorPos(3,2) }

  // Note: nulls as files are ignored
  val gvimOpenBashRcAndVimRcCursorPos32 = gvimOpen("~/.bashrc".P, null, "~/.vimrc".P, line = 3, column = 2) s
    "gvim -c call cursor(3,2) ~/.bashrc ~/.vimrc"
}



@DelicateApi("Many are changing files, like KGround:build.gradle.kts (e.g. to bump version automatically)")
data object VimAdvancedSamples {

  // FIXME_later: use UFileSys.pathXXX
  private val myKGroundRootBuildFile = "$myKGroundPath/build.gradle.kts".P
  private val myTmpKeysFile = "$myTmpPath/tmp.keys.vim".P

  val gvimBuildGradleCursorFindVer = gvim(myKGroundRootBuildFile) { -CursorLineFind("version = Ver(.*)") }

  /** Gvim will have all default look&feel (usually white background, small window, graphical menu, etc) */
  val gvimBlaSlowSCleanMode = gvimLineS(blaSlowS) { -CleanMode }

  // TODO_someday: better kitty integration (starting in existing kitty in new window/tab/etc..)
  val nvimInKittyBashRc = nvim("/home/marek/.bashrc".P).inTermKitty()



  // Note: I had strange issues with GVim. Prefer NVim in CleanMode!
  // (especially when recording but CleanMode when replying is also recommended)
  // TODO: I can use nvim because my setup run kommands in kitty terminal anyway, but it's implicit and it can change;
  //   so better to use other more explicit method (some proper nvim kitty wrapper fun knvim??)
  val nvimBuildGradleRecord = nvim(myKGroundRootBuildFile) { -CleanMode; -KeysScriptOut(myTmpKeysFile, overwrite = true) } s
    "nvim --clean -W $myTmpKeysFile $myKGroundRootBuildFile"

  val nvimShowRecord = nvim(myTmpKeysFile)

  val nvimBuildGradleReplay = nvim(myKGroundRootBuildFile) { -KeysScriptIn(myTmpKeysFile) } s
    "nvim -s $myTmpKeysFile $myKGroundRootBuildFile"

  /**
   * Bumps version in KGround:build.gradle.kts using gvim, and leave gvim open without saving, to inspect.
   * The last ":execute" / ":exe" [ExCmd] is needed because the problem with entering <C-A> in vim cmd-line
   */
  val gvimBumpVerImpl1ExSc =
    gvim(myKGroundRootBuildFile) { -CursorLineFind("version = Ver(.*)"); -ExCmd("norm t)"); -ExCmd("exe \"norm \\<C-A>\"") } s
      "gvim +/version = Ver(.*) -c norm t) -c exe \"norm \\<C-A>\" $myKGroundRootBuildFile"
    // Note: this :exe (:execute) complication is there only due to the problem with entering <C-A> key in commandline

  /** Better impl of [gvimBumpVerImpl1ExSc] */
  val gvimBumpVerImpl2ExSc = gvim(myKGroundRootBuildFile) { -ExCmd("g/version = Ver(.*)/exe \"norm t)\\<C-A>\"") } s
    "gvim -c g/version = Ver(.*)/exe \"norm t)\\<C-A>\" $myKGroundRootBuildFile"

  /** Pretty good impl of bumping versions in scripts using ex-script (but keys-scripts are even more awesome?). */
  val vimBumpVerImpl3ExSc =
    vimExScriptStdIn(myKGroundRootBuildFile).reducedToLists("g/version = Ver(.*)/exe \"norm t)\\<C-A>ZZ\"") rs
    "vim -N --clean -es $myKGroundRootBuildFile" // BTW reducedToLines puts ex-script line to stdin
  // BTW nvim would also work here (-es also takes script in stdin)
  // (But -Es in nvim flips stuff and takes buffer content as stdin; BTW in nvim both -es and -Es are Ex-IMPROVED mode)
  // To run this sample manually in terminal (from KGround working dir) (-V just to see more):
  // printf 'g/version = Ver(.*)/exe "norm t)\<C-A>ZZ"\n' | vim -V -N --clean -es build.gradle.kts
  // printf 'g/version = Ver(.*)/exe "norm t)\<C-A>ZZ"\n' | nvim -V -N --clean -es build.gradle.kts
  // Update: nvim version of the same:
  val nvimBumpVerImpl3ExSc =
    nvimExScriptStdIn(myKGroundRootBuildFile).reducedToLists("g/version = Ver(.*)/exe \"norm t)\\<C-A>ZZ\"") rs
      "nvim -N --clean -es $myKGroundRootBuildFile"

  private const val keyCtrlA = '\u0001' // to increase number at cursor in vim

  /** Impl for experiments with KeysScripts with gvim */
  @NotPortableApi("Not for NVim")
  val gvimBumpVerImpl4KeysSc = gvim(myKGroundRootBuildFile) { -KeysScriptStdInForVim }.reducedToLists(
    "/version = Ver",
    "t)$keyCtrlA", // no :wq so I can inspect result in gvim visually/manually
    // "t)$keyCtrlA:wq", // WARN: each input line ends with \n, so this version WILL do :wq
  ) rs "gvim -s /dev/stdin $myKGroundRootBuildFile"

  /**
   * Kinda fragile impl of bumping versions in scripts using keys-script in original Vim.
   * -T dumb is needed (I tried many experiments and it helps; BTW --not-a-term makes it worse)
   * Looks like -V breaks stuff, so don't try it (vim confused about terminal or sth)
   * WARN: Also make sure there is no .build.gradle.kts.swp left after some old crash,
   * or it will fail again mysteriously..
   */
  @NotPortableApi("Not for NVim")
  val vimBumpVerImpl5KeysSc =
    vim(myKGroundRootBuildFile) { -CleanMode; -SwapNONE; -VimTermDumb; -KeysScriptStdInForVim }
      .reducedToLists(
        "/version = Ver",
        // "t)$keyCtrlA", // WARN: This is WRONG! no :wq so it expects terminal and CRASHES
        "t)$keyCtrlA:wq", // WARN: Each input line ends with \n, so this version WILL do :wq (which is needed for it to work at all)
        ignoreOut = true, // normally outputs file content with color codes, so garbage.
      ) rs "vim --clean -n -T dumb -s /dev/stdin $myKGroundRootBuildFile"

  /**
   * More stable impl of bumping versions in scripts using keys-script in NVim.
   * WARN: Also make sure there is no build.gradle.kts swap file left in ~/.local/state/nvim/swap/
   * left after some old crash, or it can fail weirdly.. (doing wrong things because of some notif or sth)
   */
  @NotPortableApi("Not for original Vim")
  val nvimBumpVerImpl6KeysSc =
    nvim(myKGroundRootBuildFile) { -CleanMode; -SwapNONE; -KeysScriptStdInForNVim }
      .reducedToLists(
        "/version = Ver",
        // "t)$keyCtrlA", // WARN: This is WRONG! no :wq so nvim hangs! (waiting for user, but I run it in context without terminal)
        "t)$keyCtrlA:wq", // WARN: Each input line ends with \n, so this version WILL do :wq (which is needed for it to work at all)
        ignoreOut = true, // I think NVim does NOT output garbage in this case, but let's ignore unwanted output anyway.
      )  rs "nvim --clean -n -s - $myKGroundRootBuildFile"


  // TODO: support awesome new nvim options like -l !! Allows to use nvim as superpowered lua interpreter!
  //   https://neovim.io/doc/user/starting.html#-l
  //   see also Folke comment: https://www.reddit.com/r/neovim/comments/1dr5152/testing_your_neovim_plugins_with_busted_and/


  /** Pretty good impl of bumping versions in scripts using keys-script. */
  @NotPortableApi("Not for NVim")
  val vimBumpVerImpl7KeysSc = vimKeysScriptContent("/version = Ver\nt)$keyCtrlA:wq\n", myKGroundRootBuildFile) rs
    "vim -N --clean -n -c set nomore -T dumb -s /dev/stdin $myKGroundRootBuildFile"

  /**
   * Example of using -Es in nvim (see in NVim :h vim_diff.txt/Startup)
   * Usually I don't recommend -Es (ExImScriptMode), but in NeoVim it's useful when you know what you're doing.
   *
   * BTW simplest example/explanation on youtube: "We can have nice things" by Justin M Keyes at 39:40:
   * https://youtu.be/Bt-vmPC_-Ho?si=1lBmf5NhJ3juoi25&t=2390
   * $ echo foo | nvim -Es +"%p" | tr o x
   * fxx
   */
  @NotPortableApi("Only for nvim.")
  val nvimStyleProcessor = nvim {
    -ExImScriptMode // In NeoVim this mode have good modern defaults for using inside shell pipes.
    -ExCmd("%s/nvim/NVim")
    -ExCmd("%s/\\<Vim/vim")
    // we can use vim style regular expressions obviously
    // we can also use whole nvim power obviously
    -ExCmd("%p") // print it all to stdout
    // Note: no explicit quitting (-c "qa!") needed here in neovim
  } s "nvim -Es -c %s/nvim/NVim -c %s/\\<Vim/vim -c %p"

  @NotPortableApi
  val bashPipeWithNvimProcessor = bashPipe(echo("Vim vim nvim NVim"), nvimStyleProcessor) s "bash -c echo Vim\\ vim\\ nvim\\ NVim | nvim -Es -c %s/nvim/NVim -c %s/\\\\\\<Vim/vim -c %p"
}
