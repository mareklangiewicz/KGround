@file:Suppress("unused")

package pl.mareklangiewicz.kommand.admin

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.io.pth
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.debian.*
import pl.mareklangiewicz.kommand.samples.*


data object AdminSamples {
  val Sudo = SudoSamples
}

@OptIn(DelicateApi::class)
data object SudoSamples {
  val sudoVersion = sudo { -SudoOpt.Version } s "sudo --version"
  val sudoLsRoot = sudo(ls("/root".pth)) s "sudo -- ls /root"
  val sudoEditHosts = sudoEdit("/etc/hosts".pth) s "sudo --edit /etc/hosts"
  private val debFile = "discord-0.0.24.deb"
  val sudoDpkgInstallDiscord = dpkg(DpkgAct.Install(debFile)).withSudo() s "sudo -- dpkg -i $debFile"
}
