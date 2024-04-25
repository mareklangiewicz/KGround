package pl.mareklangiewicz.kommand.jupyter

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.kotlinx.jupyter.api.libraries.*
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.uctx.uctx

internal class Integration : JupyterIntegration() {
  override fun Builder.onLoaded() {
//        render<BlaBla> { HTML("<p><b>bla1: </b>${it.bla1}</p><p><b>bla2: </b>${it.bla2}</p>") }
    import("kotlinx.coroutines.*")
    import("kotlinx.coroutines.flow.*")
    // FIXME_later: refactor packages, so only high-level functions are imported automatically, and delicate api are NOT.
    //   maybe high-level fun stuff up to pl.mareklangiewicz.kommand package??
    import("pl.mareklangiewicz.kommand.*")
    import("pl.mareklangiewicz.kommand.core.*")
    import("pl.mareklangiewicz.kommand.CLI.Companion.SYS")
    import("pl.mareklangiewicz.kommand.find.*")
    import("pl.mareklangiewicz.kommand.github.*")
  }
}


fun Flow<*>.logb() = logEachBlocking()


/**
 * Less strict flavor of ax, because in notebooks we are in more local "experimental" context.
 * BTW I don't want too many shortcut names inside kommandline itself, but here in kommandjupyter it's fine.
 */
suspend fun Kommand.ax(
  dir: String? = null,
  vararg useNamedArgs: Unit,
  inContent: String? = null,
  inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
  inFile: String? = null,
  outFile: String? = null,
  outFileAppend: Boolean = false,
  errToOut: Boolean = false,
  errFile: String? = null,
  errFileAppend: Boolean = false,
  expectedExit: ((Int) -> Boolean)? = { it == 0 },
  expectedErr: ((List<String>) -> Boolean)? = null,
  outLinesCollector: FlowCollector<String>? = null,
): List<String> = coroutineScope {
  val cli = implictx<CLI>()
  req(cli.isRedirectFileSupported || (inFile == null && outFile == null)) { "redirect file not supported here" }
  req(inLineS == null || inFile == null) { "Either inLineS or inFile or none, but not both" }
  req(outLinesCollector == null || outFile == null) { "Either outLinesCollector or outFile or none, but not both" }
  val eprocess = cli.start(
    this@ax,
    dir = dir,
    inFile = inFile,
    outFile = outFile,
    outFileAppend = outFileAppend,
    errToOut = errToOut,
    errFile = errFile,
    errFileAppend = errFileAppend,
  )
  val inJob = inLineS?.let { launch { eprocess.stdin.collect(it) } }
  // Note, Have to start pushing to stdin before collecting stdout,
  // because many commands wait for stdin before outputting data.
  val outJob = outLinesCollector?.let { eprocess.stdout.onEach(it::emit).launchIn(this) }
  inJob?.join()
  outJob?.join()
  eprocess
    .awaitResult() // inLineS already used
    .unwrap(expectedExit, expectedErr)
}

fun <K : Kommand, In, Out, Err> TypedKommand<K, In, Out, Err>.start(cli: CLI, dir: String? = null) =
  cli.start(this, dir)

/**
 * Blocking flavor of fun Kommand.ax(...). Will be deprecated when kotlin notebooks support suspending fun.
 * See: https://github.com/Kotlin/kotlin-jupyter/issues/239
 */
fun Kommand.axb(
  cli: CLI = provideSysCLI(),
  dir: String? = null,
  vararg useNamedArgs: Unit,
  inContent: String? = null,
  inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
  inFile: String? = null,
  outFile: String? = null,
  outFileAppend: Boolean = false,
  errToOut: Boolean = false,
  errFile: String? = null,
  errFileAppend: Boolean = false,
  expectedExit: ((Int) -> Boolean)? = { it == 0 },
  expectedErr: ((List<String>) -> Boolean)? = { it.isEmpty() },
  outLinesCollector: FlowCollector<String>? = null,
): List<String> = runBlocking {
  uctx(cli) {
    ax(
      dir,
      inContent = inContent,
      inLineS = inLineS,
      inFile = inFile,
      outFile = outFile,
      outFileAppend = outFileAppend,
      errToOut = errToOut,
      errFile = errFile,
      errFileAppend = errFileAppend,
      expectedExit = expectedExit,
      expectedErr = expectedErr,
      outLinesCollector = outLinesCollector,
    )
  }
}

/**
 * Blocking flavor of fun ReducedKommand.ax(...). Will be deprecated when kotlin notebooks support suspending fun.
 * See: https://github.com/Kotlin/kotlin-jupyter/issues/239
 */
fun <ReducedOut> ReducedKommand<ReducedOut>.axb(dir: String? = null): ReducedOut =
  runBlocking { ax(dir = dir) }
