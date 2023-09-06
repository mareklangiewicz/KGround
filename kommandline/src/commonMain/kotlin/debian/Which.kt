package pl.mareklangiewicz.kommand.debian

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kommand.*

fun isKommandAvailable(kommand: Kommand) = isCommandAvailable(kommand.name)

fun isCommandAvailable(command: String) = whichFirstOrNull(command).reducedMap { this != null }

@OptIn(DelicateKommandApi::class)
fun whichFirstOrNull(command: String) = which(command).reducedOut { firstOrNull() }

@OptIn(DelicateKommandApi::class)
fun which(vararg commands: String, all: Boolean = false) =
    which { if (all) -WhichOpt.All; for (c in commands) +c }

@DelicateKommandApi
fun which(init: Which.() -> Unit) = Which().apply(init)


/** [linux man](https://linux.die.net/man/1/which) */
@DelicateKommandApi
data class Which(
    override val opts: MutableList<WhichOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf()
) : KommandTypical<WhichOpt> { override val name get() = "which" }

@DelicateKommandApi
interface WhichOpt: KOptTypical {
    /** print all matching path names of each argument */
    data object All : KOptS("a"), WhichOpt
}
