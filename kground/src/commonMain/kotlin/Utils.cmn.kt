package pl.mareklangiewicz.kground

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlin.time.TimeMark
import kotlin.time.TimeSource


infix fun <T : Any> List<T>.plusIfNN(element: T?) = if (element == null) this else this + element
infix fun <T : Any> List<T>.prependIfNN(element: T?) = if (element == null) this else listOf(element) + this


fun Any.classSimpleWords() = this::class.simpleName!!
  .split(Regex("(?<=\\w)(?=\\p{Upper})")).map { it.lowercase() }


inline fun <T> Iterator<T>.logEach(logln: (T) -> Unit = ::println) = forEach(logln)

fun Iterator<*>.logEachWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = ::println,
) = logEach { logln(it.toStringWithMillis(mark)) }

inline fun <T> Iterable<T>.logEach(logln: (T) -> Unit = ::println) = iterator().logEach(logln)
inline fun <T> Sequence<T>.logEach(logln: (T) -> Unit = ::println) = iterator().logEach(logln)
inline fun <K, V> Map<K, V>.logEachEntry(logln: (Map.Entry<K, V>) -> Unit = ::println) = iterator().logEach(logln)

fun Iterable<*>.logEachWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = ::println,
) = iterator().logEachWithMillis(mark, logln)

fun Sequence<*>.logEachWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = ::println,
) = iterator().logEachWithMillis(mark, logln)


fun <T> Flow<T>.onEachLog(logln: (T) -> Unit = ::println) = onEach(logln)

suspend fun <T> Flow<T>.logEach(logln: (T) -> Unit = ::println) = onEachLog(logln).collect()

private fun Any?.toStringWithMillis(from: TimeMark, separator: String = " ") =
  "${from.elapsedNow().inWholeMilliseconds}$separator$this"

suspend fun <T> Flow<T>.onEachLogWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = ::println,
) = onEachLog { logln(it.toStringWithMillis(mark)) }

suspend fun <T> Flow<T>.logEachWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = ::println,
) = onEachLogWithMillis(mark, logln).collect()


