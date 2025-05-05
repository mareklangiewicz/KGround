package pl.mareklangiewicz.kommand.vim

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.find.myKGroundPath
import pl.mareklangiewicz.kommand.find.myTmpPath
import pl.mareklangiewicz.kommand.reducedManually
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.term.TermKittyOpt.StartAsType
import pl.mareklangiewicz.kommand.term.inTermKitty
import pl.mareklangiewicz.kommand.vim.XVimOpt.*
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.CursorLineLast
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.CursorPos
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.KeysScriptStdInForNVim
import pl.mareklangiewicz.kommand.vim.XVimOpt.Companion.KeysScriptStdInForVim

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
  val nvimBuildGradleRecord = nvim(myKGroundRootBuildFile) { -CleanMode; -KeysScriptOut(myTmpKeysFile, overwrite = true) }

  val nvimShowRecord = nvim(myTmpKeysFile)

  val nvimBuildGradleReplay = nvim(myKGroundRootBuildFile) { -KeysScriptIn(myTmpKeysFile) }

  /**
   * Bumps version in KGround:build.gradle.kts using gvim, and leave gvim open without saving, to inspect
   * The last ":execute" / ":exe" [ExCmd] is needed because the problem with entering <C-A> in vim cmd-line
   */
  val gvimBumpVerImpl1ExSc =
    gvim(myKGroundRootBuildFile) { -CursorLineFind("version = Ver(.*)"); -ExCmd("norm t)"); -ExCmd("exe \"norm \\<C-A>\"") } s
      "gvim +/version = Ver(.*) -c norm t) -c exe \"norm \\<C-A>\" $myKGroundRootBuildFile"
    // Note: this :exe (:execute) complication is there only due to the problem with entering <C-A> key in commandline

  /** Better impl of [gvimBumpVerImpl1ExSc] */
  val gvimBumpVerImpl2ExSc = gvim(myKGroundRootBuildFile) { -ExCmd("g/version = Ver(.*)/exe \"norm t)\\<C-A>\"") } s
    "gvim -c g/version = Ver(.*)/exe \"norm t)\\<C-A>\" $myKGroundRootBuildFile"

  /** Pretty good impl of bumping versions in scripts using ex-script (but keys-scripts are more awesome). */
  val vimBumpVerImpl3ExSc = vimExScriptContent("g/version = Ver(.*)/exe \"norm t)\\<C-A>ZZ\"", myKGroundRootBuildFile) rs
    "vim -N --clean -es $myKGroundRootBuildFile"

  private const val keyCtrlA = '\u0001' // to increase number at cursor in vim

  /** Impl for experiments with KeysScripts with gvim */
  @NotPortableApi("Not for NVim")
  val gvimBumpVerImpl4KeysSc = ReducedScript {
    val inContent = "/version = Ver\nt)$keyCtrlA"
    // val inContent = "/version = Ver\nt)$keyCtrlA:wq"
    // Warning: ax always ends inContent with \n, so it WILL do :wq
    gvim(myKGroundRootBuildFile) { -KeysScriptStdInForVim }.ax(inContent = inContent)
  }

  /** Pretty good impl of bumping versions in scripts using keys-script. */
  @NotPortableApi("Not for NVim")
  val vimBumpVerImpl5KeysSc = vim(myKGroundRootBuildFile) { -KeysScriptStdInForVim }.reducedManually {
    stdin.collect(flowOf("/version = Ver\nt)$keyCtrlA:wq"))
    // Warning: collect always ends with \n (by default), so it WILL do :wq
    awaitAndChkExit(firstCollectErr = true)
  } rs "vim -s /dev/stdin $myKGroundRootBuildFile"

  @NotPortableApi("Not for original Vim")
  val nvimBumpVerImpl6KeysSc = nvim(myKGroundRootBuildFile) { -KeysScriptStdInForNVim }.reducedManually {
    stdin.collect(flowOf("/version = Ver\nt)$keyCtrlA:wq"))
    // Warning: collect always ends with \n (by default), so it WILL do :wq
    awaitAndChkExit(firstCollectErr = true)
  } rs "nvim -s - $myKGroundRootBuildFile"

  // TODO: support awesome new nvim options like -l !! Allows to use nvim as superpowered lua interpreter!
  //   https://neovim.io/doc/user/starting.html#-l
  //   see also Folke comment: https://www.reddit.com/r/neovim/comments/1dr5152/testing_your_neovim_plugins_with_busted_and/


  /** Pretty good impl of bumping versions in scripts using keys-script. */
  @NotPortableApi("Not for NVim")
  val vimBumpVerImpl7KeysSc = vimKeysScriptContent("/version = Ver\nt)$keyCtrlA:wq\n", myKGroundRootBuildFile)rs
    "vim -N --clean -n -c set nomore -T dumb -s /dev/stdin $myKGroundRootBuildFile"
}
