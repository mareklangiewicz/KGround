@file:Suppress("unused")

package pl.mareklangiewicz.kommand.admin

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.admin.SudoOpt.*

@OptIn(DelicateKommandApi::class)
fun sudo(
    k: Kommand,
    asUser: String? = null,
    inPass: String? = null,
    vararg options: SudoOpt
) = sudo(k, *options) {
    asUser?.let { -User(it) }
    inPass?.let { -Stdin; -Prompt("") }
}.reducedManually {
    inPass?.let { stdin.collect(flowOf(it)) }
    val out = stdout.toList()
    awaitAndChkExit(firstCollectErr = true)
    out
}

@DelicateKommandApi
fun sudoEdit(file: String, asUser: String? = null) = sudo {
    -Edit; asUser?.let { -User(it) }; +file
}

@DelicateKommandApi
fun Kommand.withSudo(vararg options: SudoOpt, init: Sudo.() -> Unit = {}): Sudo = sudo(this, *options, init = init)

@DelicateKommandApi
fun sudo(k: Kommand, vararg options: SudoOpt, init: Sudo.() -> Unit = {}) = sudo {
    opts.addAll(options); init(); -EOOpt; nonopts.addAll(k.toArgs())
}

@DelicateKommandApi
fun sudo(init: Sudo.() -> Unit = {}) = Sudo().apply(init)

/**
 * [home page](https://www.sudo.ws/)
 * [linux man](https://www.sudo.ws/docs/man/sudo.man/) */
@DelicateKommandApi
data class Sudo(
    override val opts: MutableList<SudoOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<SudoOpt> { override val name get() = "sudo" }

@DelicateKommandApi
interface SudoOpt: KOptTypical {
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
