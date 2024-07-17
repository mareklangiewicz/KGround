package pl.mareklangiewicz.kommand.vim

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.vim.XVim.Option.*
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.term.termKitty

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

  val gvimBlaContent = gvimStdIn(blas.joinToString("\n"))

  val gvimBlas = gvimStdIn(blas)

  val gvimBlaS = gvimStdIn(blaS)

  /** GVim will display 'reading from stdin...' until it reads full flow and at the end show the full content */
  val gvimBlaSlowS = gvimStdIn(blaSlowS)

  val gvimBlaSCursorLineBlu = gvimStdIn(blaS) { -CursorLineFind("blu") }

  /** Lines are numbered from 1 */
  val gvimBlaSCursorLine2 = gvimStdIn(blaS) { -CursorLine(2) }

  val gvimBlaSCursorLineLast = gvimStdIn(blaS) { -CursorLineLast }

  /** Gvim will have all default look&feel (usually white background, small window, graphical menu, etc) */
  val gvimBlaSlowSCleanMode = gvimStdIn(blaSlowS) { -CleanMode }

  val nvimInKittyBashRc = termKitty(nvim("~/.bashrc"))

}
