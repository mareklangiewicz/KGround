package pl.mareklangiewicz.kground

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource


val Any?.unit get() = Unit


/** @throws BadArgErr if not found or found more than one */
fun Regex.findSingle(input: CharSequence, startIndex: Int = 0): MatchResult {
    val r1 = find(input, startIndex).reqNN { "this regex: \"$this\" is nowhere in input" }
    val r2 = find(input, r1.range.last + 1)
    r2.reqEqNull { "this regex: \"$this\" has been found second time at idx: ${r2!!.range.first}" }
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


