@file:Suppress("unused")

package pl.mareklangiewicz.kommand.admin

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.debian.*
import pl.mareklangiewicz.kommand.samples.*
import kotlin.reflect.*


data object AdminSamples {
    val Sudo = SudoSamples
}

@OptIn(DelicateKommandApi::class)
data object SudoSamples {
    val sudoVersion = sudo { -SudoOpt.Version } s "sudo --version"
    val sudoLsRoot = sudo(lsDefault("/root/")) s "sudo -- ls /root/"
    val sudoEditHosts = sudoEdit("/etc/hosts") s "sudo --edit /etc/hosts"
    private val debFile = "discord-0.0.24.deb"
    val sudoDpkgInstallDiscord = dpkg(DpkgAct.Install(debFile)).withSudo() s "sudo -- dpkg -i $debFile"

    // TODO_someday: browser+executor UI for execs/wrappers; then add a similar list to other samples
    val execs: List<KFunction<*>> = listOf(
        CliPlatform::sudoExec,
    )
}
