package pl.mareklangiewicz.kommand

import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.startInGnomeTermIfUserConfirms
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import kotlin.time.*

val Any?.unit get() = Unit

infix fun <T: Any> List<T>.plusIfNN(element: T?) = if (element == null) this else this + element
infix fun <T: Any> List<T>.prependIfNN(element: T?) = if (element == null) this else listOf(element) + this

fun Iterator<String>.loglns(logln: (String) -> Unit = ::println) = forEach(logln)

@OptIn(ExperimentalTime::class)
fun Iterator<String>.loglnsWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println,
) = loglns { logln("${mark.elapsedNow().inWholeMilliseconds} $it") }

fun Iterable<String>.loglns(logln: (String) -> Unit = ::println) = iterator().loglns(logln)
fun Sequence<String>.loglns(logln: (String) -> Unit = ::println) = iterator().loglns(logln)

@OptIn(ExperimentalTime::class)
fun Iterable<String>.loglnsWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println,
) = iterator().loglnsWithMillis(mark, logln)

@OptIn(ExperimentalTime::class)
fun Sequence<String>.loglnsWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println,
) = iterator().loglnsWithMillis(mark, logln)

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

fun Kommand.checkWithUser(expectedKommandLine: String? = null, execInDir: String? = null, platform: CliPlatform = SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.startInGnomeTermIfUserConfirms(kommand = this, execInDir = execInDir) }
}

fun Kommand.checkInIdeap(expectedKommandLine: String? = null, execInDir: String? = null, platform: CliPlatform = SYS) {
    this.println()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.run {
        val tmpFile = "$pathToUserTmp/tmp.notes"
        start(this@checkInIdeap, dir = execInDir, outFile = tmpFile).waitForExit()
        start(ideap { +tmpFile }).waitForExit()
    } }
}

