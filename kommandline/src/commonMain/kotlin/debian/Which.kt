package pl.mareklangiewicz.kommand.debian

import pl.mareklangiewicz.kommand.*

fun CliPlatform.isKommandAvailable(kommand: Kommand) = isCommandAvailable(kommand.name)

fun CliPlatform.isCommandAvailable(command: String) = start(which(command)).waitForExit() == 0

fun CliPlatform.whichOneExec(command: String) = which(command).execb(this).firstOrNull()

fun which(vararg commands: String, all: Boolean = false) =
    which { if (all) -WhichOpt.All; for (c in commands) +c }

fun which(init: Which.() -> Unit = {}) = Which().apply(init)


/** [linux man](https://linux.die.net/man/1/which) */
data class Which(
    val opts: MutableList<WhichOpt> = mutableListOf(),
    val commands: MutableList<String> = mutableListOf()
) : Kommand {
    override val name get() = "which"
    override val args get() = opts.flatMap { it.toArgs() } + commands
    operator fun String.unaryPlus() = commands.add(this)
    operator fun WhichOpt.unaryMinus() = opts.add(this)
}

@DelicateKommandApi
interface WhichOpt: KOptTypical {
    /** print all matching path names of each argument */
    data object All : KOptS("a"), WhichOpt
}
