@file:Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package pl.mareklangiewicz.kommand.ssh

import pl.mareklangiewicz.annotations.DelicateApi
import okio.Path
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.term.*

@OptIn(DelicateApi::class)
data object SshSamples {

  val version = sshVersion() s "ssh -V"

  val sshPimInTermGnome = ssh("mypi").inTermGnome() s "gnome-terminal -- ssh mypi"

  val sshPimInTermKitty = ssh("mypi").inTermKitty() s "kitty --detach -- ssh mypi"

  val sshPimLsInTermKitty = ssh("mypi", "ls").inTermKitty(hold = true) s "kitty --detach --hold -- ssh mypi ls"

  val sshPimLsLAH = ssh("mypi", ls { -LongFormat; -All; -BlockHuman }) s "ssh mypi ls -l -a -h"
}

// TODO NOW: Continue, it's first experiment based on:
// https://www.codejam.info/2021/11/standalone-userland-ssh-server.html
@OptIn(DelicateApi::class)
suspend fun runSshdStandalone(dir: Path = "/tmp/sshd-sdandalone".P) {
  mkdir(dir, withParents = true).ax()
  TODO()

}
