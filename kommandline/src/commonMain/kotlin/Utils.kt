package pl.mareklangiewicz.kommand

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.startInGnomeTermIfUserConfirms
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import kotlin.time.*


val Any?.unit get() = Unit

infix fun <T: Any> List<T>.plusIfNN(element: T?) = if (element == null) this else this + element
infix fun <T: Any> List<T>.prependIfNN(element: T?) = if (element == null) this else listOf(element) + this

fun Any.classSimpleWords() = this::class.simpleName!!
    .split(Regex("(?<=\\w)(?=\\p{Upper})")).map { it.lowercase() }


fun Iterator<*>.logEach(logln: (Any?) -> Unit = ::println) = forEach(logln)

@OptIn(ExperimentalTime::class)
fun Iterator<*>.logEachWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println,
) = logEach { logln(it.toStringWithMillis(mark)) }

fun Iterable<*>.logEach(logln: (Any?) -> Unit = ::println) = iterator().logEach(logln)
fun Sequence<*>.logEach(logln: (Any?) -> Unit = ::println) = iterator().logEach(logln)

@OptIn(ExperimentalTime::class)
fun Iterable<*>.logEachWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println,
) = iterator().logEachWithMillis(mark, logln)

@OptIn(ExperimentalTime::class)
fun Sequence<*>.logEachWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println,
) = iterator().logEachWithMillis(mark, logln)



suspend fun Flow<*>.onEachLog(logln: (Any?) -> Unit = ::println) = onEach(logln)

suspend fun Flow<*>.logEach(logln: (Any?) -> Unit = ::println) = onEachLog(logln).collect()

private fun Any?.toStringWithMillis(from: TimeMark, separator: String = " ") =
    "${from.elapsedNow().inWholeMilliseconds}$separator$this"

@OptIn(ExperimentalTime::class)
suspend fun Flow<*>.onEachLogWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println,
) = onEachLog { logln(it.toStringWithMillis(mark)) }

@OptIn(ExperimentalTime::class)
suspend fun Flow<*>.logEachWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println
) = onEachLogWithMillis(mark, logln).collect()



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
    this.logln()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.startInGnomeTermIfUserConfirms(kommand = this, execInDir = execInDir) }
}

fun Kommand.checkInIdeap(
    expectedKommandLine: String? = null,
    execInDir: String? = null,
    platform: CliPlatform = SYS
) {
    this.logln()
    if (expectedKommandLine != null) check(expectedKommandLine == line())
    ifInteractive { platform.run {
        val tmpFile = "$pathToUserTmp/tmp.notes"
        start(this@checkInIdeap, dir = execInDir, outFile = tmpFile).waitForExit()
        start(ideap { +tmpFile }).waitForExit()
    } }
}

