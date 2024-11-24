package pl.mareklangiewicz.kground

import kotlinx.coroutines.*
import pl.mareklangiewicz.annotations.*
import pl.mareklangiewicz.bad.BadStateErr
import pl.mareklangiewicz.bad.bad
import pl.mareklangiewicz.interactive.*
import pl.mareklangiewicz.kground.io.uctxWithIO
import pl.mareklangiewicz.kgroundx.maintenance.ZenitySupervisor
import pl.mareklangiewicz.kommand.getSysCLI
import pl.mareklangiewicz.kommand.getUserFlagFullStr
import pl.mareklangiewicz.kommand.localCLI
import pl.mareklangiewicz.kommand.setUserFlag
import pl.mareklangiewicz.udata.str
import pl.mareklangiewicz.ulog.hack.UHackySharedFlowLog
import pl.mareklangiewicz.ulog.i
import pl.mareklangiewicz.ulog.localULog

@OptIn(DelicateApi::class, NotPortableApi::class, ExperimentalApi::class)
fun main(args: Array<String>) {
  try {
    mainCodeExperiments(args)
      // FIXME_later: better messages inside (probably use some args parsing library)
      // (after merging KommandLine with KGround)
  }
  catch (e: BadStateErr) {
    if (e.message?.startsWith("Incorrect args") == true) {
      println(e.message)
      println("For example:")
      println("kgroundx get-user-flag code.interactive")
      println("kgroundx set-user-flag code.interactive true")
      println("kgroundx set-user-flag code.interactive false")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectToAbcdK")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectToKGround")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.maintenance.MyTemplatesExamples#tryInjectAllMyProjects")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.maintenance.MyOtherExamples#updateGradlewInMyProjects")
      println("kgroundx try-code pl.mareklangiewicz.kgroundx.experiments.MyExperiments#collectGabrysCards")
    }
    else throw e
  }
}


/**
 * Experimenting directly in kotlin notebooks would be ideal, but the IDE support it's still not great...
 * So this fun (called from main fun) allows invoking any code pointed by reference or clipboard (containing reference)
 * (see also IntelliJ action: CopyReference)
 * Usually it will be from samples/examples/demos, or from gitignored playground, like:
 * pl.mareklangiewicz.kommand.demo.MyDemoSamples#getBtop
 * pl.mareklangiewicz.kommand.app.Playground#play
 * So way we have the IDE support, and later we can C&P working code snippets into notebooks or whateva.
 * The gradle kommandapp:run task is set up to run the mainCodeExperiments fun here.
 */
@NotPortableApi
@DelicateApi("API for manual interactive experimentation. Careful because it an easily call ANY code with reflection.")
@ExperimentalApi("Will be removed someday. Temporary solution for running some code parts fast. Like examples/samples.")
private fun mainCodeExperiments(args: Array<String>) {
  val a0 = args.getOrNull(0).orEmpty()
  val a1 = args.getOrNull(1).orEmpty()
  val a2 = args.getOrNull(2).orEmpty()
  runBlockingMain(a0) {
    when {
      args.size == 2 && a0 == "try-code" -> tryInteractivelyCodeRefWithLogging(a1)
      // Note: get and set flag can't use interactive features, because it should work even if disabled.
      args.size == 2 && a0 == "get-user-flag" -> localULog().i(getUserFlagFullStr(localCLI(), a1))
      args.size == 3 && a0 == "set-user-flag" -> setUserFlag(localCLI(), a1, a2.toBoolean())
      else -> bad { "Incorrect args. See KommandLine -> InteractiveSamples.kt -> mainCodeExperiments" }
    }
  }
}

private fun runBlockingMain(name: String, block: suspend CoroutineScope.() -> Unit) =
  runBlocking {
    val log = UHackySharedFlowLog { level, data -> "L ${level.symbol} ${data.str(maxLength = 512)}" }
    uctxWithIO(
      context = log + ZenitySupervisor() + getSysCLI(),
      name = name,
      // dispatcher = null, // FIXME_later: rethink default dispatcher
      block = block,
    )
  }


//
// class KGroundXApp() : CliktCommand(name = "kgroundx") {
//   override fun run() {
//     TODO("Not yet implemented")
//   }
// }
//
// class GetUserFlag() : CliktCommand() {
//   override fun run() {
//     TODO("Not yet implemented")
//   }
// }
