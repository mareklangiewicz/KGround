package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.interactive.*
import pl.mareklangiewicz.kground.io.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.core.LsOpt.ColorType.*
import pl.mareklangiewicz.kommand.core.LsOpt.IndicatorStyle.*
import pl.mareklangiewicz.kommand.vim.*
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.term.*

@OptIn(DelicateApi::class)
data object LsSamples {

  val lsHelp = ls { -Help } s
    "ls --help"

  val lsVersion = ls { -Version } s
    "ls --version"

  val lsEtcDir = ls("/etc".pth) s
    "ls /etc"

  val lsWithHidden = ls(".".pth, wHidden = true) s
    "ls -A ."

  val lsParentWithSlashes = ls("..".pth, wIndicator = Slash) s
    "ls --indicator-style=slash .."

  val lsParentSubDirs = lsSubDirs("..".pth) rs
    "ls --indicator-style=slash .."

  // same kommand line as above because difference is only in postprocessing: the "reduce" lambda
  val lsParentRegFiles = lsRegFiles("..".pth) rs
    "ls --indicator-style=slash .."

  val lsALot1KSizes = ls("/home/marek".pth, "/usr".pth, wHidden = true) { -Size; -Block1K } s
    "ls -A -s -k /home/marek /usr"

  val lsALotNicely = ls("/home/marek".pth, "/usr".pth, wHidden = true, wColor = Always) {
    -Author; -LongFormat; -BlockHuman; -Sort(SortType.Time)
  } s
    "ls -A --color=always --author -l -h --sort=time /home/marek /usr"

  // Notice: it should add colors because "ls" is called with terminal as stdout
  val lsALotNicelyInTerm = lsALotNicely.inTermKitty(hold = true) s
    "kitty --detach --hold -- ls -A --color=always --author -l -h --sort=time /home/marek /usr"

  // Notice: it will NOT add colors because "ls" is called with file as stdout
  val lsALotNicelyInGVim = InteractiveScript {
    val fs = localUFileSys()
    val notes = fs.pathToTmpNotes
    lsALotNicely.ax(outFile = notes)
    gvim(notes).ax()
  }
}
