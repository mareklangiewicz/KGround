package pl.mareklangiewicz.kommand.debian

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.debian.DpkgAct.*
import pl.mareklangiewicz.kommand.samples.*
import kotlin.reflect.*


data object DebianSamples {
    val Which = WhichSamples
    val Dpkg = DpkgSamples
}

@OptIn(DelicateApi::class)
data object WhichSamples {
    val whichDpkg = which("dpkg") s "which dpkg"
    val whichDpkgAndApt = which("dpkg", "apt") s "which dpkg apt"
    val whichAllDpkgAndApt = which("dpkg", "apt", all = true) s "which -a dpkg apt"
    val whichDpkgFirstReduced = whichFirstOrNull("dpkg") rs "which dpkg"
    val isDpkgCommandAvailable = isCommandAvailable("dpkg") rs "which dpkg"
    val isDpkgKommandAvailable = isKommandAvailable(dpkg()) rs "which dpkg"
}

@OptIn(DelicateApi::class)
data object DpkgSamples {
    val dpkgSearchBinWhich = dpkg(Search("*bin*which*")) s "dpkg -S *bin*which*"
    val dpkgStatusDebianUtils = dpkg(Status("debianutils")) s "dpkg -l debianutils"
    val dpkgListFilesDebianUtils = dpkg(ListFiles("debianutils")) s "dpkg -L debianutils"
    val dpkgVerifyDebianUtils = dpkg(Verify("debianutils")) s "dpkg -V debianutils"
    val dpkgListPackagesDebian = dpkg(ListPackages("*debian*")) s "dpkg -l *debian*"

    // TODO_someday: browser+executor UI for scripts (ReducedScript) also!
    val searchZenity = searchCommand("zenity")
}
