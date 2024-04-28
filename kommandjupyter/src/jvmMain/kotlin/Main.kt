package pl.mareklangiewicz.kommand.jupyter

import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.interactive.tryInteractivelySomethingRef
import pl.mareklangiewicz.kground.io.uctxWithIO
import pl.mareklangiewicz.kgroundx.maintenance.ZenitySupervisor
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.getDefaultCLI
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.kommand.withLogBadStreams
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.ulog.i

/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this file allows invoking any code pointed by reference or clipboard (containing reference)
 * (see also IntelliJ action: CopyReference)
 * Usually it will be from samples/examples/demos, or from gitignored playground, like:
 * pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop
 * pl.mareklangiewicz.kommand.jupyter.PlaygroundKt#play
 * So way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 * The gradle kommandjupyter:run task is set up to run the main fun here.
 */
@OptIn(DelicateApi::class, NotPortableApi::class)
fun main(args: Array<String>) = runBlocking {
  val log = UHackySharedFlowLog { level, data -> "L ${level.symbol} ${data.str(maxLength = 512)}" }
  val submit = ZenitySupervisor()
  val cli = getDefaultCLI()
  // uctxWithIO(log + submit + cli, dispatcher = null) { // FIXME_later: rethink default dispatcher..
  uctxWithIO(log + submit + cli) {
    when {
      args.size == 2 && args[0] == "try-code" -> withLogBadStreams { tryInteractivelySomethingRef(args[1]) }
      args.size == 2 && args[0] == "get-user-flag" -> log.i(getUserFlagFullStr(cli, args[1]))
      args.size == 3 && args[0] == "set-user-flag" -> setUserFlag(cli, args[1], args[2].toBoolean())
      else -> bad { "Incorrect args. See Main.kt:main" }
    }
  }
}

