package pl.mareklangiewicz.kommand

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import okio.Path
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kground.io.UWorkDir
import pl.mareklangiewicz.kground.plusIfNN
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import pl.mareklangiewicz.uctx.uctx
import pl.mareklangiewicz.udata.strf
import pl.mareklangiewicz.ulog.e
import pl.mareklangiewicz.ulog.localULog


// the ".enabled" suffix is important, so it's clear the user explicitly enabled a boolean "flag"
fun setUserFlag(cli: CLI, key: String, enabled: Boolean) {
  konfigInUserHomeConfigDir(cli)["$key.enabled"] = enabled.strf
}

fun getUserFlag(cli: CLI, key: String) = konfigInUserHomeConfigDir(cli)["$key.enabled"]?.trim().toBoolean()

fun getUserFlagStr(cli: CLI, key: String) = if (getUserFlag(cli, key)) "enabled" else "NOT enabled"

fun getUserFlagFullStr(cli: CLI, key: String) = "User flag: $key is " + getUserFlagStr(cli, key) + "."

// TODO: remove cli params from these user flags utils, and let konfigInUserHomeConfigDir use default getSysCLI()



fun <K : Kommand> K.chkEqLineRaw(expectedLineRaw: String): K = apply {
  val lineRaw = lineRaw()
  lineRaw.chkEq(expectedLineRaw) { "bad Kommand.lineRaw(): $lineRaw != $expectedLineRaw" }
}

@OptIn(DelicateApi::class)
fun <ReducedOut> ReducedKommand<ReducedOut>.chkEqLineRaw(expectedLineRaw: String) = apply {
  val lineRaw = lineRaw()
  lineRaw.chkEq(expectedLineRaw) { "bad ReducedKommand.lineRaw(): $lineRaw != $expectedLineRaw" }
}

fun <K : Kommand> K.chkThisLineRaw(thisLineRawIsFine: String.() -> Boolean): K = apply {
  val lineRaw = lineRaw()
  lineRaw.chkThis({ "this is bad Kommand.lineRaw(): $lineRaw" }, thisLineRawIsFine)
}

@OptIn(DelicateApi::class)
fun <ReducedOut> ReducedKommand<ReducedOut>.chkThisLineRaw(thisLineRawIsFine: String.() -> Boolean) = apply {
  val lineRaw = lineRaw()
  lineRaw.chkThis({ "this is bad ReducedKommand.lineRaw(): $lineRaw" }, thisLineRawIsFine)
}

/** @param stderr null means unknown/not-saved (emptyList should represent known empty stderr) */
class BadExitStateErr(val exit: Int, val stderr: List<String>? = null, message: String? = null) : BadStateErr(message)
class BadStdErrStateErr(val stderr: List<String>, message: String? = null) : BadStateErr(message)
class BadStdOutStateErr(val stdout: List<String>, message: String? = null) : BadStateErr(message)

// TODO_someday: figure out a nicer approach not to lose full error messages (maybe when we have context receivers in kotlin).
// But it's nice to have it mostly on the caller side. To just throw collected stderr/out on kommand execution side,
// without logging or any additional complexity there.
suspend inline fun withLogBadStreams(
  limitLines: Int? = 40,
  stdoutLinePrefix: String = "STDOUT: ",
  stderrLinePrefix: String = "STDERR: ",
  skippedMarkersSuffix: String = " lines skipped",
  code: () -> Unit,
) {
  val log = localULog()
  // Kotlin doesn't support local fun inside inline fun, or even private fun below in the same file,
  // that's the reason why logSome lambda is "val"
  val logSome: List<String>.(prefix: String) -> Unit = { prefix ->
    // TODO_someday: investigate:
    // I had a strange error where coerceAtMost didn't work. That's why I used explicit "when".
    // Maybe there is some bigger kotlin bug when defining lambdas inside inline fun.
    val max = when {
      limitLines == null -> size
      limitLines > size -> size
      else -> limitLines
    }
    for (idx in 0 until max) log.e(prefix + this[idx])
    if (max < size) log.e(prefix + (size - max) + skippedMarkersSuffix)
  }
  try {
    code()
  } catch (e: BadExitStateErr) {
    e.stderr?.logSome(stderrLinePrefix); throw e
  } catch (e: BadStdErrStateErr) {
    e.stderr.logSome(stderrLinePrefix); throw e
  } catch (e: BadStdOutStateErr) {
    e.stdout.logSome(stdoutLinePrefix); throw e
  }
}

/** @param stderr null means unknown/not-saved (emptyList should represent known empty stderr) */
inline fun Int.chkExit(
  test: Int.() -> Boolean = { this == 0 },
  stderr: List<String>? = null,
  lazyMessage: () -> String = { "bad exit: $this" },
): Int {
  test() || throw BadExitStateErr(this, stderr, lazyMessage()); return this
}


inline fun List<String>.chkStdErr(
  test: List<String>.() -> Boolean = { isEmpty() },
  lazyMessage: () -> String = { "bad stderr" },
): List<String> {
  test(this) || throw BadStdErrStateErr(this, lazyMessage()); return this
}

inline fun List<String>.chkStdOut(
  test: List<String>.() -> Boolean = { isEmpty() },
  lazyMessage: () -> String = { "bad stdout" },
): List<String> {
  test(this) || throw BadStdOutStateErr(this, lazyMessage()); return this
}

@NotPortableApi @DelicateApi
expect fun <T> runBlockingOrErr(block: suspend CoroutineScope.() -> T): T

@NotPortableApi
@DelicateApi
@Deprecated("Use suspend fun Kommand.ax(...)")
fun Kommand.axBlockingOrErr(
  cli: CLI,
  vararg useNamedArgs: Unit,
  workDir: Path? = null,
  inContent: String? = null,
  inLineS: Flow<String>? = inContent?.lineSequence()?.asFlow(),
  inFile: Path? = null,
  outFile: Path? = null,
): List<String> = runBlockingOrErr {
  uctx(cli plusIfNN workDir?.let(::UWorkDir)) {
    ax(inContent = inContent, inLineS = inLineS, inFile = inFile, outFile = outFile)
  }
}

@NotPortableApi
@DelicateApi
@Deprecated("Use suspend fun Kommand.ax(...)")
fun <ReducedOut> ReducedScript<ReducedOut>.axBlockingOrErr(
  cli: CLI,
  vararg useNamedArgs: Unit,
  workDir: Path? = null,
): ReducedOut = runBlockingOrErr {
  uctx(cli plusIfNN workDir?.let(::UWorkDir)) { ax() }
}
