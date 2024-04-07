package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.bad.*
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.ULogLevel
import pl.mareklangiewicz.ulog.e


// Hacky ulog impl for kommandline, before we have real ulog in context receivers
var ulogPrintLevel: ULogLevel = ULogLevel.INFO
var ulog = ULog { level, data -> if (level >= ulogPrintLevel) println("kl ${level.symbol} $data") }


// the ".enabled" suffix is important, so it's clear the user explicitly enabled a boolean "flag"
fun setUserFlag(cli: CLI, key: String, enabled: Boolean) {
    konfigInUserHomeConfigDir(cli)["$key.enabled"] = enabled.toString()
}

fun getUserFlag(cli: CLI, key: String) = konfigInUserHomeConfigDir(cli)["$key.enabled"]?.trim().toBoolean()

fun getUserFlagStr(cli: CLI, key: String) = if (getUserFlag(cli, key)) "enabled" else "NOT enabled"

fun getUserFlagFullStr(cli: CLI, key: String) = "User flag: $key is " + getUserFlagStr(cli, key) + "."


/**
 * Just some convention I like; additional "tmp" in name is there to emphasize that
 * this file content is temporary, and can be easily replaced by some kommand/sample/etc.
 */
val CLI.pathToTmpNotes get() = listOfNotNull(pathToUserTmp, pathToSystemTmp, pathToUserHome).first() + "/tmp.notes"


@OptIn(DelicateApi::class)
fun <ReducedOut> ReducedKommand<ReducedOut>.chkLineRaw(expectedLineRaw: String): ReducedKommand<ReducedOut> {
    val lineRaw = lineRawOrNull() ?: bad { "Unknown ReducedKommand implementation" }
    lineRaw.chkEq(expectedLineRaw)
    return this
}

/** @param stderr null means unknown/not-saved (emptyList should represent known empty stderr) */
class BadExitStateErr(val exit: Int, val stderr: List<String>? = null, message: String? = null) : BadStateErr(message)
class BadStdErrStateErr(val stderr: List<String>, message: String? = null) : BadStateErr(message)
class BadStdOutStateErr(val stdout: List<String>, message: String? = null) : BadStateErr(message)

// TODO_someday: figure out a nicer approach not to lose full error messages (maybe when we have context receivers in kotlin).
// But it's nice to have it mostly on the caller side. To just throw collected stderr/out on kommand execution side,
// without logging or any additional complexity there.
inline fun withLogBadStreams(
    limitLines: Int? = 40,
    stdoutLinePrefix: String = "STDOUT: ",
    stderrLinePrefix: String = "STDERR: ",
    skippedMarkersSuffix: String = " lines skipped",
    code: () -> Unit,
) {
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
        for (idx in 0 until max) ulog.e(prefix + this[idx])
        if (max < size) ulog.e(prefix + (size - max) + skippedMarkersSuffix)
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

