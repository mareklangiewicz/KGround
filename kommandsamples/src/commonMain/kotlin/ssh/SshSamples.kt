@file:Suppress("unused", "SpellCheckingInspection", "MemberVisibilityCanBePrivate")

package pl.mareklangiewicz.kommand.ssh

import pl.mareklangiewicz.kommand.*
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
                "kitty -1 -- ssh pim"

    val sshPimLsInTermKitty =
        termKitty(ssh("pim", "ls"), hold = true) s
                "kitty -1 --hold -- ssh pim ls"

    val sshPimLsLAH =
        ssh("pim", "ls -lah") s
                "ssh pim ls"
}

