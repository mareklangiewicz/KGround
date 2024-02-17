package pl.mareklangiewicz.kommand.debian

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kommand.*

fun isKommandAvailable(kommand: Kommand) = isCommandAvailable(kommand.name)

fun isCommandAvailable(command: String) = whichFirstOrNull(command).reducedMap { this != null }

@OptIn(DelicateApi::class)
fun whichFirstOrNull(command: String) = which(command).reducedOut { firstOrNull() }

@OptIn(DelicateApi::class)
fun which(vararg commands: String, all: Boolean = false) =
    which { if (all) -WhichOpt.All; for (c in commands) +c }

@DelicateApi
fun which(init: Which.() -> Unit) = Which().apply(init)


/** [linux man](https://linux.die.net/man/1/which) */
@DelicateApi
data class Which(
    override val opts: MutableList<WhichOpt> = mutableListOf(),
    override val nonopts: MutableList<String> = mutableListOf()
) : KommandTypical<WhichOpt> { override val name get() = "which" }

@DelicateApi
interface WhichOpt: KOptTypical {
    /** print all matching path names of each argument */
    data object All : KOptS("a"), WhichOpt
}
