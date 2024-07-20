package pl.mareklangiewicz.kommand.vim

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.ReducedScript
import pl.mareklangiewicz.kommand.find.myKommandLinePath
import pl.mareklangiewicz.kommand.find.myTmpPath
import pl.mareklangiewicz.kommand.reducedManually
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.term.termKitty
import pl.mareklangiewicz.kommand.vim.XVim.Option.*
import pl.mareklangiewicz.kommand.vim.XVim.Option.Companion.KeysScriptStdInForNVim
import pl.mareklangiewicz.kommand.vim.XVim.Option.Companion.KeysScriptStdInForVim

val blas = listOf("bla", "ble", "blu", "bli", "blo")
val blaS = blas.asFlow()
val blaSlowS = blaS.map { delay(1000); it }

@OptIn(DelicateApi::class)
data object VimSamples {

  val vimHelp = vim { -Help } s
    "vim -h"

  val vimVersion = vim { -Version } s
    "vim --version"

  val nvimVersion = nvim { -Version } s
    "nvim --version"

  val nvimVerboseVersion = nvim { -Verbose(); -Version } s
    "nvim -V --version"

  val gvimBlaContent = gvimContent(blas.joinToString("\n"))

  val gvimBlas = gvimLines(blas)

  val gvimBlaS = gvimLineS(blaS)

  /** GVim will display 'reading from stdin...' until it reads full flow and at the end show the full content */
  val gvimBlaSlowS = gvimLineS(blaSlowS)

  val gvimBlaSCursorLineBlu = gvimLineS(blaS) { -CursorLineFind("blu") }

  /** Lines are numbered from 1 */
  val gvimBlaSCursorLine2 = gvimLineS(blaS) { -CursorLine(2) }

  val gvimBlaSCursorLineLast = gvimLineS(blaS) { -CursorLineLast }

  // FIXME_later: use UFileSys.pathXXX, and generally use Path type
  private val myBuildFile = "$myKommandLinePath/build.gradle.kts"
  private val myTmpKeysFile = "$myTmpPath/tmp.keys.vim"

  val gvimBuildGradleCursorFindVer = gvim(myBuildFile) { -CursorLineFind("version = Ver(.*)") }

  /** Gvim will have all default look&feel (usually white background, small window, graphical menu, etc) */
  val gvimBlaSlowSCleanMode = gvimLineS(blaSlowS) { -CleanMode }

  // TODO: better kitty integration (starting in existing kitty in new window/tab/etc..)
  val nvimInKittyBashRc = termKitty(nvim("~/.bashrc"))



  // Note: I had strange issues with GVim. Prefer NVim in CleanMode!
  // (especially when recording but CleanMode when replying is also recommended)
  // TODO: I can use nvim because my setup run kommands in kitty terminal anyway, but it's implicit and it can change;
  //   so better to use other more explicit method (some proper nvim kitty wrapper fun knvim??)
  val nvimBuildGradleRecord = nvim(myBuildFile) { -CleanMode; -KeysScriptOut(myTmpKeysFile, overwrite = true) }

  val nvimShowRecord = nvim(myTmpKeysFile)

  val nvimBuildGradleReplay = nvim(myBuildFile) { -KeysScriptIn(myTmpKeysFile) }




  @DelicateApi
  val gvimBuildGradleBumpVer1 =
    gvim(myBuildFile) { -CursorLineFind("version = Ver(.*)"); -ExCmd("norm t)"); -ExCmd("exe \"norm \\<C-A>\"") }
    // Note: this :exe (:execute) complication is there only due to the problem with entering <C-A> key in commandline

  @DelicateApi
  val gvimBuildGradleBumpVer2 =
    gvim(myBuildFile) { -ExCmd("g/version = Ver(.*)/exe \"norm t)\\<C-A>\"") }

  @DelicateApi
  val vimExScriptBuildGradleBumpVer3 = vimExScriptContent("g/version = Ver(.*)/exe \"norm t)\\<C-A>ZZ\"", myBuildFile)

  private const val keyCtrlA = '\u0001' // to increase number at cursor in vim

  @NotPortableApi @DelicateApi
  val gvimKeyScriptBuildGradleBumpVer4 = ReducedScript {
    gvim(myBuildFile) { -KeysScriptStdInForVim }.ax(inContent = "/version = Ver\nt)$keyCtrlA:wq")
  }

  @NotPortableApi @DelicateApi
  val vimKeyScriptBuildGradleBumpVer5 = vim(myBuildFile) { -KeysScriptStdInForVim }.reducedManually {
    stdin.collect(flowOf("/version = Ver\nt)$keyCtrlA:wq"))
    awaitAndChkExit(firstCollectErr = true)
  }

  @NotPortableApi @DelicateApi
  val nvimKeyScriptBuildGradleBumpVer6 = nvim(myBuildFile) { -KeysScriptStdInForNVim }.reducedManually {
    stdin.collect(flowOf("/version = Ver\nt)$keyCtrlA:wq"))
    awaitAndChkExit(firstCollectErr = true)
  }
}
