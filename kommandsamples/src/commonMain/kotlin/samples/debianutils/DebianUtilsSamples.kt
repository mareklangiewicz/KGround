package pl.mareklangiewicz.kommand.debianutils

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.samples.*


object DebianUtilsSamples {
    val Which = WhichSamples
}

object WhichSamples {
    val whichDpkg = which("dpkg") s "which dpkg"
    val whichDpkgAndApt = which("dpkg", "apt") s "which dpkg apt"
    val whichAllDpkgAndApt = which("dpkg", "apt", all = true) s "which -a dpkg apt"

    // TODO_someday: browser+executor UI for execs/wrappers; then add a similar list to other samples
    val execs = listOf(
        CliPlatform::isCommandAvailable,
        CliPlatform::isKommandAvailable,
        CliPlatform::whichOneExec,
    )
}