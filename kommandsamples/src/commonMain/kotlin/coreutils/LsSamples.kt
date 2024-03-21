package pl.mareklangiewicz.kommand.core

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.samples.*

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

    val lsParentWithSlashes = ls("..", wIndicator = IndicatorStyle.SLASH) s
            "ls --indicator-style=slash .."

    val lsParentSubDirs = lsSubDirs("..") rs
            "ls --indicator-style=slash .."

    // same kommand line as above because difference is only in postprocessing: the "reduce" lambda
    val lsParentRegFiles = lsRegFiles("..") rs
            "ls --indicator-style=slash .."
}