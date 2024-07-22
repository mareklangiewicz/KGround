@file:Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package pl.mareklangiewicz.kommand.ssh

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.core.LsOpt.*
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.term.*

@OptIn(DelicateApi::class)
data object SshSamples {

  val version = sshVersion() s "ssh -V"

  val sshPimInTermGnome = ssh("pim").inTermGnome() s "gnome-terminal -- ssh pim"

  val sshPimInTermKitty = ssh("pim").inTermKitty() s "kitty --detach -- ssh pim"

  val sshPimLsInTermKitty = ssh("pim", "ls").inTermKitty(hold = true) s "kitty --detach --hold -- ssh pim ls"

  val sshPimLsLAH = ssh("pim", ls { -LongFormat; -All; -HumanReadable }) s "ssh pim ls -l -a -h"
}

// TODO NOW: Continue, it's first experiment based on:
// https://www.codejam.info/2021/11/standalone-userland-ssh-server.html
@OptIn(DelicateApi::class)
suspend fun runSshdStandalone(dir: String = "/tmp/sshd-sdandalone") {
  mkdir(dir, withParents = true).ax()
  TODO()

}

