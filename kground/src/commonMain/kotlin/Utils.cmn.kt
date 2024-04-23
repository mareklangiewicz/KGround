@file:Suppress("NOTHING_TO_INLINE")

package pl.mareklangiewicz.kground

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import pl.mareklangiewicz.ulog.ULog
import pl.mareklangiewicz.ulog.ULogLevel
import pl.mareklangiewicz.ulog.full
import pl.mareklangiewicz.ulog.implictx
import pl.mareklangiewicz.ulog.timed


infix fun <T : Any> List<T>.plusIfNN(element: T?) = if (element == null) this else this + element
infix fun <T : Any> List<T>.prependIfNN(element: T?) = if (element == null) this else listOf(element) + this


fun Any.classSimpleWords() = this::class.simpleName!!
  .split(Regex("(?<=\\w)(?=\\p{Upper})")).map { it.lowercase() }


inline fun <T> Iterator<T>.logEach(log: ULog, level: ULogLevel = ULogLevel.INFO, timed: Boolean = true) {
  forEach { if (timed) log.timed(level, it) else log(level, it) }
}

inline fun <T> Iterable<T>.logEach(log: ULog, level: ULogLevel = ULogLevel.INFO, timed: Boolean = true) =
  iterator().logEach(log, level, timed)

inline fun <T> Sequence<T>.logEach(log: ULog, level: ULogLevel = ULogLevel.INFO, timed: Boolean = true) =
  iterator().logEach(log, level, timed)

inline fun <K, V> Map<K, V>.logEachEntry(log: ULog, level: ULogLevel = ULogLevel.INFO, timed: Boolean = true) =
  iterator().logEach(log, level, timed)

suspend inline fun <T> Iterator<T>.logEach(level: ULogLevel = ULogLevel.INFO, full: Boolean = true) {
  val log = implictx<ULog>()
  forEach { if (full) log.full(level, it) else log(level, it) }
}

suspend inline fun <T> Iterable<T>.logEach(level: ULogLevel = ULogLevel.INFO, full: Boolean = true) =
  iterator().logEach(level, full)

suspend inline fun <T> Sequence<T>.logEach(level: ULogLevel = ULogLevel.INFO, full: Boolean = true) =
  iterator().logEach(level, full)

suspend inline fun <K, V> Map<K, V>.logEachEntry(level: ULogLevel = ULogLevel.INFO, full: Boolean = true) =
  iterator().logEach(level, full)

inline fun <T> Flow<T>.onEachLog(log: ULog, level: ULogLevel = ULogLevel.INFO, timed: Boolean = true) =
  onEach { if (timed) log.timed(level, it) else log(level, it) }

suspend inline fun <T> Flow<T>.onEachLog(level: ULogLevel = ULogLevel.INFO, full: Boolean = true): Flow<T> {
  val log = implictx<ULog>()
  return onEach { if (full) log.full(level, it) else log(level, it) }
}

suspend inline fun <T> Flow<T>.logEach(level: ULogLevel = ULogLevel.INFO, full: Boolean = true) =
  onEachLog(level, full).collect()

