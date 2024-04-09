package pl.mareklangiewicz.kgroundx.jupyter

import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.interactive.isInteractiveCodeEnabled
import pl.mareklangiewicz.interactive.tryInteractivelySomethingRef
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.ideOpen
import pl.mareklangiewicz.kommand.pathToTmpNotes
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.kommand.withLogBadStreams
import pl.mareklangiewicz.kommand.writeFileWithDD
import pl.mareklangiewicz.kommand.zenityAskIf
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.hack.ulogCache
import pl.mareklangiewicz.ulog.i

/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this file allows invoking any code pointed by reference or clipboard (containing reference)
 * (see also IntelliJ action: CopyReference)
 * Usually it will be from samples/examples/demos, or from gitignored playground, like:
 * pl.mareklangiewicz.kgroundx.maintenance.MyExamples#checkMyRegionsAndWorkflows
 * pl.mareklangiewicz.kgroundx.jupyter.PlaygroundKt#play
 * So that way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 * The gradle kgroundx-jupyter:run task is set up to run the main fun here.
 */
@OptIn(DelicateApi::class, NotPortableApi::class) fun main(args: Array<String>) = runBlocking {
  when {
    args.size == 2 && args[0] == "try-code" -> try {
      withLogBadStreams { tryInteractivelySomethingRef(args[1]) }
    } finally {
      tryInteractivelyOpenLogCacheInIde()
    }
    args.size == 2 && args[0] == "get-user-flag" -> ulog.i(getUserFlagFullStr(SYS, args[1]))
    args.size == 3 && args[0] == "set-user-flag" -> setUserFlag(SYS, args[1], args[2].toBoolean())
    else -> bad { "Incorrect args. See Main.kt:main" }
  }
}

@OptIn(DelicateApi::class) suspend fun tryInteractivelyOpenLogCacheInIde() {
  val lines = ulogCache ?: return
  isInteractiveCodeEnabled() && zenityAskIf("Try to open log cache in IDE (in tmp.notes)?").ax() || return
  writeFileWithDD(lines, SYS.pathToTmpNotes).ax()
  ideOpen(SYS.pathToTmpNotes).ax()
}
