package pl.mareklangiewicz.kommand.debian

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.debian.DpkgAct.*
import pl.mareklangiewicz.kommand.samples.*


data object DebianSamples {
  val Which = WhichSamples
  val Dpkg = DpkgSamples
}

@OptIn(DelicateApi::class)
data object WhichSamples {
  val whichDpkg = which("dpkg") s "which dpkg"
  val whichDpkgAndApt = which("dpkg", "apt") s "which dpkg apt"
  val whichAllDpkgAndApt = which("dpkg", "apt", all = true) s "which -a dpkg apt"
  val whichDpkgFirstReduced = whichFirst("dpkg") rs "which dpkg"

  // Based on custom ReducedScript { ...try, catch, etc... } so can't infer original kommand.line, so expected null
  val whichDpkgFirstOrNullReduced = whichFirstOrNull("dpkg") rs null
  val isDpkgCommandAvailable = isCommandAvailable("dpkg") rs null
  val isDpkgKommandAvailable = isKommandAvailable(dpkg()) rs null
}

@OptIn(DelicateApi::class)
data object DpkgSamples {
  val dpkgSearchBinWhich = dpkg(Search("*bin*which*")) s "dpkg -S *bin*which*"
  val dpkgStatusDebianUtils = dpkg(Status("debianutils")) s "dpkg -l debianutils"
  val dpkgListFilesDebianUtils = dpkg(ListFiles("debianutils")) s "dpkg -L debianutils"
  val dpkgVerifyDebianUtils = dpkg(Verify("debianutils")) s "dpkg -V debianutils"
  val dpkgListPackagesDebian = dpkg(ListPackages("*debian*")) s "dpkg -l *debian*"

  // TODO_someday: browser+executor UI for scripts (ReducedScript) also!
  val searchPip = searchCommand("pip")
  val searchZenity = searchCommand("zenity")
}
