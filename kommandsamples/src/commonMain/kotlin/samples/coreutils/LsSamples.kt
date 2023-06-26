package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.samples.s
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.LsOpt.*

private val lsCommon = "-1 --group-directories-first --color=never --escape"

object LsSamples {
    val lsHelp = ls { -Help } s "ls --help"
    val lsVersion = ls { -Version } s "ls --version"
    val lsDefault = ls() s "ls"
    val lsDir = ls("blabla") s "ls $lsCommon --indicator-style=none blabla"
    val lsWithHidden = ls(".", withHidden = true) s "ls $lsCommon --indicator-style=none -A ."
    val lsWithSlashes = ls(".", style = IndicatorStyle.SLASH) s "ls $lsCommon --indicator-style=slash ."

    // TODO_someday: browser+executor UI for execs/wrappers; then add a similar list to other samples
    val execs = listOf(
        CliPlatform::lsExec,
        CliPlatform::lsRegFilesExec,
        CliPlatform::lsSubDirsExec,
    )
}