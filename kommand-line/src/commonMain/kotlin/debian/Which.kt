package pl.mareklangiewicz.kommand.debian

import kotlinx.coroutines.flow.*
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.udata.MutLO

fun isKommandAvailable(kommand: Kommand): ReducedKommand<Boolean> = isCommandAvailable(kommand.name)

fun isCommandAvailable(command: String): ReducedKommand<Boolean> = whichFirstOrNull(command).reducedMap { this != null }

@OptIn(DelicateApi::class)
fun whichFirstOrNull(command: String): ReducedKommand<Path?> = which(command).reducedOut { firstOrNull()?.P }

@OptIn(DelicateApi::class)
fun which(vararg commands: String, all: Boolean = false): Which =
  which { if (all) -WhichOpt.All; for (c in commands) +c }

@DelicateApi
fun which(init: Which.() -> Unit) = Which().apply(init)


/** [linux man](https://linux.die.net/man/1/which) */
@DelicateApi
data class Which(
  override val opts: MutableList<WhichOpt> = MutLO(),
  override val nonopts: MutableList<String> = MutLO(),
) : KommandTypical<WhichOpt> {
  override val name get() = "which"
}

@DelicateApi
interface WhichOpt : KOptTypical {
  /** print all matching path names of each argument */
  data object All : KOptS("a"), WhichOpt
}
