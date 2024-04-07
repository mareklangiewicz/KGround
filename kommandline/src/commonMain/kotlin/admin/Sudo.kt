@file:Suppress("unused")

package pl.mareklangiewicz.kommand.admin

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.admin.SudoOpt.*

/**
 * Tested only for simple non-interactive commands, that don't expect any input and just print some output.
 * Password (if provided) is given to sudo via stdin, and then stdin is closed, so no input for inner kommand.
 */
// This is just a simple sequential implementation, for simple kommands.
// TODO_someday_maybe:
// Implementation for long-running kommands that read input and write to stdout and stderr concurrently over time.
// But carefully because with outer sudo kommand, it can get complicated.
// Would need to be sure how exactly sudo passes data through.
// For example, when first sudo itself is collecting password from stdin.
// So I guess the correct concurrent implementation would be complicated and require a lot of testing.
// See fun TypedKommand<...>.reducedOut for more typical concurrent implementation
// (but without sudo related complications).
@DelicateApi
fun sudo(
    k: Kommand,
    vararg useNamedArgs: Unit,
    asUser: String? = null,
    inPass: String? = null,
) = sudo(k) {
    asUser?.let { -User(it) }
    inPass?.let { -Stdin; -Prompt("") }
}.reducedManually {
    inPass?.let { stdin.collect(flowOf(it)) }
    val out = stdout.toList()
    awaitAndChkExit(firstCollectErr = true)
    out
}

@DelicateApi
fun sudoEdit(file: String, asUser: String? = null) = sudo {
    -Edit; asUser?.let { -User(it) }; +file
}

@DelicateApi
fun Kommand.withSudo(vararg options: SudoOpt, init: Sudo.() -> Unit = {}): Sudo = sudo(this, *options, init = init)

@DelicateApi
fun sudo(k: Kommand, vararg options: SudoOpt, init: Sudo.() -> Unit = {}) = sudo {
    opts.addAll(options); init(); -EOOpt; nonopts.addAll(k.toArgs())
}

@DelicateApi
fun sudo(init: Sudo.() -> Unit = {}) = Sudo().apply(init)

/**
 * [home page](https://www.sudo.ws/)
 * [linux man](https://www.sudo.ws/docs/man/sudo.man/) */
@DelicateApi
data class Sudo(
    override val opts: MutableList<SudoOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf(),
) : KommandTypical<SudoOpt> {
    override val name get() = "sudo"
}

@DelicateApi
interface SudoOpt : KOptTypical {
    data object Help : KOptLN(), SudoOpt // there is also short -h but it does NOT always mean help
    data object Version : KOptLN(), SudoOpt
    data object SetHome : KOptLN(), SudoOpt
    data class Host(val host: String) : KOptLN(host), SudoOpt
    data object Login : KOptLN(), SudoOpt
    data class Prompt(val prompt: String) : KOptLN(prompt), SudoOpt
    data class ChRoot(val dir: String) : KOptL("chroot", dir), SudoOpt
    data class Role(val role: String) : KOptLN(role), SudoOpt
    data object AskPass : KOptL("askpass"), SudoOpt
    data object Edit : KOptLN(), SudoOpt
    data object List : KOptLN(), SudoOpt
    data object NonInteractive : KOptLN(), SudoOpt
    data object Stdin : KOptLN(), SudoOpt
    data object Shell : KOptLN(), SudoOpt
    data class Type(val type: String) : KOptLN(type), SudoOpt
    data class User(val user: String) : KOptLN(user), SudoOpt
    data class OtherUser(val user: String) : KOptLN(user), SudoOpt
    data class Timeout(val timeout: String) : KOptL("command-timeout", timeout), SudoOpt
    /** Update the user's cached credentials, authenticating the user if necessary. */
    data object Validate : KOptLN(), SudoOpt
    data object NoUpdate : KOptLN(), SudoOpt
    data object RemoveTimestamp : KOptLN(), SudoOpt
    data object ResetTimestamp : KOptLN(), SudoOpt
    data object EOOpt : KOptL(""), SudoOpt
}
