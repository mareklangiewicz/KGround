@file:Suppress("unused")

package pl.mareklangiewicz.kommand.admin

import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.admin.SudoOpt.*

fun CliPlatform.sudoExec(k: Kommand, asUser: String? = null, inPass: String? = null, vararg options: SudoOpt) =
    sudo(k, *options) {
        asUser?.let { -User(it) }
        inPass?.let { -Stdin; -Prompt("") }
    }.execb(this, inContent = inPass)

fun sudoEdit(file: String, asUser: String? = null) = sudo {
    -Edit; asUser?.let { -User(it) }; +file
}

fun Kommand.withSudo(vararg options: SudoOpt, init: Sudo.() -> Unit = {}): Sudo = sudo(this, *options, init = init)

fun sudo(k: Kommand, vararg options: SudoOpt, init: Sudo.() -> Unit = {}) = sudo {
    opts.addAll(options); init(); -EOOpt; stuff.addAll(k.toArgs())
}

fun sudo(init: Sudo.() -> Unit = {}) = Sudo().apply(init)

/**
 * [home page](https://www.sudo.ws/)
 * [linux man](https://www.sudo.ws/docs/man/sudo.man/) */
data class Sudo(
    val opts: MutableList<SudoOpt> = mutableListOf(),
    val stuff: MutableList<String> = mutableListOf(),
) : Kommand {
    override val name get() = "sudo"
    override val args get() = opts.flatMap { it.toArgs() } + stuff
    operator fun SudoOpt.unaryMinus() = opts.add(this)
    operator fun String.unaryPlus() = stuff.add(this)
}

interface SudoOpt: KOpt {
    data object Help : KOptL("help"), SudoOpt // there is also short -h but it does NOT always mean help
    data object Version : KOptL("version"), SudoOpt
    data object SetHome : KOptL("set-home"), SudoOpt
    data class Host(val host: String) : KOptL("host", host), SudoOpt
    data object Login : KOptL("login"), SudoOpt
    data class Prompt(val prompt: String) : KOptL("prompt", prompt), SudoOpt
    data class ChRoot(val dir: String) : KOptL("chroot", dir), SudoOpt
    data class Role(val role: String) : KOptL("role", role), SudoOpt
    data object AskPass : KOptL("askpass"), SudoOpt
    data object Edit : KOptL("edit"), SudoOpt
    data object List : KOptL("list"), SudoOpt
    data object NonInteractive : KOptL("non-interactive"), SudoOpt
    data object Stdin : KOptL("stdin"), SudoOpt
    data object Shell : KOptL("shell"), SudoOpt
    data class Type(val type: String) : KOptL("type", type), SudoOpt
    data class User(val user: String) : KOptL("user", user), SudoOpt
    data class OtherUser(val user: String) : KOptL("other-user", user), SudoOpt
    data class Timeout(val timeout: String) : KOptL("command-timeout", timeout), SudoOpt
    /** Update the user's cached credentials, authenticating the user if necessary. */
    data object Validate : KOptL("validate"), SudoOpt
    data object NoUpdate : KOptL("no-update"), SudoOpt
    data object RemoveTimestamp : KOptL("remove-timestamp"), SudoOpt
    data object ResetTimestamp : KOptL("reset-timestamp"), SudoOpt
    data object EOOpt : KOptL(""), SudoOpt
}
