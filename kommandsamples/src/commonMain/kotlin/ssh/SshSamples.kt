@file:Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package pl.mareklangiewicz.kommand.ssh

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.samples.*
import pl.mareklangiewicz.kommand.term.*

@OptIn(DelicateKommandApi::class)
data object SshSamples {

    val version =
        sshVersion() s
                "ssh -V"

    val sshPimInTermGnome =
        termGnome(ssh("pim")) s
                "gnome-terminal -- ssh pim"

    val sshPimInTermKitty =
        termKitty(ssh("pim")) s
                "kitty -1 --detach -- ssh pim"

    val sshPimLsInTermKitty =
        termKitty(ssh("pim", "ls"), hold = true) s
                "kitty -1 --detach --hold -- ssh pim ls"

    val sshPimLsLAH =
        ssh("pim", ls { -LsOpt.LongFormat; -LsOpt.All; -LsOpt.HumanReadable }) s
                "ssh pim ls -l -a -h"
}

