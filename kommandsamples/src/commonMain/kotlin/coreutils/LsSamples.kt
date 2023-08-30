package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.samples.*

private val lsCommon = "-1 --group-directories-first --color=never --escape"

data object LsSamples {
    val lsHelp = ls { -Help } s "ls --help"
    val lsVersion = ls { -Version } s "ls --version"
    val lsDefault = ls() s "ls"
    val lsDir = ls("blabla") s "ls $lsCommon --indicator-style=none blabla"
    val lsWithHidden = ls(".", withHidden = true) s "ls $lsCommon --indicator-style=none -A ."
    val lsParentWithSlashes = ls("..", style = IndicatorStyle.SLASH) s "ls $lsCommon --indicator-style=slash .."
    val lsParentSubDirs =
        lsSubDirs("..") rs
                "ls $lsCommon --indicator-style=slash .."
    val lsParentRegFiles =
        lsRegFiles("..") rs
                "ls $lsCommon --indicator-style=slash .."
                // same kommand line as above because difference is only in postprocessing: the "reduce" lambda
}