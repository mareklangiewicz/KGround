package pl.mareklangiewicz.kommand

import kotlinx.coroutines.flow.*
import pl.mareklangiewicz.kommand.CliPlatform.Companion.SYS
import pl.mareklangiewicz.kommand.gnome.startInGnomeTermIfUserConfirms
import pl.mareklangiewicz.kommand.konfig.konfigInUserHomeConfigDir
import kotlin.contracts.*
import kotlin.time.*





// TODO: move some of it to some ukommon lib

val Any?.unit get() = Unit


// Why introducing own strange exceptions? To easily spot when my own exceptions happen in large codebases.
// I'm using bad/hacky practices (like funny shortcut names) intentionally to avoid name clashes with other code.
// These exceptions still override std "Illegal(State/Argument)" so can still be handled as usual.

interface BadErr

class BadStateErr(override val message: String? = null, override val cause: Throwable? = null): IllegalStateException(), BadErr
class BadArgErr(override val message: String? = null, override val cause: Throwable? = null): IllegalArgumentException(), BadErr

inline fun bad(lazyMessage: () -> String = { "this is bad" }): Nothing = throw BadStateErr(lazyMessage())
inline fun badArg(lazyMessage: () -> String = { "this arg is bad" }): Nothing = throw BadArgErr(lazyMessage())

@OptIn(ExperimentalContracts::class)
inline fun chk(value: Boolean, lazyMessage: () -> String = { "this is bad" }) {
    contract { returns() implies value }
    value || bad(lazyMessage)
}

@OptIn(ExperimentalContracts::class)
inline fun req(value: Boolean, lazyMessage: () -> String = { "this arg is bad" }) {
    contract { returns() implies value }
    value || badArg(lazyMessage)
}

inline fun <T: Any> T?.chkNN(lazyMessage: () -> String = { "this null is bad" }): T = this ?: bad(lazyMessage)
inline fun <T: Any> T?.reqNN(lazyMessage: () -> String = { "this null arg is bad" }): T = this ?: badArg(lazyMessage)

/** @throws BadArgErr if not found or found more than one */
fun Regex.findSingle(input: CharSequence, startIndex: Int = 0): MatchResult {
    val r1 = find(input, startIndex).reqNN { "this regex: \"$this\" is nowhere in input" }
    val r2 = find(input, r1.range.last + 1)
    req(r2 == null) { "this regex: \"$this\" has been found second time at idx: ${r2!!.range.first}" }
    return r1
}

/** @throws BadArgErr if not found or found more than one */
fun Regex.replaceSingle(input: CharSequence, replacement: CharSequence, startIndex: Int = 0): CharSequence =
    input.replaceRange(findSingle(input, startIndex).range, replacement)



infix fun <T: Any> List<T>.plusIfNN(element: T?) = if (element == null) this else this + element
infix fun <T: Any> List<T>.prependIfNN(element: T?) = if (element == null) this else listOf(element) + this

fun Any.classSimpleWords() = this::class.simpleName!!
    .split(Regex("(?<=\\w)(?=\\p{Upper})")).map { it.lowercase() }


fun String.removeReqPrefix(prefix: CharSequence): String {
    req(startsWith(prefix)) { "Can not find prefix: $prefix" }
    return removePrefix(prefix)
}

fun String.removeReqSuffix(suffix: CharSequence): String {
    req(endsWith(suffix)) { "Can not find suffix: $suffix" }
    return removeSuffix(suffix)
}

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



suspend fun <T> Flow<T>.onEachLog(logln: (T) -> Unit = ::println) = onEach(logln)

suspend fun <T> Flow<T>.logEach(logln: (T) -> Unit = ::println) = onEachLog(logln).collect()

private fun Any?.toStringWithMillis(from: TimeMark, separator: String = " ") =
    "${from.elapsedNow().inWholeMilliseconds}$separator$this"

@OptIn(ExperimentalTime::class)
suspend fun <T> Flow<T>.onEachLogWithMillis(
    mark: TimeMark = TimeSource.Monotonic.markNow(),
    logln: (String) -> Unit = ::println,
) = onEachLog { logln(it.toStringWithMillis(mark)) }

@OptIn(ExperimentalTime::class)
suspend fun <T> Flow<T>.logEachWithMillis(
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

fun Kommand.chkWithUser(expectedKommandLine: String? = null, execInDir: String? = null, platform: CliPlatform = SYS) {
    this.logln()
    if (expectedKommandLine != null) chk(expectedKommandLine == line())
    ifInteractive { platform.startInGnomeTermIfUserConfirms(kommand = this, execInDir = execInDir) }
}

fun Kommand.chkInIdeap(
    expectedKommandLine: String? = null,
    execInDir: String? = null,
    platform: CliPlatform = SYS
) {
    this.logln()
    if (expectedKommandLine != null) chk(expectedKommandLine == line())
    ifInteractive { platform.run {
        val tmpFile = "$pathToUserTmp/tmp.notes"
        start(this@chkInIdeap, dir = execInDir, outFile = tmpFile).waitForExit()
        start(ideap { +tmpFile }).waitForExit()
    } }
}

