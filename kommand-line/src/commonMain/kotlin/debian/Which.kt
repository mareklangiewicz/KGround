package pl.mareklangiewicz.kommand.debian

import kotlinx.coroutines.flow.*
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.debian.WhichOpt.All
import pl.mareklangiewicz.udata.MutLO

fun isKommandAvailable(kommand: Kommand): ReducedScript<Boolean> = isCommandAvailable(kommand.name)

fun isCommandAvailable(command: String): ReducedScript<Boolean> = ReducedScript { whichFirstOrNull(command).ax() != null }

@OptIn(DelicateApi::class)
fun whichFirstOrNull(command: String): ReducedScript<Path?> = ReducedScript {
  try { whichFirst(command).ax() }
  catch (_: NoSuchElementException) { null }
  catch (_: BadExitStateErr) { null }
}

@OptIn(DelicateApi::class)
fun whichFirst(command: String): ReducedKommand<Path> = which(command).reducedOut { first().P }

@OptIn(DelicateApi::class)
fun which(vararg commands: String, all: Boolean = false): Which =
  which { if (all) -All; for (c in commands) +c }

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
