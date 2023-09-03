package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kground.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.startInTermIfUserConfirms
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import pl.mareklangiewicz.kommand.term.*


// the ".enabled" suffix is important, so it's clear the user explicitly enabled a boolean "flag"
fun CliPlatform.isUserFlagEnabled(key: String) = konfigInUserHomeConfigDir()["$key.enabled"]?.trim().toBoolean()
fun CliPlatform.setUserFlag(key: String, enabled: Boolean) { konfigInUserHomeConfigDir()["$key.enabled"] = enabled.toString() }

private val interactive by lazy {
    when {
        SYS.isJvm -> SYS.isUserFlagEnabled("code.interactive")
        else -> {
            println("Interactive stuff is only available on jvm platform (for now).")
            false
        }
    }
}

fun ifInteractive(block: () -> Unit) = if (interactive) block() else println("Interactive code is disabled.")

// FIXME_maybe: stuff like this is a bit too opinionated for kommandline module.
// Maybe move to kommandsamples or somewhere else??
@OptIn(DelicateKommandApi::class)
fun Kommand.chkWithUser(expectedLineRaw: String? = null, execInDir: String? = null, platform: CliPlatform = SYS) {
    this.logLineRaw()
    if (expectedLineRaw != null) lineRaw().chkEq(expectedLineRaw)
    ifInteractive { platform.startInTermIfUserConfirms(
        kommand = this,
        execInDir = execInDir,
        termKommand = { termKitty(it) },
    ) }
}

@OptIn(DelicateKommandApi::class)
fun ReducedKommand<*>.chkLineRawAndExec(expectedLineRaw: String, execInDir: String? = null, platform: CliPlatform = SYS) {
    val lineRaw = lineRawOrNull() ?: bad { "Unknown ReducedKommand implementation" }
    println(lineRaw)
    lineRaw.chkEq(expectedLineRaw)
    execb(platform, execInDir)
}

/** @param stderr null means unknown/not-saved (known empty stderr should be represented by emptyList) */
class BadExitStateErr(exp: Int, act: Int, val stderr: List<String>? = null, message: String? = null): NotEqStateErr(exp, act, message)
class BadStdErrStateErr(val stderr: List<String>, message: String? = null): BadStateErr(message)
class BadStdOutStateErr(val stdout: List<String>, message: String? = null): BadStateErr(message)

// TODO_someday: figure out nicer approach not to loose full error messages (maybe when we have context receivers in kotlin).
// But it's nice to have it mostly on caller side. To just throw collected stderr/out on kommand execution side,
// without logging or any additional complexity there..
inline fun withPrintingBadStreams(
    limitLines: Int? = 40,
    stdoutLinePrefix: String = "STDOUT: ",
    stderrLinePrefix: String = "STDERR: ",
    skippedMarkersSuffix: String = " lines skipped",
    code: () -> Unit
) {
    // Kotlin doesn't support local fun inside inline fun, or even private fun below in the same file,
    // that's the reason why logSome lambda is "val"
    val logSome: List<String>.(prefix: String) -> Unit = { prefix ->
        // TODO_someday: investigate:
        // I had strange error where coerceAtMost didn't work. That's why I used explicit "when".
        // Maybe there is some bigger kotlin bug when defining lambdas inside inline fun.
        val max = when {
            limitLines == null -> size
            limitLines > size -> size
            else -> limitLines
        }
        for (idx in 0 until max) println(prefix + this[idx])
        if (max < size) println(prefix + (size - max) + skippedMarkersSuffix)
    }
    try { code() }
    catch (e: BadExitStateErr) { e.stderr?.logSome(stderrLinePrefix); throw e }
    catch (e: BadStdErrStateErr) { e.stderr.logSome(stderrLinePrefix); throw e }
    catch (e: BadStdOutStateErr) { e.stdout.logSome(stdoutLinePrefix); throw e }
}

/** @param stderr null means unknown/not-saved (known empty stderr should be represented by emptyList) */
inline fun Int.chkExit(
    exp: Int = 0,
    stderr: List<String>? = null,
    lazyMessage: () -> String = { "bad exit $this != $exp" }
) { this == exp || throw BadExitStateErr(exp, this, stderr, lazyMessage()) }


inline fun List<String>.chkStdErr(
    test: List<String>.() -> Boolean = { isEmpty() },
    lazyMessage: () -> String = { "bad stderr" }
) { test(this) || throw BadStdErrStateErr(this, lazyMessage()) }

inline fun List<String>.chkStdOut(
    test: List<String>.() -> Boolean = { isEmpty() },
    lazyMessage: () -> String = { "bad stdout" }
) { test(this) || throw BadStdOutStateErr(this, lazyMessage()) }

@OptIn(DelicateKommandApi::class)
fun Kommand.chkInIdeap(
    expectedLineRaw: String? = null,
    execInDir: String? = null,
    platform: CliPlatform = SYS
) {
    this.logLineRaw()
    if (expectedLineRaw != null) lineRaw().chkEq(expectedLineRaw)
    ifInteractive { platform.run {
        val tmpFile = "$pathToUserTmp/tmp.notes"
        start(this@chkInIdeap, dir = execInDir, outFile = tmpFile).waitForExit()
        start(ideap { +tmpFile }).waitForExit()
    } }
}

