package pl.mareklangiewicz.kgroundx.jupyter

import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.interactive.isInteractiveCodeEnabled
import pl.mareklangiewicz.interactive.tryInteractivelySomethingRef
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.kground.logEach
import pl.mareklangiewicz.kgroundx.maintenance.MyZenityManager
import pl.mareklangiewicz.kommand.CLI.Companion.SYS
import pl.mareklangiewicz.kommand.ax
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.ideOpen
import pl.mareklangiewicz.kommand.pathToTmpNotes
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.kommand.withLogBadStreams
import pl.mareklangiewicz.kommand.writeFileWithDD
import pl.mareklangiewicz.kommand.zenity.zenityAskIf
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.e
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.implictxOrNull
import pl.mareklangiewicz.ulog.w

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
  val log = UHackySharedFlowLog { level, data -> "L ${level.symbol} ${data.str(maxLength = 512)}" }
  val submit = MyZenityManager()
  uctx(log, submit) {
    when {
      args.size == 2 && args[0] == "try-code" -> try {
        log.w("try-code ${args[1]} started")
        withLogBadStreams { tryInteractivelySomethingRef(args[1]) }
        log.w("try-code ${args[1]} finished")
        tryInteractivelyOpenLogCacheInIde()
      } catch (ex: Exception) {
        log.e("try-code ${args[1]} failed")
        log.exWithTrace(ex)
        tryInteractivelyOpenLogCacheInIde()
      }
      args.size == 2 && args[0] == "get-user-flag" -> log.i(getUserFlagFullStr(SYS, args[1]))
      args.size == 3 && args[0] == "set-user-flag" -> setUserFlag(SYS, args[1], args[2].toBoolean())
      else -> bad { "Incorrect args. See Main.kt:main" }
    }
  }
}

@OptIn(DelicateApi::class) suspend fun tryInteractivelyOpenLogCacheInIde() {
  val lines = implictxOrNull<UHackySharedFlowLog>()?.flow?.replayCache ?: return
  isInteractiveCodeEnabled() && zenityAskIf("Try to open log cache in IDE (in tmp.notes)?").ax() || return
  writeFileWithDD(lines, SYS.pathToTmpNotes).ax()
  ideOpen(SYS.pathToTmpNotes).ax()
}

// FIXME: common "UStr" utils with different MPP parametrized exception conversions / string representations.
//   (start with moving some of what is in UWidgets to KGround) (see comments in UHackyLog)
@Deprecated("This is temporary fast&dirty impl")
private fun ULog.exWithTrace(ex: Throwable) {
  e(ex.toString())
  ex.stackTrace?.let {
    e("STACK TRACE:")
    it.toList().logEach { e(it) }
    // each line have to be logged separately: don't use it.joinToString("\n"), because truncation in logger
  }
  ex.cause?.let {
    e("CAUSE:")
    exWithTrace(it)
  }
}
