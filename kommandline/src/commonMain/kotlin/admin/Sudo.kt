@file:Suppress("unused")

package pl.mareklangiewicz.kommand.admin

import pl.mareklangiewicz.kommand.*

fun CliPlatform.sudoExec(k: Kommand, asUser: String? = null, inPass: String? = null, vararg options: SudoOpt) =
    sudo(k, *options) {
        asUser?.let { -SudoOpt.User(it) }
        inPass?.let { -SudoOpt.Stdin; -SudoOpt.Prompt("") }
    }.exec(inContent = inPass)

fun sudoEdit(file: String, asUser: String? = null) = sudo {
    -SudoOpt.Edit; asUser?.let { -SudoOpt.User(it) }; +file
}

fun Kommand.withSudo(vararg options: SudoOpt, init: Sudo.() -> Unit = {}): Sudo = sudo(this, *options, init = init)

fun sudo(k: Kommand, vararg options: SudoOpt, init: Sudo.() -> Unit = {}) = sudo {
    for (o in options) -o
    init(); +"--"; +k.name; for (a in k.args) +a
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
    override val args get() = opts.flatMap { it.args } + stuff
    operator fun SudoOpt.unaryMinus() = opts.add(this)
    operator fun String.unaryPlus() = stuff.add(this)
}

interface SudoOpt: KOpt {
    object Help : KOptL("help"), SudoOpt // there is also short -h but it does NOT always mean help
    object Version : KOptL("version"), SudoOpt
    object SetHome : KOptL("set-home"), SudoOpt
    data class Host(val host: String) : KOptL("host", host), SudoOpt
    object Login : KOptL("login"), SudoOpt
    data class Prompt(val prompt: String) : KOptL("prompt", prompt), SudoOpt
    data class ChRoot(val dir: String) : KOptL("chroot", dir), SudoOpt
    data class Role(val role: String) : KOptL("role", role), SudoOpt
    object AskPass : KOptL("askpass"), SudoOpt
    object Edit : KOptL("edit"), SudoOpt
    object List : KOptL("list"), SudoOpt
    object NonInteractive : KOptL("non-interactive"), SudoOpt
    object Stdin : KOptL("stdin"), SudoOpt
    object Shell : KOptL("shell"), SudoOpt
    data class Type(val type: String) : KOptL("type", type), SudoOpt
    data class User(val user: String) : KOptL("user", user), SudoOpt
    data class OtherUser(val user: String) : KOptL("other-user", user), SudoOpt
    data class Timeout(val timeout: String) : KOptL("command-timeout", timeout), SudoOpt
    /** Update the user's cached credentials, authenticating the user if necessary. */
    object Validate : KOptL("validate"), SudoOpt
    object NoUpdate : KOptL("no-update"), SudoOpt
    object RemoveTimestamp : KOptL("remove-timestamp"), SudoOpt
    object ResetTimestamp : KOptL("reset-timestamp"), SudoOpt
}