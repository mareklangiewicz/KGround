package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.InteractiveScript
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.core.LsOpt.ColorType.*
import pl.mareklangiewicz.kommand.core.LsOpt.IndicatorStyle.*
import pl.mareklangiewicz.kommand.core.LsOpt.SortType.*
import pl.mareklangiewicz.kommand.gvim
import pl.mareklangiewicz.kommand.pathToTmpNotes
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.term.*

@OptIn(DelicateApi::class)
data object LsSamples {

    val lsHelp = ls { -Help } s
            "ls --help"

    val lsVersion = ls { -Version } s
            "ls --version"

    val lsEtcDir = ls("/etc") s
            "ls /etc"

    val lsWithHidden = ls(".", wHidden = true) s
            "ls -A ."

    val lsParentWithSlashes = ls("..", wIndicator = SLASH) s
            "ls --indicator-style=slash .."

    val lsParentSubDirs = lsSubDirs("..") rs
            "ls --indicator-style=slash .."

    // same kommand line as above because difference is only in postprocessing: the "reduce" lambda
    val lsParentRegFiles = lsRegFiles("..") rs
            "ls --indicator-style=slash .."

    val lsALotNicely = ls("/home/marek", "/usr", wHidden = true, wColor = ALWAYS) {
        -Author; -LongFormat; -HumanReadable; -Sort(TIME) } s
            "ls -A --color=always --author -l -h --sort=time /home/marek /usr"

    // Notice: it should add colors because "ls" is called with terminal as stdout
    val lsALotNicelyInTerm = termKitty(lsALotNicely, hold = true) s
            "kitty -1 --detach --hold -- ls -A --color=always --author -l -h --sort=time /home/marek /usr"

    // Notice: it will NOT add colors because "ls" is called with file as stdout
    val lsALotNicelyInGVim = InteractiveScript { cli ->
        lsALotNicely.ax(cli, outFile = cli.pathToTmpNotes)
        gvim(cli.pathToTmpNotes).ax(cli)
    }
}