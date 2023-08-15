package pl.mareklangiewicz.kommand.ssh

import pl.mareklangiewicz.kommand.*


@DelicateKommandApi
fun ssh(destination: String, command: String? = null, vararg options: SshOpt) =
    ssh(destination, command) { opts.addAll(options) }

@DelicateKommandApi
fun ssh(destination: String, command: String? = null, init: Ssh.() -> Unit = {}) =
    Ssh().apply { +destination; command?.let { +it }; init() }

/**
 * [openssh homepage](https://www.openssh.com/)
 * [openssh manuals](https://www.openssh.com/manual.html)
 * [openbsd man ssh](https://man.openbsd.org/ssh)
 * [linux man ssh](https://man7.org/linux/man-pages/man1/ssh.1.html)
 */
@DelicateKommandApi
class Ssh(
    override val opts: MutableList<SshOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
): KommandTypical<SshOpt> {
    override val name get() = "ssh"
}


@DelicateKommandApi
interface SshOpt: KOptTypical {
    data object IpV4: KOptS("4"), SshOpt
    data object IpV6: KOptS("6"), SshOpt
    data class AuthAgentForwarding(val enable: Boolean): KOptS(if (enable) "A" else "a"), SshOpt
    data class ConfigOption(val option: String): KOptS("o", option), SshOpt
    data class LoginName(val user: String): KOptS("l", user), SshOpt
    data class Port(val port: Int): KOptS("p", port.toString()), SshOpt
    data object Quiet: KOptS("q"), SshOpt
}
