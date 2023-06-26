package pl.mareklangiewicz.kommand.debian

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.samples.*
import kotlin.reflect.*


object DebianSamples {
    val Which = WhichSamples
}

object WhichSamples {
    val whichDpkg = which("dpkg") s "which dpkg"
    val whichDpkgAndApt = which("dpkg", "apt") s "which dpkg apt"
    val whichAllDpkgAndApt = which("dpkg", "apt", all = true) s "which -a dpkg apt"

    // TODO_someday: browser+executor UI for execs/wrappers; then add a similar list to other samples
    val execs: List<KFunction<*>> = listOf(
        CliPlatform::isCommandAvailable,
        CliPlatform::isKommandAvailable,
        CliPlatform::whichOneExec,
    )
}