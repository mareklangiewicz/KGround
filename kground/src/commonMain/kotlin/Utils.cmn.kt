package pl.mareklangiewicz.kground

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import pl.mareklangiewicz.ulog.hack.ulog
import pl.mareklangiewicz.ulog.i


infix fun <T : Any> List<T>.plusIfNN(element: T?) = if (element == null) this else this + element
infix fun <T : Any> List<T>.prependIfNN(element: T?) = if (element == null) this else listOf(element) + this


fun Any.classSimpleWords() = this::class.simpleName!!
  .split(Regex("(?<=\\w)(?=\\p{Upper})")).map { it.lowercase() }


inline fun <T> Iterator<T>.logEach(logln: (T) -> Unit = { ulog.i(it) }) = forEach(logln)

// TODO: move timemarks saving support to [UHackySharedFlowLog] and remove all ...WithMillis extensions here
fun Iterator<*>.logEachWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = { ulog.i(it) },
) = logEach { logln(it.toStringWithMillis(mark)) }

inline fun <T> Iterable<T>.logEach(logln: (T) -> Unit = { ulog.i(it) }) = iterator().logEach(logln)
inline fun <T> Sequence<T>.logEach(logln: (T) -> Unit = { ulog.i(it) }) = iterator().logEach(logln)
inline fun <K, V> Map<K, V>.logEachEntry(logln: (Map.Entry<K, V>) -> Unit = { ulog.i(it) }) = iterator().logEach(logln)

fun Iterable<*>.logEachWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = { ulog.i(it) },
) = iterator().logEachWithMillis(mark, logln)

fun Sequence<*>.logEachWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = { ulog.i(it) },
) = iterator().logEachWithMillis(mark, logln)


fun <T> Flow<T>.onEachLog(logln: (T) -> Unit = { ulog.i(it) }) = onEach(logln)

suspend fun <T> Flow<T>.logEach(logln: (T) -> Unit = { ulog.i(it) }) = onEachLog(logln).collect()

private fun Any?.toStringWithMillis(from: TimeMark, separator: String = " ") =
  "${from.elapsedNow().inWholeMilliseconds}$separator$this"

suspend fun <T> Flow<T>.onEachLogWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = { ulog.i(it) },
) = onEachLog { logln(it.toStringWithMillis(mark)) }

suspend fun <T> Flow<T>.logEachWithMillis(
  mark: TimeMark = TimeSource.Monotonic.markNow(),
  logln: (String) -> Unit = { ulog.i(it) },
) = onEachLogWithMillis(mark, logln).collect()


